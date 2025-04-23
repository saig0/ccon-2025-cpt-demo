package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThat;
import static io.camunda.process.test.api.assertions.ElementSelectors.byName;
import static org.mockito.Mockito.when;

import io.camunda.client.CamundaClient;
import io.camunda.demo.model.Account;
import io.camunda.demo.model.SignUpForm;
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
    final var signUpForm = new SignUpForm("Demo", "demo@camunda.com", true);
    final var account = new Account("id-1", "Demo", "demo@camunda.com", true, "code-1");

    when(accountService.createAccount(signUpForm)).thenReturn(account);

    mockJobWorker("io.camunda:sendgrid:1");

    final var processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId("sign-up")
            .latestVersion()
            .variable("signUpForm", signUpForm)
            .send()
            .join();

    // when

    // then
    assertThat(processInstance)
        .isActive()
        .hasCompletedElements(byName("Create account"), byName("Send activation email"))
        .hasActiveElements(byName("Send confirmation"))
        .hasVariable("account", account);
  }

  private void mockJobWorker(final String jobType) {
    client
        .newWorker()
        .jobType(jobType)
        .handler((jobClient, job) -> jobClient.newCompleteCommand(job.getKey()).send()).open();
  }
}
