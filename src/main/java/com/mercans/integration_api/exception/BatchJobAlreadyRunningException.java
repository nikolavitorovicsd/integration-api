package com.mercans.integration_api.exception;

public class BatchJobAlreadyRunningException extends RuntimeException {
  public BatchJobAlreadyRunningException(String message) {
    super(message);
  }
}
