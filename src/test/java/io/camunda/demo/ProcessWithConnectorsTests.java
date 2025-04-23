package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThat;
import static io.camunda.process.test.api.assertions.ElementSelectors.byName;
import static org.mockito.Mockito.when;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.demo.model.Account;
import io.camunda.demo.model.SignUpForm;
import io.camunda.demo.services.AccountService;
import io.camunda.demo.services.BackendService;
import io.camunda.demo.services.SubscriptionService;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {"io.camunda.process.test.connectors-enabled=true"})
@TestPropertySource(locations = {"/connector-secrets.yml"})
@CamundaSpringProcessTest
class ProcessWithConnectorsTests {

  private static final String PROCESS_ID = "sign-up";

  private static final String USER_NAME = "Demo";
  private static final String EMAIL = "demo@camunda.com";
  private static final String ACCOUNT_ID = "account-id-0001";
  private static final String ACTIVATION_CODE = "activation-code-0001";

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  @MockitoBean private AccountService accountService;
  @MockitoBean private BackendService backendService;
  @MockitoBean private SubscriptionService subscriptionService;

  @DisplayName("Should send activation email")
  @Test
  void shouldSendActivationEmail() {
    // given
    final var signUpForm = new SignUpForm(USER_NAME, EMAIL, true);
    final var account = new Account(ACCOUNT_ID, USER_NAME, EMAIL, true, ACTIVATION_CODE);

    when(accountService.createAccount(signUpForm)).thenReturn(account);

    // when
    final var processInstance = createProcessInstance(signUpForm);

    // then
    assertThat(processInstance)
        .isActive()
        .hasCompletedElements(byName("Create account"), byName("Send activation email"));
  }

  private ProcessInstanceEvent createProcessInstance(final SignUpForm signUpForm) {
    return client
        .newCreateInstanceCommand()
        .bpmnProcessId(PROCESS_ID)
        .latestVersion()
        .variable("signUpForm", signUpForm)
        .send()
        .join();
  }
}
