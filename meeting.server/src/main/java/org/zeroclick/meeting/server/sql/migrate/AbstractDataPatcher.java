/**
 * Copyright 2017 Djer13

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
limitations under the License.
 */
package org.zeroclick.meeting.server.sql.migrate;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.zeroclick.comon.config.AbstractVersionConfigProperty;
import org.zeroclick.meeting.server.sql.SuperUserRunContextProducer;

import com.github.zafarkhaja.semver.Version;

/**
 * @author djer
 *
 */
public abstract class AbstractDataMigrate implements IDataMigrate {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractDataMigrate.class);

	private static final String MAVEN_PACKAGE = "org.zeroclick";

	private static final String MAVEN_ARTIFACT = "meeting.server";

	private String description;

	protected abstract void execute();

	protected Version getSourceCodeVersion() {
		String mavenVersion = this.getClass().getPackage().getImplementationVersion();
		if (null == mavenVersion) {
			mavenVersion = this.getSourceVersion();
		}
		return Version.valueOf(mavenVersion);
	}

	protected Version getDataVersion() {
		return CONFIG.getPropertyValue(DataVersionProperty.class);
	}

	@Override
	public void apply() {
		LOG.info("Launching migration");
		final RunContext context = BEANS.get(SuperUserRunContextProducer.class).produce();
		try {
			final IRunnable runnable = new IRunnable() {

				@Override
				@SuppressWarnings("PMD.SignatureDeclareThrowsException")
				public void run() throws Exception {
					AbstractDataMigrate.this.execute();
				}
			};

			context.run(runnable);
		} catch (final RuntimeException e) {
			BEANS.get(ExceptionHandler.class).handle(e);
		}

		LOG.info("Launching migration END");
	}

	@Override
	/**
	 * ByDefault return TRUE if major/minor sourceCode version is strictly
	 * higher than data version<br />
	 * Follow the semVer standard (see http://semver.org/)<<br />
	 * General semVer format : Major.Minor.Patch-pre-release+Build metadata :
	 * 1.0.0-alpha.1+001<br />
	 * Example :<br />
	 * 1.0.0 vs 1.0.5 => FALSE<br />
	 * 1.0.0 vs 1.0.0 => FALSE<br />
	 * 1.0.1 vs 1.1.0 => TRUE<br />
	 * 1.5.3 vs 1.5.6 => FALSE<br />
	 * 1.5.6 vs 1.5.6-build48+bidule => false
	 *
	 */
	public Boolean isRequired() {
		return this.getSourceCodeVersion().getMajorVersion() >= this.getDataVersion().getMajorVersion()
				&& this.getSourceCodeVersion().getMinorVersion() >= this.getDataVersion().getMinorVersion();
	}

	@Override
	/**
	 * ByDefault return TRUE if the datasourceVersion is strictly higher than
	 * the dataVersion.
	 */
	public Boolean canMigrate() {
		return this.getSourceCodeVersion().greaterThan(this.getDataVersion());
	}

	public synchronized final String getSourceVersion() {
		// Try to get version number from pom.xml (available in Eclipse)
		try {
			final String className = this.getClass().getName();
			final String classfileName = "/" + className.replace('.', '/') + ".class";
			final URL classfileResource = this.getClass().getResource(classfileName);
			if (classfileResource != null) {
				final Path absolutePackagePath = Paths.get(classfileResource.toURI()).getParent();
				final int packagePathSegments = className.length() - className.replace(".", "").length();
				// Remove package segments from path, plus two more levels
				// for "target/classes", which is the standard location for
				// classes in Eclipse.
				Path path = absolutePackagePath;
				for (int i = 0, segmentsToRemove = packagePathSegments + 2; i < segmentsToRemove; i++) {
					path = path.getParent();
				}
				final Path pom = path.resolve("pom.xml");
				try (InputStream is = Files.newInputStream(pom)) {
					final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
					doc.getDocumentElement().normalize();
					String version = (String) XPathFactory.newInstance().newXPath().compile("/project/version")
							.evaluate(doc, XPathConstants.STRING);
					if (version != null) {
						version = version.trim();
						if (!version.isEmpty()) {
							LOG.debug("SourceCode version loaded from pom.xml file");
							return version;
						}
					}
				}
			}
		} catch (final Exception e) {
			// Ignore
		}

		// Try to get version number from maven properties in jar's META-INF
		try (InputStream is = this.getClass()
				.getResourceAsStream("/META-INF/maven/" + MAVEN_PACKAGE + "/" + MAVEN_ARTIFACT + "/pom.properties")) {
			if (is != null) {
				final Properties p = new Properties();
				p.load(is);
				final String version = p.getProperty("version", "").trim();
				if (!version.isEmpty()) {
					LOG.debug("SourceCode version loaded from pom.properties file");
					return version;
				}
			}
		} catch (final Exception e) {
			// Ignore
		}

		// Fallback to using Java API to get version from MANIFEST.MF
		String version = null;
		final Package pkg = this.getClass().getPackage();
		if (pkg != null) {
			version = pkg.getImplementationVersion();
			if (version == null) {
				LOG.debug("SourceCode version loaded from standard getImplementationVersion()");
				version = pkg.getSpecificationVersion();
			}
		}
		version = version == null ? "" : version.trim();
		return version.isEmpty() ? "unknown" : version;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	protected void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * the data(base) version currently used
	 */
	public static class DataVersionProperty extends AbstractVersionConfigProperty {

		@Override
		public String getKey() {
			return "zeroclick.version.data";
		}

		@Override
		protected Version getDefaultValue() {
			return Version.valueOf("1.0.0");
		}
	}

}
