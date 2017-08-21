package org.zeroclick.meeting.server.sql;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.server.sql.migrate.DatabaseMigrateService;

@ApplicationScoped
@CreateImmediately
public class DatabaseSetupService implements IDataStoreService {
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseSetupService.class);

	private DatabaseHelper databaseHelper;

	@PostConstruct
	public void autoCreateDatabase() {
		final IJobManager jobManager = BEANS.get(IJobManager.class);
		final DatabaseMigrateService dbms = BEANS.get(DatabaseMigrateService.class);
		final RunContext context = BEANS.get(SuperUserRunContextProducer.class).produce();

		this.databaseHelper = DatabaseHelper.get();
		IFuture<Void> dropResult = null;

		if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoDropAllProperty.class)) {
			LOG.warn("AutoDrop set to TRUE, droping all existing Tables and contents");
			try {

				final IRunnable runnable = new IRunnable() {
					@Override
					public void run() throws Exception {
						DatabaseSetupService.this.dropDataStore();
					}
				};

				dropResult = jobManager.schedule(runnable,
						new JobInput().withRunContext(context).withName("DropingDatabase"));

				// context.run(runnable);
				// dropRunMonitor = context.getRunMonitor();
			} catch (final RuntimeException e) {
				BEANS.get(ExceptionHandler.class).handle(e);
			}
		}
		if (null != dropResult) {
			// jobManager.awaitDone(Jobs.newFutureFilterBuilder().andMatchFuture(dropResult).toFilter(),
			// 15, TimeUnit.SECONDS);
			jobManager.addListener(Jobs.newEventFilterBuilder().andMatchFuture(dropResult).toFilter(),
					new IJobListener() {

						@Override
						public void changed(final JobEvent event) {
							LOG.info(event + " occurs");
							if (JobState.DONE == event.getData().getState()) {
								LOG.info(event
										+ " completed, checking if database need to be populated and populate if required");
								DatabaseSetupService.this.populateDataBase();
								// check for dataBase update
								dbms.checkMigration();
							}

						}
					});
		} else {
			this.populateDataBase();
			// check for dataBase update
			dbms.checkMigration();
		}

	}

	protected void populateDataBase() {
		final RunContext context = BEANS.get(SuperUserRunContextProducer.class).produce();
		if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoPopulateProperty.class)) {
			try {
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
		this.databaseHelper.createSequence("EVENT_ID_SEQ");

		if (!this.databaseHelper.existTable("EVENT")) {

			SQL.insert(SQLs.EVENT_CREATE_TABLE);
			LOG.info("Database table 'EVENT' created");

			if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoPopulateProperty.class)) {
				SQL.insert(SQLs.EVENT_INSERT_SAMPLE + SQLs.EVENT_VALUES_01);
				SQL.insert(SQLs.EVENT_INSERT_SAMPLE + SQLs.EVENT_VALUES_02);
				SQL.insert(SQLs.EVENT_INSERT_SAMPLE + SQLs.EVENT_VALUES_03);
				LOG.info("Database table 'EVENT' populated with sample data");
			}
		} else {
			LOG.info("Database table 'EVENT' already exist");
		}
	}

	public void createOAuthCredentialTable() {
		this.databaseHelper.createSequence("OAUHTCREDENTIAL_ID_SEQ");

		if (!this.databaseHelper.existTable("OAUHTCREDENTIAL")) {
			final String blobType = this.databaseHelper.getBlobType();
			SQL.insert(SQLs.OAUHTCREDENTIAL_CREATE_TABLE.replace("__blobType__", blobType));
			LOG.info("Database table 'OAUHTCREDENTIAL' created");

			if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoPopulateProperty.class)) {
				SQL.insert(SQLs.OAUHTCREDENTIAL_INSERT_SAMPLE + SQLs.OAUHTCREDENTIAL_VALUES_01);
				LOG.info("Database table 'OAUHTCREDENTIAL' populated with sample data");
			}
		} else {
			LOG.info("Database table 'OAUHTCREDENTIAL' already exist");
		}

	}

	protected void createUserTable() {
		this.databaseHelper.createSequence("USER_ID_SEQ");

		if (!this.databaseHelper.existTable("APP_USER")) {
			SQL.insert(SQLs.USER_CREATE_TABLE);
			LOG.info("Database table 'USER' created");

			if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoPopulateProperty.class)) {
				SQL.insert(SQLs.USER_INSERT_SAMPLE + SQLs.USER_VALUES_01);
				SQL.insert(SQLs.USER_INSERT_SAMPLE + SQLs.USER_VALUES_02);
				LOG.info("Database table 'USER' populated with sample data");
			}
		} else {
			LOG.info("Database table 'APP_USER' already exist");
		}
	}

	protected void createRolePermisisonTables() {
		if (!this.databaseHelper.existTable("ROLE")) {
			SQL.insert(SQLs.ROLE_CREATE_TABLE);
			LOG.info("Database table 'ROLE' created");

			if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoPopulateProperty.class)) {
				SQL.insert(SQLs.ROLE_INSERT_SAMPLE + SQLs.ROLE_VALUES_01);
				SQL.insert(SQLs.ROLE_INSERT_SAMPLE + SQLs.ROLE_VALUES_02);
				LOG.info("Database table 'ROLE' populated with sample data");
			}
		} else {
			LOG.info("Database table 'ROLE' already exist");
		}

		if (!this.databaseHelper.existTable("ROLE_PERMISSION")) {
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
		} else {
			LOG.info("Database table 'ROLE_PERMISSION' already exist");
		}
		if (!this.databaseHelper.existTable("USER_ROLE")) {
			SQL.insert(SQLs.USER_ROLE_CREATE_TABLE);
			LOG.info("Database table 'USER_ROLE', created");

			if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoPopulateProperty.class)) {
				SQL.insert(SQLs.USER_ROLE_INSERT_SAMPLE + SQLs.USER_ROLE_VALUES_01);
				SQL.insert(SQLs.USER_ROLE_INSERT_SAMPLE + SQLs.USER_ROLE_VALUES_02);
				LOG.info("Database table 'USER_ROLE' populated with sample data");
			}
		} else {
			LOG.info("Database table 'USER_ROLE' already exist");
		}

	}

	@Override
	public void dropDataStore() {

		final DatabaseMigrateService databaseMigrateService = BEANS.get(DatabaseMigrateService.class);

		this.databaseHelper.dropTable("EVENT");
		this.databaseHelper.dropSequence("EVENT_ID_SEQ");
		this.databaseHelper.dropTable("OAUHTCREDENTIAL");
		this.databaseHelper.dropSequence("OAUHTCREDENTIAL_ID_SEQ");
		this.databaseHelper.dropTable("APP_USER");
		this.databaseHelper.dropSequence("USER_ID_SEQ");
		this.databaseHelper.dropTable("ROLE");
		this.databaseHelper.dropTable("ROLE_PERMISSION");
		this.databaseHelper.dropTable("USER_ROLE");

		databaseMigrateService.undoMigration();

		this.databaseHelper.resetExistingTablesCache();

	}

	@Override
	public void createDataStore() {
		this.createEventTable();
		this.createOAuthCredentialTable();
		this.createRolePermisisonTables();
	}
}
