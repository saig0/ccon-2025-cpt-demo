package io.camunda.demo;

import io.camunda.client.CamundaClient;
import io.camunda.demo.model.Account;
import io.camunda.demo.model.SignUpForm;
import io.camunda.demo.services.AccountService;
import io.camunda.demo.services.BackendService;
import io.camunda.demo.services.SubscriptionService;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@CamundaSpringProcessTest
class DemoProcessTests {

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

  // Sign-up process (id: "sign-up")
  // |- New sign-up (id: "new-sign-up")
  // |--- Create account (id: "create-account")
  // |--- Send activation email (id: "send-activation-email")
  // |--- Send confirmation (id: "send-confirmation")
  // |--- Await email activation (id: "await-email-activation")
  // |--- (message) Email confirmed (id: "message-email-confirmed")
  // |--- Activate account (id: "activate-account")
  // |--- Subscribe to newsletter (id: "subscribe-to-newsletter")
  // |--- Account created (id: "account-created")
  // |- (timer) 3 days (id: "timer-three-days")
  // |--- Delete account (id: "delete-account")
  // |--- Account deleted (id: "account-deleted")
  // |- (error) Invalid (id: "error-invalid-account")
  // |--- Send rejection (id: "send-rejection")
  // |--- Sign-up rejected (id: "sign-up-rejected")

  @BeforeEach
  void configureMocks() {
    // mocking
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

    // when
    // then
  }

  @DisplayName("Should reject sign-up if email address is already in use")
  @Test
  void shouldRejectSignUp() {
    // given
    final var signUpForm = new SignUpForm(USER_NAME, EMAIL, true);
    final String rejectionReason = "Email address is already used by a different account";

    // when
    // then
  }
}
