package io.camunda.demo.workers;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.demo.model.Account;
import io.camunda.demo.services.AccountService;
import io.camunda.spring.client.annotation.JobWorker;
import io.camunda.spring.client.annotation.Variable;
import org.springframework.stereotype.Component;

@Component
public class ActivateAccountWorker {

  private final AccountService accountService;

  public ActivateAccountWorker(final AccountService accountService) {
    this.accountService = accountService;
  }

  @JobWorker(type = "accounts:activate")
  public void handleJob(final ActivatedJob job, @Variable("account") final Account account) {
    accountService.activateAccount(account);
  }
}
