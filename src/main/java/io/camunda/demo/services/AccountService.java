package io.camunda.demo.services;

import io.camunda.demo.model.Account;
import io.camunda.demo.model.AccountServiceException;
import io.camunda.demo.model.SignUpForm;

public interface AccountService {

  Account createAccount(SignUpForm signUpForm) throws AccountServiceException;

  void activateAccount(Account account);

  void deleteAccount(Account account);
}
