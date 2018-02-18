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
package org.zeroclick.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * @author djer
 *
 */
public class VersionHelper {

	private static final Logger LOG = LoggerFactory.getLogger(VersionHelper.class);

	private static final String MAVEN_PACKAGE = "org.zeroclick";
	private static final String MAVEN_ARTIFACT = "meeting.server";
	private static final String DEFAULT_UNKNOW_VERSION = "unknow";

	private static VersionHelper instance;

	private VersionHelper() {
	}

	public static VersionHelper get() {
		if (null == instance) {
			instance = new VersionHelper();
		}
		return instance;
	}

	/**
	 * Extract the sourceCode version (from maven pom file/manifest file)
	 *
	 * /!\ will get the version of the package of this (VersionHelper) class !
	 * <br/>
	 * Move this class in your own package before use !
	 *
	 * @see https://stackoverflow.com/a/38193883/8029150 <br/>
	 *
	 * @return
	 */
	public synchronized final String getSourceVersion() {
		String version;
		version = this.getPomXmlVersion();
		if (null == version) {
			version = this.getPomPropertiesVersion();
		}
		if (null == version) {
			version = this.getJavaApiVersion();
		}

		if (null == version) {
			version = this.getMavenArchiverVersion();
		}

		final String bestVersion = null == version ? "" : version.trim();
		return bestVersion.isEmpty() ? DEFAULT_UNKNOW_VERSION : bestVersion;
	}

	/**
	 * Try to get version number from pom.xml (available in Eclipse)
	 *
	 * @return
	 */
	private String getPomXmlVersion() {
		String pomXmlVersion = null;
		try {
			final Path path = this.getProjectFolder();
			final Path pom = path.resolve("pom.xml");
			try (InputStream pomIs = Files.newInputStream(pom)) {
				final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pomIs);
				doc.getDocumentElement().normalize();
				String version = (String) XPathFactory.newInstance().newXPath().compile("/project/version")
						.evaluate(doc, XPathConstants.STRING);
				if (version != null) {
					version = version.trim();
					if (!version.isEmpty()) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("SourceCode version loaded from pom.xml file");
						}
						pomXmlVersion = version;
					}
				}
			}
		} catch (final Exception e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception while searching version in pom.xml file", e);
			}
		}
		return pomXmlVersion;
	}

	/**
	 * Try to get version number from maven properties in jar's META-INF
	 *
	 * @return
	 */
	private String getPomPropertiesVersion() {

		String pomPropertiesVersion = null;
		try (InputStream pomPropertiesIs = this.getClass()
				.getResourceAsStream("/META-INF/maven/" + MAVEN_PACKAGE + "/" + MAVEN_ARTIFACT + "/pom.properties")) {
			final String version = this.searchVersionFromProps(pomPropertiesIs);
			if (!version.isEmpty()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("SourceCode version loaded from pom.properties file");
				}
				pomPropertiesVersion = version;
			}
		} catch (final Exception e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception while searching version in pom.properties file", e);
			}
		}

		return pomPropertiesVersion;
	}

	/**
	 * Fallback to using Java API to get version from MANIFEST.MF
	 *
	 * @return
	 */
	private String getJavaApiVersion() {
		String javaApiVersion = null;
		String version = null;
		final Package pkg = this.getClass().getPackage();
		if (pkg != null) {
			version = pkg.getImplementationVersion();
			if (version == null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("SourceCode version loaded from standard getImplementationVersion()");
				}
				javaApiVersion = pkg.getSpecificationVersion();
			}
		}
		return javaApiVersion;
	}

	private String getMavenArchiverVersion() {
		String mavenArchiverPomPropertiesVersion = null;
		try {
			final Path projectPath = this.getProjectFolder();
			final Path mvnArchiver = Paths.get(projectPath.toString() + "/target/maven-archiver/");
			final Path mvnArchiverProps = mvnArchiver.resolve("pom.properties");
			try (InputStream mvnArchiverPropsIs = Files.newInputStream(mvnArchiverProps)) {
				final String version = this.searchVersionFromProps(mvnArchiverPropsIs);
				if (!version.isEmpty()) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("SourceCode version loaded from maven archiver pom.properties file");
					}
					mavenArchiverPomPropertiesVersion = version;
				}
			}
		} catch (final Exception e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception while searching version in maven archiver pom.properties file", e);
			}
		}
		return mavenArchiverPomPropertiesVersion;
	}

	private Path getProjectFolder() {
		Path path = null;
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
				path = absolutePackagePath;
				for (int i = 0, segmentsToRemove = packagePathSegments + 2; i < segmentsToRemove; i++) {
					path = path.getParent();
				}
			}
		} catch (final Exception e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception while resolving projectFolder to search version in pom.xml file", e);
			}
		}

		return path;
	}

	private String searchVersionFromProps(final InputStream propsInputStream) {

		String propertiesVersion = "";
		if (propsInputStream != null) {
			final Properties allPropos = new Properties();
			try {
				allPropos.load(propsInputStream);

				final String version = allPropos.getProperty("version", "").trim();
				if (!version.isEmpty()) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("SourceCode version loaded from pom.properties file");
					}
					propertiesVersion = version;
				}
			} catch (final IOException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Exception while loading properties from InputTream :" + propsInputStream, e);
				}
			}
		}
		return propertiesVersion;
	}
}
