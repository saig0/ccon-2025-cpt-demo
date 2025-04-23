package io.camunda.demo.services.impl;

import io.camunda.demo.model.Account;
import io.camunda.demo.model.AccountServiceException;
import io.camunda.demo.model.SignUpForm;
import io.camunda.demo.services.AccountService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

  private static final Logger LOG = LoggerFactory.getLogger(AccountServiceImpl.class);

  @Override
  public Account createAccount(final SignUpForm signUpForm) throws AccountServiceException {
    final String accountId = UUID.randomUUID().toString();
    final String activationCode = UUID.randomUUID().toString();

    LOG.debug("Account created. [id={}]", accountId);

    return new Account(
        accountId,
        signUpForm.userName(),
        signUpForm.email(),
        signUpForm.subscribeToNewsletter(),
        activationCode);
  }

  @Override
  public void activateAccount(final Account account) {
    LOG.debug("Account activated. [id={}]", account.id());
  }

  @Override
  public void deleteAccount(final Account account) {
    LOG.debug("Account deleted. [id={}]", account.id());
  }
}
