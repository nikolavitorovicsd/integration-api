package com.mercans.integration_api.exception.handlers;

import com.mercans.integration_api.exception.JsonFileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

  @Value("${spring.servlet.multipart.max-file-size}")
  private String maxFileSize;

  @ExceptionHandler(JsonFileNotFoundException.class)
  public ResponseEntity<String> handleJsonFileNotFoundException(
      JsonFileNotFoundException exception) {
    log.warn(exception.getMessage());
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<String> handleMaxUploadSizeExceededException(
      MaxUploadSizeExceededException exception) {
    String message =
        String.format("Maximum upload size exceeded, allowed size is '%s'.", maxFileSize);
    log.warn(message);
    return new ResponseEntity<>(message, HttpStatus.NOT_ACCEPTABLE);
  }
}
