package org.zeroclick.configuration.shared.api;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications
 * recommended.
 */
@Generated(value = "org.zeroclick.configuration.client.api.ApiTablePage", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class ApiTablePageData extends AbstractTablePageData {

	private static final long serialVersionUID = 1L;

	@Override
	public ApiTableRowData addRow() {
		return (ApiTableRowData) super.addRow();
	}

	@Override
	public ApiTableRowData addRow(int rowState) {
		return (ApiTableRowData) super.addRow(rowState);
	}

	@Override
	public ApiTableRowData createRow() {
		return new ApiTableRowData();
	}

	@Override
	public Class<? extends AbstractTableRowData> getRowType() {
		return ApiTableRowData.class;
	}

	@Override
	public ApiTableRowData[] getRows() {
		return (ApiTableRowData[]) super.getRows();
	}

	@Override
	public ApiTableRowData rowAt(int index) {
		return (ApiTableRowData) super.rowAt(index);
	}

	public void setRows(ApiTableRowData[] rows) {
		super.setRows(rows);
	}

	public static class ApiTableRowData extends AbstractTableRowData {

		private static final long serialVersionUID = 1L;
		public static final String apiCredentialId = "apiCredentialId";
		public static final String provider = "provider";
		public static final String accessToken = "accessToken";
		public static final String expirationTimeMilliseconds = "expirationTimeMilliseconds";
		public static final String refreshToken = "refreshToken";
		public static final String userId = "userId";
		public static final String accountEmail = "accountEmail";
		public static final String tenantId = "tenantId";
		private Long m_apiCredentialId;
		private Long m_provider;
		private String m_accessToken;
		private Long m_expirationTimeMilliseconds;
		private String m_refreshToken;
		private Long m_userId;
		private String m_accountEmail;
		private String m_tenantId;

		public Long getApiCredentialId() {
			return m_apiCredentialId;
		}

		public void setApiCredentialId(Long newApiCredentialId) {
			m_apiCredentialId = newApiCredentialId;
		}

		public Long getProvider() {
			return m_provider;
		}

		public void setProvider(Long newProvider) {
			m_provider = newProvider;
		}

		public String getAccessToken() {
			return m_accessToken;
		}

		public void setAccessToken(String newAccessToken) {
			m_accessToken = newAccessToken;
		}

		public Long getExpirationTimeMilliseconds() {
			return m_expirationTimeMilliseconds;
		}

		public void setExpirationTimeMilliseconds(Long newExpirationTimeMilliseconds) {
			m_expirationTimeMilliseconds = newExpirationTimeMilliseconds;
		}

		public String getRefreshToken() {
			return m_refreshToken;
		}

		public void setRefreshToken(String newRefreshToken) {
			m_refreshToken = newRefreshToken;
		}

		public Long getUserId() {
			return m_userId;
		}

		public void setUserId(Long newUserId) {
			m_userId = newUserId;
		}

		public String getAccountEmail() {
			return m_accountEmail;
		}

		public void setAccountEmail(String newAccountEmail) {
			m_accountEmail = newAccountEmail;
		}

		public String getTenantId() {
			return m_tenantId;
		}

		public void setTenantId(String newTenantId) {
			m_tenantId = newTenantId;
		}
	}
}
