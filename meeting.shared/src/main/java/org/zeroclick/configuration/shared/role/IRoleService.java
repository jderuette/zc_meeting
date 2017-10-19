package org.zeroclick.configuration.shared.role;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.common.document.DocumentFormData;
import org.zeroclick.common.document.DocumentFormData.LinkedRole.LinkedRoleRowData;
import org.zeroclick.common.document.link.AssignDocumentToRoleFormData;

@TunnelToServer
public interface IRoleService extends IService {

	RoleTablePageData getRoleTableData(SearchFilter filter);

	RoleFormData load(RoleFormData formData);

	RoleFormData store(RoleFormData formData);

	RoleFormData prepareCreate(RoleFormData formData);

	RoleFormData create(RoleFormData formData);

	void delete(RoleFormData roleFormData);

	AssignDocumentToRoleFormData create(AssignDocumentToRoleFormData formData);

	AssignDocumentToRoleFormData store(AssignDocumentToRoleFormData formData);

	void delete(AssignDocumentToRoleFormData formData);

	DocumentFormData getActiveDocument(Long roleId);

	LinkedRoleRowData getDocumentMetaData(Long roleId, Long documentId);

}
