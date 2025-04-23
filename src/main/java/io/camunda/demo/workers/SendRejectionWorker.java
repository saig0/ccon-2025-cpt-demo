package io.camunda.demo.workers;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.demo.model.SignUpForm;
import io.camunda.demo.services.BackendService;
import io.camunda.spring.client.annotation.JobWorker;
import io.camunda.spring.client.annotation.Variable;
import org.springframework.stereotype.Component;

@Component
public class SendRejectionWorker {

  private final BackendService backendService;

  public SendRejectionWorker(final BackendService backendService) {
    this.backendService = backendService;
  }

  @JobWorker(type = "backend:reject-sign-up")
  public void handleJob(
      final ActivatedJob job,
      @Variable("signUpForm") final SignUpForm signUpForm,
      @Variable("rejectionReason") final String rejectionReason) {
    backendService.sendRejection(signUpForm, rejectionReason);
  }
}
