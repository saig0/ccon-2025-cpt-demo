package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThat;
import static io.camunda.process.test.api.assertions.ElementSelectors.byName;
import static org.mockito.Mockito.*;

import io.camunda.client.CamundaClient;
import io.camunda.demo.model.Account;
import io.camunda.demo.model.SignUpForm;
import io.camunda.demo.services.AccountService;
import io.camunda.demo.services.BackendService;
import io.camunda.demo.services.SubscriptionService;
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
  @MockitoBean private BackendService backendService;
  @MockitoBean private SubscriptionService subscriptionService;

  @Test
  void shouldCreateAccount() {
    // given
    final var signUpForm = new SignUpForm("Demo", "demo@camunda.com", true);
    final var account =
        new Account(
            "id-1",
            signUpForm.userName(),
            signUpForm.email(),
            signUpForm.subscribeToNewsletter(),
            "code-1");

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
    assertThat(processInstance)
        .isActive()
        .hasCompletedElements(
            byName("New sign-up"),
            byName("Create account"),
            byName("Send activation email"),
            byName("Send confirmation"));

    client
        .newPublishMessageCommand()
        .messageName("backend:email-confirmed")
        .correlationKey(account.id())
        .send()
        .join();

    // then
    assertThat(processInstance)
        .isCompleted()
        .hasCompletedElements(
            byName("Email confirmed"),
            byName("Activate account"),
            byName("Subscribe to newsletter"),
            byName("Account created"));

    // verify mock invocations
    verify(accountService).createAccount(signUpForm);
    verify(backendService).confirmAccount(account);
    verify(accountService).activateAccount(account);
    verify(subscriptionService).subscribeAccount(account);
  }

  @Test
  void shouldCreateAccountNoSubscription() {
    // given
    final var signUpForm = new SignUpForm("Demo", "demo@camunda.com", false);
    final var account =
        new Account(
            "id-1",
            signUpForm.userName(),
            signUpForm.email(),
            signUpForm.subscribeToNewsletter(),
            "code-1");

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
    assertThat(processInstance).isActive().hasCompletedElements(byName("Send confirmation"));

    client
        .newPublishMessageCommand()
        .messageName("backend:email-confirmed")
        .correlationKey(account.id())
        .send()
        .join();

    // then
    assertThat(processInstance)
        .isCompleted()
        .hasCompletedElements(byName("Account created"))
        .hasNotActivatedElements(byName("Subscribe to newsletter"));

    // verify mock invocations
    verify(subscriptionService, never()).subscribeAccount(account);
  }

  private void mockJobWorker(final String jobType) {
    client
        .newWorker()
        .jobType(jobType)
        .handler((jobClient, job) -> jobClient.newCompleteCommand(job.getKey()).send())
        .open();
  }
}
