package org.zeroclick.meeting.client.calendar;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.zeroclick.configuration.shared.api.ApiTablePageData;
import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;
import org.zeroclick.configuration.shared.provider.ProviderCodeType;
import org.zeroclick.meeting.shared.calendar.IApiService;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AddCalendarFormTest {

	@BeanMock
	private IApiService mockSvc;

	@Before
	public void setUp() {

		final ApiTablePageData answer = new ApiTablePageData();

		final ApiTableRowData firstApi = answer.addRow();
		firstApi.setUserId(1L);
		firstApi.setAccessToken("GoogleUnitTestAccesToken");
		firstApi.setAccountEmail("UnitTest@gmail42.org");
		firstApi.setApiCredentialId(1L);
		firstApi.setExpirationTimeMilliseconds(100L);
		firstApi.setProvider(ProviderCodeType.GoogleCode.ID);
		firstApi.setRefreshToken("UnitTestRefreshToken");
		firstApi.setTenantId(null);// No tenant ID for Google APIs

		final ApiTableRowData secondApi = answer.addRow();
		secondApi.setUserId(1L);
		secondApi.setAccessToken("MicrosoftUnitTestAccesToken");
		secondApi.setAccountEmail("UnitTest@hotmail42.org");
		secondApi.setApiCredentialId(2L);
		secondApi.setExpirationTimeMilliseconds(100L);
		secondApi.setProvider(ProviderCodeType.MicrosoftCode.ID);
		secondApi.setRefreshToken("UnitTestRefreshToken");
		secondApi.setTenantId("AUnitTestTennantId");

		Mockito.when(this.mockSvc.getApiTableData(Matchers.any(Boolean.class))).thenReturn(answer);
	}

	@Test
	public void testNew() {
		final AddCalendarForm form = new AddCalendarForm();

		form.startNew();

		form.doSave();
		form.doClose();
	}
}
