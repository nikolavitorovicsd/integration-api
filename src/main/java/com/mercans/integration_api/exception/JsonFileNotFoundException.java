package com.mercans.integration_api.exception;

public class JsonFileNotFoundException extends RuntimeException {

  public JsonFileNotFoundException(String message) {
    super(message);
  }
}
