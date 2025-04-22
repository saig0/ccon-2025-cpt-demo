package io.camunda.demo.services;

import io.camunda.demo.model.Account;
import io.camunda.demo.model.AccountServiceException;
import io.camunda.demo.model.UserSignUp;

public interface AccountService {

    Account createAccount(UserSignUp userSignUp) throws AccountServiceException;

}
