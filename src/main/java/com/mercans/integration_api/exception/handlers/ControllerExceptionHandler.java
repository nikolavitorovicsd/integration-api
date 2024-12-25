package com.mercans.integration_api.exception.handlers;

import com.mercans.integration_api.exception.JsonFileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

  @ExceptionHandler(JsonFileNotFoundException.class)
  public ResponseEntity<String> handleJsonFileNotFoundException(
      JsonFileNotFoundException exception) {
    log.warn(exception.getMessage());
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
  }
}
