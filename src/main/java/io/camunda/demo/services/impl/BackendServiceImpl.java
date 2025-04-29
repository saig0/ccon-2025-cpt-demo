package io.camunda.demo.services.impl;

import io.camunda.demo.model.Account;
import io.camunda.demo.model.SignUpForm;
import io.camunda.demo.services.BackendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BackendServiceImpl implements BackendService {

  private static final Logger LOG = LoggerFactory.getLogger(BackendServiceImpl.class);

  @Override
  public void confirmAccount(final Account account) {
    LOG.info("Account confirmed. [id: {}]", account.id());
  }

  @Override
  public void sendRejection(final SignUpForm signUpForm, final String rejectionReason) {
    LOG.info(
        "Sign-up rejected. [userName: {}, rejectionReason: {}]",
        signUpForm.userName(),
        rejectionReason);
  }
}
