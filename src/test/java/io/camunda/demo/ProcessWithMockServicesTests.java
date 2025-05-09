package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThat;
import static io.camunda.process.test.api.assertions.ElementSelectors.byName;
import static org.mockito.Mockito.*;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.demo.model.Account;
import io.camunda.demo.model.AccountServiceException;
import io.camunda.demo.model.SignUpForm;
import io.camunda.demo.services.AccountService;
import io.camunda.demo.services.BackendService;
import io.camunda.demo.services.SubscriptionService;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@CamundaSpringProcessTest
class ProcessWithMockServicesTests {

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

  @BeforeEach
  void configureMocks() {
    processTestContext.mockJobWorker("io.camunda:sendgrid:1").thenComplete();
  }

  @DisplayName("Should create account and subscribe to newsletter")
  @Test
  void shouldCreateAccountWithSubscription() {
    // given
    final var signUpForm = new SignUpForm(USER_NAME, EMAIL, true);
    final var account = new Account(ACCOUNT_ID, USER_NAME, EMAIL, true, ACTIVATION_CODE);

    when(accountService.createAccount(signUpForm)).thenReturn(account);

    final var processInstance = createProcessInstance(signUpForm);

    // when
    assertThat(processInstance)
        .isActive()
        .hasCompletedElements(byName("Create account"))
        .hasVariable("account", account)
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

    // verify mock invocations
    verify(accountService).createAccount(signUpForm);
    verify(backendService).confirmAccount(account);
    verify(accountService).activateAccount(account);
    verify(subscriptionService).subscribeAccount(account);
  }

  @DisplayName("Should create account without subscribing to newsletter")
  @Test
  void shouldCreateAccountWithoutSubscription() {
    // given
    final var signUpForm = new SignUpForm(USER_NAME, EMAIL, false);
    final var account = new Account(ACCOUNT_ID, USER_NAME, EMAIL, false, ACTIVATION_CODE);

    when(accountService.createAccount(signUpForm)).thenReturn(account);

    final var processInstance = createProcessInstance(signUpForm);

    // when
    assertThat(processInstance).isActive().hasActiveElements(byName("Await email activation"));

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

  @DisplayName("Should delete account if no confirmation is received")
  @Test
  void shouldDeleteAccountNoConfirmation() {
    // given
    final var signUpForm = new SignUpForm(USER_NAME, EMAIL, true);
    final var account = new Account(ACCOUNT_ID, USER_NAME, EMAIL, true, ACTIVATION_CODE);

    when(accountService.createAccount(signUpForm)).thenReturn(account);

    final var processInstance = createProcessInstance(signUpForm);

    // when
    assertThat(processInstance).isActive().hasActiveElements(byName("Await email activation"));

    processTestContext.increaseTime(Duration.ofDays(3));

    // then
    assertThat(processInstance)
        .isCompleted()
        .hasCompletedElementsInOrder(
            byName("3 days"), byName("Delete account"), byName("Account deleted"));

    // verify mock invocations
    verify(accountService).deleteAccount(account);
  }

  @DisplayName("Should reject sign-up if email address is already in use")
  @Test
  void shouldRejectSignUp() {
    // given
    final var signUpForm = new SignUpForm(USER_NAME, EMAIL, true);
    final String rejectionReason = "Email address is already used by a different account";

    doThrow(new AccountServiceException(rejectionReason))
        .when(accountService)
        .createAccount(signUpForm);

    // when
    final var processInstance = createProcessInstance(signUpForm);

    // then
    assertThat(processInstance)
        .isCompleted()
        .hasTerminatedElements(byName("Create account"))
        .hasCompletedElementsInOrder(byName("Send rejection"), byName("Sign-up rejected"));

    // verify mock invocations
    verify(backendService).sendRejection(signUpForm, rejectionReason);
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
