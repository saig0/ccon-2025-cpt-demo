package io.camunda.demo.workers;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.demo.model.Account;
import io.camunda.demo.model.SignUpForm;
import io.camunda.demo.services.AccountService;
import io.camunda.spring.client.annotation.JobWorker;
import io.camunda.spring.client.annotation.Variable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CreateAccountWorker {

    private final AccountService accountService;

    public CreateAccountWorker(AccountService accountService) {
        this.accountService = accountService;
    }

    @JobWorker(type = "accounts:create")
    public Map<String, Object> handleJob(final ActivatedJob job, @Variable("signUpForm") SignUpForm signUpForm) {
        final Account account = accountService.createAccount(signUpForm);
        return Map.of("account", account);
    }
}
