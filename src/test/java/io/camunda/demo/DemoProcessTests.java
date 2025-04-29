package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThat;
import static io.camunda.process.test.api.assertions.ElementSelectors.byName;
import static org.mockito.Mockito.verify;
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
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@CamundaSpringProcessTest
class DemoProcessTests {

  private static final String USER_NAME = "Demo";
  private static final String EMAIL = "demo@camunda.com";
  private static final String ACCOUNT_ID = "account-accountId-0001";
  private static final String ACTIVATION_CODE = "activation-code-0001";

  private static final String PROCESS_ID = "sign-up";
  private static final String VARIABLE_NAME_SIGN_UP_FORM = "signUpForm";
  private static final String JOB_TYPE_SENDGRID = "io.camunda:sendgrid:1";
  private static final String MESSAGE_NAME_EMAIL_CONFIRMED = "backend:email-confirmed";

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  @MockitoBean private AccountService accountService;
  @MockitoBean private BackendService backendService;
  @MockitoBean private SubscriptionService subscriptionService;

  // Sign-up process (accountId: "sign-up")
  // |- New sign-up (accountId: "new-sign-up")
  // |--- Create account (accountId: "create-account")
  // |--- Send activation email (accountId: "send-activation-email")
  // |--- Send confirmation (accountId: "send-confirmation")
  // |--- Await email activation (accountId: "await-email-activation")
  // |--- (message) Email confirmed (accountId: "message-email-confirmed")
  // |--- Activate account (accountId: "activate-account")
  // |--- Subscribe to newsletter (accountId: "subscribe-to-newsletter")
  // |--- Account created (accountId: "account-created")
  // |- (timer) 3 days (accountId: "timer-three-days")
  // |--- Delete account (accountId: "delete-account")
  // |--- Account deleted (accountId: "account-deleted")
  // |- (error) Invalid (accountId: "error-invalid-account")
  // |--- Send rejection (accountId: "send-rejection")
  // |--- Sign-up rejected (accountId: "sign-up-rejected")

  @BeforeEach
  void configureMocks() {
    // mock services and job workers
  }

  @DisplayName("Should create account and subscribe to newsletter")
  @Test
  void shouldCreateAccountWithSubscription() {
    // given
    final var signUpForm = new SignUpForm(USER_NAME, EMAIL, true);
    final var account = new Account(ACCOUNT_ID, USER_NAME, EMAIL, true, ACTIVATION_CODE);

    // when

    // then

  }

  @DisplayName("Should delete account if no confirmation is received")
  @Test
  void shouldDeleteAccountNoConfirmation() {
    // given
    final var signUpForm = new SignUpForm(USER_NAME, EMAIL, true);
    final var account = new Account(ACCOUNT_ID, USER_NAME, EMAIL, true, ACTIVATION_CODE);

    when(accountService.createAccount(signUpForm)).thenReturn(account);

    final ProcessInstanceEvent processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId(PROCESS_ID)
            .latestVersion()
            .variable(VARIABLE_NAME_SIGN_UP_FORM, signUpForm)
            .send()
            .join();

    // when
    assertThat(processInstance).isActive().hasActiveElements(byName("Await email activation"));

    processTestContext.increaseTime(Duration.ofDays(3));

    // then
    assertThat(processInstance)
        .isCompleted()
        .hasCompletedElementsInOrder(
            byName("3 days"), byName("Delete account"), byName("Account deleted"));

    verify(accountService).deleteAccount(account);
  }
}
