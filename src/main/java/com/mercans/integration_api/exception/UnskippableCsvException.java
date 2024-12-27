package com.mercans.integration_api.exception;

public class UnskippableCsvException extends RuntimeException {
  public UnskippableCsvException(String message) {
    super(message);
  }
}
