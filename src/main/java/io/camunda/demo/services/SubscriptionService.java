package io.camunda.demo.services;

import io.camunda.demo.model.Account;

public interface SubscriptionService {

  void subscribeAccount(Account account);
}
