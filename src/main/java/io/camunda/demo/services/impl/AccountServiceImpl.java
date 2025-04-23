package io.camunda.demo.services.impl;

import io.camunda.demo.model.Account;
import io.camunda.demo.model.AccountServiceException;
import io.camunda.demo.model.SignUpForm;
import io.camunda.demo.services.AccountService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

  @Override
  public Account createAccount(SignUpForm signUpForm) throws AccountServiceException {
    final String accountId = UUID.randomUUID().toString();
    final String activationCode = UUID.randomUUID().toString();
    return new Account(
        accountId,
        signUpForm.userName(),
        signUpForm.email(),
        signUpForm.subscribeToNewsletter(),
        activationCode);
  }
}
