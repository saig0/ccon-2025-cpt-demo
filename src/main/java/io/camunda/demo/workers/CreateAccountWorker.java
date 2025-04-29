package io.camunda.demo.workers;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.demo.model.Account;
import io.camunda.demo.model.AccountServiceException;
import io.camunda.demo.model.SignUpForm;
import io.camunda.demo.services.AccountService;
import io.camunda.spring.client.annotation.JobWorker;
import io.camunda.spring.client.annotation.Variable;
import io.camunda.spring.client.exception.BpmnError;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CreateAccountWorker {

  private final AccountService accountService;

  public CreateAccountWorker(final AccountService accountService) {
    this.accountService = accountService;
  }

  @JobWorker(type = "accounts:create")
  public Map<String, Object> handleJob(
      final ActivatedJob job, @Variable("signUpForm") final SignUpForm signUpForm) {

    try {
      final Account account = accountService.createAccount(signUpForm);
      return Map.of("account", account);

    } catch (final AccountServiceException failure) {
      throw new BpmnError(
          "invalid",
          failure.getMessage(),
          Map.of("rejectionReason", failure.getMessage()),
          failure);
    }
  }
}
