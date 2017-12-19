package org.zeroclick.configuration.server.role;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.AbstractCommonService;
import org.zeroclick.common.document.DocumentFormData;
import org.zeroclick.common.document.DocumentFormData.LinkedRole.LinkedRoleRowData;
import org.zeroclick.common.document.link.AssignDocumentToRoleFormData;
import org.zeroclick.common.shared.document.IDocumentService;
import org.zeroclick.common.shared.document.ReadDocumentPermission;
import org.zeroclick.common.shared.document.UpdateDocumentPermission;
import org.zeroclick.configuration.shared.role.CreateRolePermission;
import org.zeroclick.configuration.shared.role.IRolePermissionService;
import org.zeroclick.configuration.shared.role.IRoleService;
import org.zeroclick.configuration.shared.role.ReadRolePermission;
import org.zeroclick.configuration.shared.role.RoleFormData;
import org.zeroclick.configuration.shared.role.RoleTablePageData;
import org.zeroclick.configuration.shared.role.UpdateAssignToRolePermission;
import org.zeroclick.configuration.shared.role.UpdateRolePermission;
import org.zeroclick.meeting.server.sql.DatabaseHelper;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.data.PatchCreateSubscription;
import org.zeroclick.meeting.server.sql.migrate.data.PatchOptimizeConfigSlot;

public class RoleService extends AbstractCommonService implements IRoleService {

	private static final Logger LOG = LoggerFactory.getLogger(RoleService.class);

	@Override
	protected Logger getLog() {
		return LOG;
	}

	@Override
	public RoleTablePageData getRoleTableData(final SearchFilter filter) {
		super.checkPermission(new ReadRolePermission());
		final RoleTablePageData pageData = new RoleTablePageData();

		final String sql = SQLs.ROLE_PAGE_SELECT + SQLs.ROLE_PAGE_DATA_SELECT_INTO;
		SQL.selectInto(sql, new NVPair("role", pageData));
		return pageData;
	}

	@Override
	public RoleFormData prepareCreate(final RoleFormData formData) {
		return this.store(formData);
	}

	@Override
	public RoleFormData create(final RoleFormData formData) {
		super.checkPermission(new CreateRolePermission());
		// add a unique Role id if necessary
		if (null == formData.getRoleId()) {
			formData.setRoleId(DatabaseHelper.get().getNextVal(PatchOptimizeConfigSlot.ROLE_ID_SEQ));
		}
		SQL.insert(SQLs.ROLE_INSERT, formData);
		return this.store(formData);
	}

	@Override
	public RoleFormData store(final RoleFormData formData) {
		super.checkPermission(new UpdateRolePermission());
		LOG.info("Updating Role with : " + formData.getRoleId() + "(new : " + formData.getRoleName() + ")");
		SQL.update(SQLs.ROLE_UPDATE, formData);
		return formData;
	}

	@Override
	public RoleFormData load(final RoleFormData formData) {
		super.checkPermission(new ReadRolePermission());
		if (null == formData.getRoleId()) {
			this.loadByRoleName(formData);
		} else {
			if (this.isAfterCreateSubcriptionPatch()) {
				SQL.selectInto(SQLs.ROLE_SELECT + SQLs.ROLE_SELECT_INTO, formData);
			} else {
				SQL.selectInto(SQLs.ROLE_UPDATE_WITHOUT_TYPE + SQLs.ROLE_SELECT_INTO, formData);
			}
		}
		return formData;
	}

	@Override
	public AssignDocumentToRoleFormData create(final AssignDocumentToRoleFormData formData) {
		// To add linked Doc to role must be allowed to modify Role
		super.checkPermission(new UpdateRolePermission());
		if (null == formData.getRoleId().getValue()) {
			throw new VetoException("Role ID required");
		}
		if (null == formData.getDocumentId().getValue()) {
			throw new VetoException("Document ID required");
		}

		SQL.insert(SQLs.ROLE_INSERT_NEW_LINKED_DOC, formData);
		// No "store" because this table contains ONLY a PK
		return formData;
	}

	@Override
	public void delete(final RoleFormData formData) {
		super.checkPermission(new UpdateRolePermission());
		LOG.info("Deleting Role (and link to documents, and link to Users and link to permissions) for Role ID : "
				+ formData.getRoleId());

		if (null == formData.getRoleId()) {
			this.loadByRoleName(formData);
			if (null == formData.getRoleId()) {
				throw new VetoException("Role Id (or valid roleName) required");
			}
		}

		final AssignDocumentToRoleFormData assignDocFormData = new AssignDocumentToRoleFormData();
		assignDocFormData.getRoleId().setValue(formData.getRoleId());
		this.deleteAssignDocumentByRole(assignDocFormData);

		this.deleteSubscriptionMetaDataByRole(formData);
		this.deleteUserRoleByRole(formData);
		this.deletePermissionsByRole(formData.getRoleId());

		SQL.insert(SQLs.ROLE_DELETE, formData);

	}

	private Long loadByRoleName(final RoleFormData formData) {
		super.checkPermission(new ReadRolePermission());
		LOG.info("Loading Role by Name : " + formData.getRoleName().getValue());

		if (this.isAfterCreateSubcriptionPatch()) {
			SQL.selectInto(SQLs.ROLE_SELECT_BY_NAME + SQLs.ROLE_SELECT_INTO, formData);
		} else {
			SQL.selectInto(SQLs.ROLE_SELECT_BY_NAME_WITHOUT_TYPE + SQLs.ROLE_SELECT_INTO_WITHOUT_TYPE, formData);
		}
		return formData.getRoleId();
	}

	private void deleteUserRoleByRole(final RoleFormData formData) {
		super.checkPermission(new UpdateAssignToRolePermission());
		LOG.info("Deleting all link between users and role for roleId : " + formData.getRoleId());
		if (null == formData.getRoleId()) {
			throw new VetoException("role ID required");
		}

		SQL.insert(SQLs.USER_ROLE_REMOVE_BY_ROLE, formData);
	}

	private void deleteAssignDocumentByRole(final AssignDocumentToRoleFormData formData) {
		// To add linked Doc to role must be allowed to modify Role
		super.checkPermission(new UpdateRolePermission());

		if (null == formData.getRoleId().getValue()) {
			throw new VetoException("Role ID required");
		}
		if (DatabaseHelper.get().existTable(PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME)) {
			SQL.insert(SQLs.ROLE_DELETE_LINKED_DOC_BY_ROLE, formData);
		}
	}

	private void deletePermissionsByRole(final Long roleId) {
		final IRolePermissionService rolePermissionService = BEANS.get(IRolePermissionService.class);
		rolePermissionService.remove(roleId);
	}

	@Override
	public void delete(final AssignDocumentToRoleFormData formData) {
		// To add linked Doc to role must be allowed to modify Role
		super.checkPermission(new UpdateRolePermission());

		if (null == formData.getRoleId().getValue()) {
			throw new VetoException("Role ID required");
		}
		if (null == formData.getDocumentId().getValue()) {
			throw new VetoException("Document ID required");
		}

		LOG.info("Deleting link between user id " + formData.getRoleId().getValue() + " and role id "
				+ formData.getDocumentId().getValue() + " for roleId : " + formData.getRoleId());

		SQL.insert(SQLs.ROLE_DELETE_LINKED_DOC, formData);
	}

	@Override
	public AssignDocumentToRoleFormData store(final AssignDocumentToRoleFormData formData) {
		super.checkPermission(new UpdateAssignToRolePermission());
		SQL.update(SQLs.ROLE_UPDATE_LINKED_DOC, formData);
		return formData;
	}

	@Override
	public DocumentFormData getActiveDocument(final Long roleId) {
		super.checkPermission(new ReadDocumentPermission());
		final IDocumentService documentService = BEANS.get(IDocumentService.class);

		final DocumentFormData formData = new DocumentFormData();

		final Object[][] docIds = SQL.select(SQLs.ROLE_DOCUMENT_SELECT_ACTIVE_DOCUMENT, new NVPair("roleId", roleId));
		if (null != docIds && docIds.length > 0) {
			final Long docId = (Long) docIds[0][0];
			formData.getDocumentId().setValue(docId);
			documentService.load(formData);
		}
		return formData;
	}

	@Override
	public LinkedRoleRowData getDocumentMetaData(final Long roleId, final Long documentId) {
		super.checkPermission(new ReadDocumentPermission());

		final LinkedRoleRowData documentRoleData = new DocumentFormData.LinkedRole.LinkedRoleRowData();
		documentRoleData.setRoleId(roleId);
		documentRoleData.setDocumentId(documentId);

		SQL.selectInto(SQLs.ROLE_DOCUMENT_SELECT_ROLE_DOCUMENT + SQLs.ROLE_DOCUMENT_SELECT_ROLE_DOCUMENT_INTO,
				documentRoleData);
		return documentRoleData;
	}

	private void deleteSubscriptionMetaDataByRole(final RoleFormData roleFormData) {
		super.checkPermission(new UpdateDocumentPermission());

		if (null == roleFormData.getRoleId()) {
			throw new VetoException("Role ID required");
		}

		LOG.info("Deleting subscription metadata for any users with role : " + roleFormData.getRoleId());
		if (DatabaseHelper.get().existTable(PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME)) {
			SQL.insert(SQLs.SUBSCRIPTION_DELETE_BY_ROLE, roleFormData);
		}
	}

	@Override
	public void deleteSubscriptionMetaDataByUser(final Long userId) {
		super.checkPermission(new UpdateDocumentPermission());

		if (null == userId) {
			throw new VetoException("User ID required");
		}
		LOG.info("Deleting subscription metadata for any role for user : " + userId);
		if (DatabaseHelper.get().existTable(PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME)) {
			SQL.insert(SQLs.SUBSCRIPTION_DELETE_BY_USER_ID, new NVPair("userId", userId));
		}
	}

	private Boolean isAfterCreateSubcriptionPatch() {
		return DatabaseHelper.get().isColumnExists(PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME,
				PatchCreateSubscription.ADDED_ROLE_COLUMN);
	}
}
