package io.camunda.demo.services;

import io.camunda.demo.model.Account;
import io.camunda.demo.model.SignUpForm;

public interface BackendService {

  void confirmAccount(Account account);

  void sendRejection(SignUpForm signUpForm, String rejectionReason);
}
