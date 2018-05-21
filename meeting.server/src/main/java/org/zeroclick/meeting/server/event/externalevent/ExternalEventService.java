package org.zeroclick.meeting.server.event.externalevent;

import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.AbstractCommonService;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.data.PatchMultiInviteeMeeting;
import org.zeroclick.meeting.shared.event.ReadEventPermission;
import org.zeroclick.meeting.shared.event.externalevent.ExternalEventFormData;
import org.zeroclick.meeting.shared.event.externalevent.ExternalEventTablePageData;
import org.zeroclick.meeting.shared.event.externalevent.IExternalEventService;

public class ExternalEventService extends AbstractCommonService implements IExternalEventService {

	private static final Logger LOG = LoggerFactory.getLogger(ExternalEventService.class);

	@Override
	protected Logger getLog() {
		return LOG;
	}

	@Override
	public ExternalEventTablePageData getExternalEventTableData(final SearchFilter filter) {
		return this.getExternalEventTableData(filter, Boolean.FALSE);
	}

	private ExternalEventTablePageData getExternalEventTableData(final SearchFilter filter,
			final Boolean displayAllForAdmin) {
		final ExternalEventTablePageData pageData = new ExternalEventTablePageData();

		final String ownerFilter = "";
		final Long currentConnectedUserId = 0L;
		if (!displayAllForAdmin
				|| ACCESS.getLevel(new ReadEventPermission((Long) null)) != ReadEventPermission.LEVEL_ALL) {
			// ownerFilter = SQLs.EVENT_PAGE_SELECT_FILTER_USER_OR_RECIPIENT;
			// currentConnectedUserId = super.userHelper.getCurrentUserId();
		}

		final String sql = SQLs.EXTERNAL_EVENT_PAGE_SELECT + ownerFilter + SQLs.EXTERNAL_EVENT_PAGE_DATA_SELECT_INTO;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("currentUser", currentConnectedUserId));

		return pageData;
	}

	@Override
	public ExternalEventFormData prepareCreate(final ExternalEventFormData formData) {
		// TODO Auto-generated method stub
		return formData;
	}

	@Override
	public ExternalEventFormData create(final ExternalEventFormData formData) {
		// TODO Djer permission Check ?
		// add a unique event id if necessary
		if (null == formData.getExternalId().getValue()) {
			formData.getExternalId()
					.setValue(Long.valueOf(SQL.getSequenceNextval(PatchMultiInviteeMeeting.EXTERNAL_EVENT_ID_SEQ)));
		}
		SQL.insert(SQLs.EXTERNAL_EVENT_INSERT, formData);
		final ExternalEventFormData storedData = this.store(formData);

		return storedData;
	}

	@Override
	public ExternalEventFormData load(final ExternalEventFormData formData) {
		// TODO Djer permission Check ?
		SQL.selectInto(SQLs.EXTERNAL_EVENT_SELECT, formData);

		return formData;
	}

	@Override
	public ExternalEventFormData store(final ExternalEventFormData formData) {
		// TODO Djer permission Check ?
		SQL.update(SQLs.EXTERNAL_EVENT_UPDATE, formData);

		return formData;
	}

}
