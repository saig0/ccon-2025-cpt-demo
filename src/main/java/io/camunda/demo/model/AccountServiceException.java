package io.camunda.demo.model;

public class AccountServiceException extends RuntimeException {
  public AccountServiceException(final String message) {
    super(message);
  }
}
