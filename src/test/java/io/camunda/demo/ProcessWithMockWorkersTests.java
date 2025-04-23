package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThat;
import static io.camunda.process.test.api.assertions.ElementSelectors.byName;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.demo.model.Account;
import io.camunda.demo.model.SignUpForm;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
      // disable all job workers
      "camunda.client.worker.defaults.enabled=false"
    })
@CamundaSpringProcessTest
class ProcessWithMockWorkersTests {

  private static final String PROCESS_ID = "sign-up";

  private static final String USER_NAME = "Demo";
  private static final String EMAIL = "demo@camunda.com";
  private static final String ACCOUNT_ID = "account-id-0001";
  private static final String ACTIVATION_CODE = "activation-code-0001";

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  @DisplayName("Should create account and subscribe to newsletter")
  @Test
  void shouldCreateAccountWithSubscription() {
    // given
    final var signUpForm = new SignUpForm(USER_NAME, EMAIL, true);
    final var account = new Account(ACCOUNT_ID, USER_NAME, EMAIL, true, ACTIVATION_CODE);

    mockJobWorker("accounts:create", Map.of("account", account));
    mockJobWorker("io.camunda:sendgrid:1");
    mockJobWorker("backend:confirm-account");
    mockJobWorker("accounts:activate");
    mockJobWorker("subscriptions:subscribe");

    final var processInstance = createProcessInstance(signUpForm);

    // when
    assertThat(processInstance)
        .isActive()
        .hasCompletedElements(byName("Create account"))
        .hasActiveElements(byName("Await email activation"));

    client
        .newPublishMessageCommand()
        .messageName("backend:email-confirmed")
        .correlationKey(account.id())
        .send()
        .join();

    // then
    assertThat(processInstance)
        .isCompleted()
        .hasCompletedElementsInOrder(
            byName("New sign-up"),
            byName("Create account"),
            byName("Send activation email"),
            byName("Send confirmation"),
            byName("Email confirmed"),
            byName("Activate account"),
            byName("Subscribe to newsletter"),
            byName("Account created"));
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

  private void mockJobWorker(final String jobType) {
    mockJobWorker(jobType, Collections.emptyMap());
  }

  private void mockJobWorker(final String jobType, final Map<String, Object> variables) {
    client
        .newWorker()
        .jobType(jobType)
        .handler(
            (jobClient, job) ->
                jobClient.newCompleteCommand(job.getKey()).variables(variables).send())
        .open();
  }
}
