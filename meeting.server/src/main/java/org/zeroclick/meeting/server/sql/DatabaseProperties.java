/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.zeroclick.meeting.server.sql;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractSubjectConfigProperty;

public class DatabaseProperties {

	public static class DatabaseAutoCreateProperty extends AbstractBooleanConfigProperty {
		@Override
		protected Boolean getDefaultValue() {
			return Boolean.TRUE;
		}

		@Override
		public String getKey() {
			return "zeroclick.database.autocreate";
		}
	}

	public static class DatabaseAutoPopulateProperty extends AbstractBooleanConfigProperty {
		@Override
		protected Boolean getDefaultValue() {
			return Boolean.TRUE;
		}

		@Override
		public String getKey() {
			return "zeroclick.database.autopopulate";
		}
	}

	public static class DatabaseAutoDropAllProperty extends AbstractBooleanConfigProperty {
		@Override
		protected Boolean getDefaultValue() {
			return Boolean.TRUE;
		}

		@Override
		public String getKey() {
			return "zeroclick.database.autodropall";
		}
	}

	public static class JdbcMappingNameProperty extends AbstractStringConfigProperty {
		@Override
		protected String getDefaultValue() {
			return "jdbc:derby:memory:zeroclick-database";
		}

		@Override
		public String getKey() {
			return "zeroclick.database.jdbc.mapping.name";
		}
	}

	public static class SuperUserSubjectProperty extends AbstractSubjectConfigProperty {
		@Override
		protected Subject getDefaultValue() {
			return this.convertToSubject("system");
		}

		@Override
		public String getKey() {
			return "zeroclick.superuser";
		}
	}
}
