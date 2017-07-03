package org.zeroclick.meeting.server.sql;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.holders.StringArrayHolder;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@CreateImmediately
public class DatabaseSetupService implements IDataStoreService {
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseSetupService.class);

	@PostConstruct
	public void autoCreateDatabase() {
		if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoCreateProperty.class)) {
			try {
				final RunContext context = BEANS.get(SuperUserRunContextProducer.class).produce();
				final IRunnable runnable = new IRunnable() {

					@Override
					public void run() throws Exception {
						DatabaseSetupService.this.createEventTable();
						DatabaseSetupService.this.createOAuthCredentialTable();
						DatabaseSetupService.this.createUserTable();
						DatabaseSetupService.this.createRolePermisisonTables();
					}
				};

				context.run(runnable);
			} catch (final RuntimeException e) {
				BEANS.get(ExceptionHandler.class).handle(e);
			}
		}
	}

	protected void createEventTable() {
		if (!this.getExistingTables().contains("EVENT")) {
			SQL.insert(SQLs.EVENT_CREATE_TABLE);
			LOG.info("Database table 'EVENT' created");

			if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoPopulateProperty.class)) {
				SQL.insert(SQLs.EVENT_INSERT_SAMPLE + SQLs.EVENT_VALUES_01);
				SQL.insert(SQLs.EVENT_INSERT_SAMPLE + SQLs.EVENT_VALUES_02);
				LOG.info("Database table 'EVENT' populated with sample data");
			}
		}
	}

	public void createOAuthCredentialTable() {
		if (!this.getExistingTables().contains("OAUHTCREDENTIAL")) {
			SQL.insert(SQLs.OAUHTCREDENTIAL_CREATE_TABLE);
			LOG.info("Database table 'OAUHTCREDENTIAL' created");

			if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoPopulateProperty.class)) {
				SQL.insert(SQLs.OAUHTCREDENTIAL_INSERT_SAMPLE + SQLs.OAUHTCREDENTIAL_VALUES_01);
				LOG.info("Database table 'OAUHTCREDENTIAL' populated with sample data");
			}
		}
	}

	protected void createUserTable() {
		if (!this.getExistingTables().contains("USER")) {
			SQL.insert(SQLs.USER_CREATE_TABLE);
			LOG.info("Database table 'USER' created");

			if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoPopulateProperty.class)) {
				SQL.insert(SQLs.USER_INSERT_SAMPLE + SQLs.USER_VALUES_01);
				SQL.insert(SQLs.USER_INSERT_SAMPLE + SQLs.USER_VALUES_02);
				LOG.info("Database table 'USER' populated with sample data");
			}
		}
	}

	protected void createRolePermisisonTables() {
		if (!this.getExistingTables().contains("ROLE")) {
			SQL.insert(SQLs.ROLE_CREATE_TABLE);
			LOG.info("Database table 'ROLE' created");

			if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoPopulateProperty.class)) {
				SQL.insert(SQLs.ROLE_INSERT_SAMPLE + SQLs.ROLE_VALUES_01);
				SQL.insert(SQLs.ROLE_INSERT_SAMPLE + SQLs.ROLE_VALUES_02);
				LOG.info("Database table 'ROLE' populated with sample data");
			}
		}

		if (!this.getExistingTables().contains("ROLE_PERMISSION")) {
			SQL.insert(SQLs.ROLE_PERMISSION_CREATE_TABLE);
			LOG.info("Database table 'ROLE_PERMISSION', created");

			if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoPopulateProperty.class)) {
				if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoPopulateProperty.class)) {
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_01);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_02);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_04);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_05);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_06);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_07);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_08);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_09);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_11);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_12);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_13);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_14);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_17);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_18);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_19);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_20);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_21);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_22);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_23);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_100);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_101);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_102);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_103);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_104);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_105);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_106);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_107);
					SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_PERMISSION_VALUES_108);
					LOG.info("Database table 'ROLE_PERMISSION' populated with sample data");
				}
			}
		}
		if (!this.getExistingTables().contains("USER_ROLE")) {
			SQL.insert(SQLs.USER_ROLE_CREATE_TABLE);
			LOG.info("Database table 'USER_ROLE', created");

			if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoPopulateProperty.class)) {
				SQL.insert(SQLs.USER_ROLE_INSERT_SAMPLE + SQLs.USER_ROLE_VALUES_01);
				SQL.insert(SQLs.USER_ROLE_INSERT_SAMPLE + SQLs.USER_ROLE_VALUES_02);
				LOG.info("Database table 'USER_ROLE' populated with sample data");
			}
		}

	}

	private Set<String> getExistingTables() {
		final StringArrayHolder tables = new StringArrayHolder();
		SQL.selectInto(SQLs.SELECT_TABLE_NAMES, new NVPair("result", tables));
		return CollectionUtility.hashSet(tables.getValue());
	}

	@Override
	public void dropDataStore() {
		SQL.update(SQLs.EVENT_DROP_TABLE);
		SQL.update(SQLs.OAUHTCREDENTIAL_DROP_TABLE);
		SQL.update(SQLs.ROLE_DROP_TABLE);
		// SQL.update(SQLs.PERMISSION_DROP_TABLE);
		SQL.update(SQLs.ROLE_PERMISSION_DROP_TABLE);
		SQL.update(SQLs.USER_ROLE_DROP_TABLE);
	}

	@Override
	public void createDataStore() {
		this.createEventTable();
		this.createOAuthCredentialTable();
		this.createRolePermisisonTables();
	}
}
