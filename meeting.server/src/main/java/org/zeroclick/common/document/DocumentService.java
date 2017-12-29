package org.zeroclick.common.document;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.AbstractCommonService;
import org.zeroclick.common.shared.document.CreateDocumentPermission;
import org.zeroclick.common.shared.document.DocumentCreatedNotification;
import org.zeroclick.common.shared.document.DocumentModifiedNotification;
import org.zeroclick.common.shared.document.IDocumentService;
import org.zeroclick.common.shared.document.ReadDocumentPermission;
import org.zeroclick.common.shared.document.UpdateDocumentPermission;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.data.PatchCreateSubscription;
import org.zeroclick.meeting.shared.security.AccessControlService;

public class DocumentService extends AbstractCommonService implements IDocumentService {

	private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);

	@Override
	protected Logger getLog() {
		return LOG;
	}

	@Override
	public DocumentTablePageData getDocumentTableData(final SearchFilter filter) {
		final DocumentTablePageData pageData = new DocumentTablePageData();
		SQL.selectInto(SQLs.DOCUMENT_PAGE_SELECT + SQLs.DOCUMENT_PAGE_DATA_SELECT_INTO, new NVPair("page", pageData));
		return pageData;
	}

	@Override
	public DocumentFormData prepareCreate(final DocumentFormData formData) {
		super.checkPermission(new CreateDocumentPermission());
		if (null == formData.getDocumentId().getValue()) {
			formData.getDocumentId()
					.setValue(Long.valueOf(SQL.getSequenceNextval(PatchCreateSubscription.DOCUMENT_ID_SEQ)));
		}
		return formData;
	}

	@Override
	public DocumentFormData create(final DocumentFormData formData) {
		super.checkPermission(new CreateDocumentPermission());

		// add a unique event id if necessary
		if (null == formData.getDocumentId().getValue()) {
			formData.getDocumentId()
					.setValue(Long.valueOf(SQL.getSequenceNextval(PatchCreateSubscription.DOCUMENT_ID_SEQ)));
		}
		formData.getLastModificationDate().setValue(new Date());

		SQL.insert(SQLs.DOCUMENT_INSERT, formData);
		final DocumentFormData storedData = this.store(formData, Boolean.TRUE);

		final Set<String> notifiedUsers = this.buildNotifiedUsers(storedData);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers,
				new DocumentCreatedNotification(storedData));

		return storedData;
	}

	@Override
	public DocumentFormData load(final DocumentFormData formData) {
		super.checkPermission(new ReadDocumentPermission());
		SQL.selectInto(SQLs.DOCUMENT_SELECT + SQLs.DOCUMENT_SELECT_INTO, formData);

		formData.getContent().setValue(this.getContent(formData.getContentData()));

		this.loadLinkedRoles(formData);

		return formData;
	}

	private void loadLinkedRoles(final DocumentFormData formData) {
		formData.getLinkedRole().clearRows();
		SQL.selectInto(SQLs.DOCUMENT_SELECT_LINKED_ROLE, new NVPair("table", formData.getLinkedRole()),
				new NVPair("selectedDocumentId", formData.getDocumentId().getValue()));
	}

	@Override
	public DocumentFormData refreshLinkedRoles(final DocumentFormData formData) {
		this.loadLinkedRoles(formData);
		return formData;
	}

	@Override
	public DocumentFormData store(final DocumentFormData formData) {
		super.checkPermission(new UpdateDocumentPermission());
		return this.store(formData, Boolean.FALSE);
	}

	private DocumentFormData store(final DocumentFormData formData, final Boolean duringCreate) {
		super.checkPermission(new UpdateDocumentPermission());
		formData.getLastModificationDate().setValue(new Date());
		formData.setContentData(this.getContentData(formData.getContent().getValue()));

		SQL.update(SQLs.DOCUMENT_UPDATE, formData);

		if (!duringCreate) {
			this.sendModifiedNotifications(formData);
		}
		return formData;
	}

	private byte[] getContentData(final String content) {
		byte[] contentData;

		try {
			contentData = content.getBytes("UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new VetoException("Content not valid (encoding)");
		}

		return contentData;
	}

	private String getContent(final byte[] dbStredContent) {
		String content = null;

		if (null != dbStredContent) {
			try {
				content = new String(dbStredContent, "UTF-8");
			} catch (final UnsupportedEncodingException e) {
				throw new VetoException("Content not valid (encoding)");
			}
		}
		return content;
	}

	private void sendModifiedNotifications(final DocumentFormData formData) {
		final Set<String> notifiedUsers = this.buildNotifiedUsers(formData);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers,
				new DocumentModifiedNotification(formData));
	}

	private Set<String> buildNotifiedUsers(final DocumentFormData formData) {
		// Notify Users for EventTable update
		final AccessControlService acs = BEANS.get(AccessControlService.class);

		final Set<String> notifiedUsers = new HashSet<>();
		notifiedUsers.addAll(acs.getUserNotificationIds(acs.getZeroClickUserIdOfCurrentSubject()));
		return notifiedUsers;
	}
}
