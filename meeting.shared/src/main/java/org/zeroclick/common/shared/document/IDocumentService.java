package org.zeroclick.common.shared.document;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.common.document.DocumentFormData;
import org.zeroclick.common.document.DocumentTablePageData;

@TunnelToServer
public interface IDocumentService extends IService {

	DocumentTablePageData getDocumentTableData(SearchFilter filter);

	DocumentFormData prepareCreate(DocumentFormData formData);

	DocumentFormData create(DocumentFormData formData);

	DocumentFormData load(DocumentFormData formData);

	DocumentFormData store(DocumentFormData formData);

	DocumentFormData refreshLinkedRoles(DocumentFormData formData);
}
