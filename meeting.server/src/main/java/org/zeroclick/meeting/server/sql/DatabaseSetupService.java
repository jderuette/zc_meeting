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
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.server.sql.migrate.DatabaseMigrateService;

@ApplicationScoped
@CreateImmediately
public class DatabaseSetupService implements IDataStoreService {
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseSetupService.class);

	private Set<String> existingTables;

	@PostConstruct
	public void autoCreateDatabase() {
		final IJobManager jobManager = BEANS.get(IJobManager.class);
		final DatabaseMigrateService dbms = BEANS.get(DatabaseMigrateService.class);
		final RunContext context = BEANS.get(SuperUserRunContextProducer.class).produce();
		IFuture<Void> dropResult = null;

		if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoDropAllProperty.class)) {
			LOG.warn("AutoDrop set to TRUE, droping all existing Tables and contents");
			try {

				final IRunnable runnable = new IRunnable() {
					@Override
					@SuppressWarnings("PMD.SignatureDeclareThrowsException")
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
					@SuppressWarnings("PMD.SignatureDeclareThrowsException")
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
		this.createSequence("EVENT_ID_SEQ");

		if (!this.getExistingTables().contains("EVENT")) {

			SQL.insert(SQLs.EVENT_CREATE_TABLE);
			LOG.info("Database table 'EVENT' created");

			if (CONFIG.getPropertyValue(DatabaseProperties.DatabaseAutoPopulateProperty.class)) {
				SQL.insert(SQLs.EVENT_INSERT_SAMPLE + SQLs.EVENT_VALUES_01);
				SQL.insert(SQLs.EVENT_INSERT_SAMPLE + SQLs.EVENT_VALUES_02);
				LOG.info("Database table 'EVENT' populated with sample data");
			}
		} else {
			LOG.info("Database table 'EVENT' already exist");
		}
	}

	public void createOAuthCredentialTable() {
		this.createSequence("OAUHTCREDENTIAL_ID_SEQ");

		if (!this.getExistingTables().contains("OAUHTCREDENTIAL")) {
			final String blobType = this.getBlobType();
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
		this.createSequence("USER_ID_SEQ");

		if (!this.getExistingTables().contains("APP_USER")) {
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
		if (!this.getExistingTables().contains("ROLE")) {
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
		} else {
			LOG.info("Database table 'ROLE_PERMISSION' already exist");
		}
		if (!this.getExistingTables().contains("USER_ROLE")) {
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

	private String getBlobType() {
		final String stylClassName = SQL.getSqlStyle().getClass().getName();
		String blobType = "BLOB";
		if ("org.eclipse.scout.rt.server.jdbc.postgresql.PostgreSqlStyle".equals(stylClassName)) {
			blobType = "bytea";
		}
		return blobType;
	}

	@Override
	public void dropDataStore() {

		this.dropTable("EVENT");
		this.dropSequence("EVENT_ID_SEQ");
		this.dropTable("OAUHTCREDENTIAL");
		this.dropSequence("OAUHTCREDENTIAL_ID_SEQ");
		this.dropTable("APP_USER");
		this.dropSequence("USER_ID_SEQ");
		this.dropTable("ROLE");
		this.dropTable("ROLE_PERMISSION");
		this.dropTable("USER_ROLE");

		this.existingTables = null;

	}

	private void dropTable(final String tableName) {
		if (this.getExistingTables(Boolean.TRUE).contains(tableName)) {
			SQL.update(SQLs.GENERIC_DROP_TABLE.replace("__tableName__", tableName));
		}
	}

	private void createSequence(final String sequenceName, final Integer start) {
		if (!this.isSequenceExists(sequenceName)) {
			SQL.update(SQLs.GENERIC_CREATE_SEQUENCE.replace("__seqName__", sequenceName).replace("__seqStart__",
					start.toString()));
		} else {
			LOG.info("Sequence : " + sequenceName + " already exists, not created");
		}
	}

	private void createSequence(final String sequenceName) {
		this.createSequence(sequenceName, 1);
	}

	private void dropSequence(final String sequenceName) {
		if (this.isSequenceExists(sequenceName)) {
			SQL.update(SQLs.GENERIC_DROP_SEQUENCE.replace("__seqName__", sequenceName));
		}
	}

	private Boolean isSequenceExists(final String sequenceName) {
		Boolean sequenceExists = Boolean.TRUE;

		final Object sequences[][] = SQL.select(SQLs.GENERIC_SEQUENCE_EXISTS.replace("__seqName__", sequenceName));

		if (null == sequences || sequences.length == 0 || sequences[0].length == 0 || null == sequences[0][0]) {
			sequenceExists = Boolean.FALSE;
		}
		return sequenceExists;
	}

	@Override
	public void createDataStore() {
		this.createEventTable();
		this.createOAuthCredentialTable();
		this.createRolePermisisonTables();
	}

	private Set<String> retrieveExistingTables() {
		final StringArrayHolder tables = new StringArrayHolder();
		final String stylClassName = SQL.getSqlStyle().getClass().getName();
		if ("org.eclipse.scout.rt.server.jdbc.postgresql.PostgreSqlStyle".equals(stylClassName)) {
			SQL.selectInto(SQLs.SELECT_TABLE_NAMES_POSTGRESQL, new NVPair("result", tables));
		} else if ("org.eclipse.scout.rt.server.jdbc.derby.DerbySqlStyle".equals(stylClassName)) {
			SQL.selectInto(SQLs.SELECT_TABLE_NAMES_DERBY, new NVPair("result", tables));
		} else {
			LOG.error("Unimplemented getExistingTables for SqlStyle : " + stylClassName);
		}

		return CollectionUtility.hashSet(tables.getValue());
	}

	/**
	 * By default existing tables are cached, use
	 * {@link #getExistingTables(Boolean)} if you need cache refresh
	 *
	 * @return
	 */
	protected Set<String> getExistingTables() {
		return this.getExistingTables(Boolean.FALSE);
	}

	/**
	 * get exiting tables in DataBase.
	 *
	 * @param refreshCache
	 *            : if FALSE and a cache exist, cached tables are returned.
	 *
	 * @return
	 */
	protected Set<String> getExistingTables(final Boolean refreshCache) {
		if (null == this.existingTables || refreshCache) {
			this.existingTables = this.retrieveExistingTables();
		}
		return this.existingTables;
	}

}
