package io.camunda.demo.workers;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.demo.model.Account;
import io.camunda.demo.model.SignUpForm;
import io.camunda.demo.services.AccountService;
import io.camunda.demo.services.BackendService;
import io.camunda.spring.client.annotation.JobWorker;
import io.camunda.spring.client.annotation.Variable;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SendConfirmationWorker {

    private final BackendService backendService;

    public SendConfirmationWorker(BackendService backendService) {
        this.backendService = backendService;
    }

    @JobWorker(type = "backend:confirm-account")
    public void handleJob(final ActivatedJob job, @Variable("account") Account account) {
        backendService.confirmAccount(account);
    }
}
