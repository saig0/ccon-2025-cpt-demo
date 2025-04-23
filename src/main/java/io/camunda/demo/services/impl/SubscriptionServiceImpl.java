package io.camunda.demo.services.impl;

import io.camunda.demo.model.Account;
import io.camunda.demo.services.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

  private static final Logger LOG = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

  @Override
  public void subscribeAccount(final Account account) {
    LOG.info("Account subscribed. [id:{}]", account.id());
  }
}
