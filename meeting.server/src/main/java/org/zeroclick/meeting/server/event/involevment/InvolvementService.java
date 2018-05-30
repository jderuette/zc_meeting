package org.zeroclick.meeting.server.event.involevment;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.AbstractCommonService;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper.SubscriptionHelperData;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.shared.event.ReadEventPermission;
import org.zeroclick.meeting.shared.event.UpdateEventPermission;
import org.zeroclick.meeting.shared.event.involevment.IInvolvementService;
import org.zeroclick.meeting.shared.event.involevment.InvolvementFormData;
import org.zeroclick.meeting.shared.event.involevment.InvolvementFormData.EventId;
import org.zeroclick.meeting.shared.event.involevment.InvolvementTablePageData;

public class InvolvementService extends AbstractCommonService implements IInvolvementService {

	private static final Logger LOG = LoggerFactory.getLogger(InvolvementService.class);

	@Override
	protected Logger getLog() {
		return LOG;
	}

	@Override
	public InvolvementTablePageData getInvolevmentTableData(final SearchFilter filter) {
		return this.getInvolevmentTableData(filter, Boolean.TRUE);
	}

	private InvolvementTablePageData getInvolevmentTableData(final SearchFilter filter,
			final Boolean displayAllForAdmin) {
		final InvolvementTablePageData pageData = new InvolvementTablePageData();
		String ownerFilter = "";
		String eventIdFilter = "";
		Long eventId = null;
		Long currentConnectedUserId = 0L;

		if (null != filter && null != filter.getFormData()
				&& null != filter.getFormData().getFieldByClass(EventId.class)) {
			eventId = filter.getFormData().getFieldByClass(EventId.class).getValue();
		}
		if (null == eventId && !displayAllForAdmin
				|| ACCESS.getLevel(new ReadEventPermission((Long) null)) != ReadEventPermission.LEVEL_ALL) {
			// when eventId is specified, filter only on EventId not currentUser
			ownerFilter = SQLs.INVOLVEMENT_FILTER_USER_ID;
			currentConnectedUserId = super.userHelper.getCurrentUserId();
		}

		if (null != eventId) {
			this.checkPermission(new ReadEventPermission(eventId));
			eventIdFilter = SQLs.INVOLEVMENT_FILTER_EVENT_ID;
		}

		final String sql = SQLs.INVOLVEMENT_PAGE_SELECT + ownerFilter + eventIdFilter
				+ SQLs.INVOLVEMENT_PAGE_DATA_SELECT_INTO;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("currentUser", currentConnectedUserId),
				new NVPair("eventId", eventId));

		return pageData;
	}

	@Override
	public InvolvementFormData prepareCreate(final InvolvementFormData formData) {
		// TODO [djer] add business logic here.
		return formData;
	}

	@Override
	public InvolvementFormData create(final InvolvementFormData formData) {
		// Permission base on Event permission
		final SubscriptionHelper subHelper = BEANS.get(SubscriptionHelper.class);

		final SubscriptionHelperData subscriptionData = subHelper.canCreateEvent();
		if (!subscriptionData.isAccessAllowed()) {
			super.throwAuthorizationFailed();
		}

		if (null == formData.getEventId()) {
			throw new VetoException("EventId required");
		}
		if (null == formData.getUserId()) {
			throw new VetoException("userId required");
		}
		SQL.insert(SQLs.INVOLEVMENT_INSERT, formData);
		final InvolvementFormData storedData = this.store(formData);

		return storedData;
	}

	@Override
	public InvolvementFormData load(final InvolvementFormData formData) {
		SQL.selectInto(SQLs.INVOLVEMENT_SELECT + SQLs.INVOLEVMENT_FILTER_PRIMARY_KEY + SQLs.INVOLVEMENT_SELECT_INTO,
				formData);
		return formData;
	}

	@Override
	public InvolvementFormData store(final InvolvementFormData formData) {
		super.checkPermission(new UpdateEventPermission(formData.getEventId().getValue()));

		SQL.update(SQLs.INVOLEVMENT_UPDATE, formData);
		return formData;
	}

	@Override
	public void updateStatusAccepted(final Long eventId, final Long userId) {
		super.checkPermission(new UpdateEventPermission(eventId));
		SQL.update(SQLs.INVOLEVMENT_UPDATE_STATUS_ACCEPT, new NVPair("eventId", eventId), new NVPair("userId", userId));

	}

	@Override
	public InvolvementFormData getOrganizer(final Long eventId) {
		final InvolvementFormData formData = new InvolvementFormData();
		formData.getEventId().setValue(eventId);
		SQL.selectInto(SQLs.INVOLVEMENT_SELECT + SQLs.INVOLEVMENT_FILTER_EVENT_ID + SQLs.INVOLEVMENT_FILTER_ORGANIZER
				+ SQLs.INVOLVEMENT_SELECT_INTO, formData);
		return formData;
	}

	@Override
	public InvolvementTablePageData getParticipants(final Long eventId) {
		final InvolvementTablePageData formData = new InvolvementTablePageData();
		SQL.selectInto(
				SQLs.INVOLVEMENT_PAGE_SELECT + SQLs.INVOLEVMENT_FILTER_EVENT_ID + SQLs.INVOLEVMENT_FILTER_PARTICIPANT
						+ SQLs.INVOLVEMENT_PAGE_DATA_SELECT_INTO,
				new NVPair("page", formData), new NVPair("eventId", eventId));
		return formData;
	}

	@Override
	public void updateStatusRefused(final Long eventId, final Long userId, final String reason) {
		super.checkPermission(new UpdateEventPermission(eventId));
		SQL.update(SQLs.INVOLEVMENT_UPDATE_STATUS_REFUSE, new NVPair("eventId", eventId), new NVPair("userId", userId),
				new NVPair("reason", reason));

	}

	@Override
	public Boolean isGuest(final Long userId) {
		Boolean isGuest = Boolean.FALSE;
		final Object[][] data = SQL.select(SQLs.INVOLVEMENT_CHECK_IF_GUEST, new NVPair("guestEmail", userId));
		// at least one ROW

		if (null != data && null != data[0]) {
			isGuest = Boolean.TRUE;
		}

		return isGuest;
	}
}
