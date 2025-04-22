package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThat;
import static io.camunda.process.test.api.assertions.ElementSelectors.byName;
import static org.mockito.Mockito.when;

import io.camunda.client.CamundaClient;
import io.camunda.demo.model.Account;
import io.camunda.demo.model.UserSignUp;
import io.camunda.demo.services.AccountService;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@CamundaSpringProcessTest
class ProcessIntegrationTests {

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  @MockitoBean private AccountService accountService;

  @Test
  void happyPath() {
    // given
    final var signUp = new UserSignUp( "Demo", "demo@camunda.com", true);
    final Account account = new Account("id-1", "Demo", "demo@camunda.com", true, "code-1");

    when(accountService.createAccount(signUp)).thenReturn(account);

    final var processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId("sign-up")
            .latestVersion()
            .variable("signUp", signUp)
            .send()
            .join();

    // when

    // then
    assertThat(processInstance).isActive()
            .hasCompletedElements(byName("Create account"))
            .hasActiveElements(byName("Send activation email"));
  }
}
