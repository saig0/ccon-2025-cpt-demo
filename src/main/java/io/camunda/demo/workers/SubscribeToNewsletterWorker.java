package io.camunda.demo.workers;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.demo.model.Account;
import io.camunda.demo.services.SubscriptionService;
import io.camunda.spring.client.annotation.JobWorker;
import io.camunda.spring.client.annotation.Variable;
import org.springframework.stereotype.Component;

@Component
public class SubscribeToNewsletterWorker {

  private final SubscriptionService subscriptionService;

  public SubscribeToNewsletterWorker(final SubscriptionService subscriptionService) {
    this.subscriptionService = subscriptionService;
  }

  @JobWorker(type = "subscriptions:subscribe")
  public void handleJob(final ActivatedJob job, @Variable("account") final Account account) {
    subscriptionService.subscribeAccount(account);
  }
}
