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
import org.zeroclick.configuration.client.api.ApiForm;
import org.zeroclick.configuration.shared.provider.ProviderCodeType;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.IApiService;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ApiFormTest {

	@BeanMock
	private IApiService mockSvc;

	@Before
	public void setUp() {
		final ApiFormData answer = new ApiFormData();

		answer.setApiCredentialId(1L);
		answer.setProviderData("SomeProviderData".getBytes());
		answer.setRepositoryId("ARepoId");
		answer.setUserId(1L);
		answer.getAccessToken().setValue("AnAccesToken");
		answer.getAccountEmail().setValue("UnitTest@gmail42.org");
		answer.getExpirationTimeMilliseconds().setValue(1000L);
		answer.getProvider().setValue(ProviderCodeType.GoogleCode.ID);
		answer.getRefreshToken().setValue("ARefreshTOken");
		answer.getTenantId().setValue(null);// No tenantId for Google APIs

		Mockito.when(this.mockSvc.prepareCreate(Matchers.any(ApiFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.create(Matchers.any(ApiFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.load(Matchers.any(ApiFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.store(Matchers.any(ApiFormData.class))).thenReturn(answer);
	}

	@Test
	public void testStartNew() {
		final ApiForm form = new ApiForm();

		form.startNew();

		form.setProviderData(null);
		form.setRepositoryId(null);
		form.setUserId(1L);
		form.getaccessTokenField().setValue("ANewMicrosoftAccesToken");
		form.getaccessTokenField().setValue("UnitTest@hotmail42.org");
		form.getExpirationTimeMillisecondsField().setValue(3000L);
		form.getProviderField().setValue(ProviderCodeType.MicrosoftCode.ID);
		form.getRefreshTokenField().setValue("ANewMicrosoftRefreshToken");
		form.getTenantIdField().setValue("AMicrosoftTenamtId");

		form.doSave();
		form.doClose();
	}

	@Test
	public void testStartModify() {
		final ApiForm form = new ApiForm();

		form.setApiCredentialId(1L);

		form.startModify();

		form.getAccountEmailField().setValue("UnitTest2@gmail42.org");

		form.doSave();
		form.doClose();
	}

	@Test
	public void testStartDelete() {
		final ApiForm form = new ApiForm();

		form.setApiCredentialId(1L);

		form.startDelete();

		form.doSave();
		form.doClose();
	}
}
