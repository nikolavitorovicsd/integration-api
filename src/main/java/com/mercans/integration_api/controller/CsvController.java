package com.mercans.integration_api.controller;

import com.mercans.integration_api.model.JsonResponse;
import com.mercans.integration_api.service.CsvReadService;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/csv")
@RequiredArgsConstructor
@Slf4j
public class CsvController {

  private final CsvReadService csvReadService;
  private final Semaphore batchSemaphore;

  @PostMapping(value = {"/upload", "/upload/"})
  public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file)
      throws JobInstanceAlreadyCompleteException,
          JobExecutionAlreadyRunningException,
          JobParametersInvalidException,
          JobRestartException {

    if (file.isEmpty()) {
      return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
    }

    UUID jsonId;
    if (batchSemaphore.tryAcquire()) {
      try {
        jsonId = csvReadService.saveCsvData(file);
      } catch (Exception exception) {
        batchSemaphore.release();
        log.error("Could not run the job, reason: {}", exception.getMessage());
        throw exception;
      }

    } else {
      log.warn("Cant run job because one is already running!");
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    return new ResponseEntity<>(
        String.format("Json response with id: '%s' saved.", jsonId), HttpStatus.OK);
  }

  @GetMapping(
      value = {"/{json_id}", "/{json_id}/"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<JsonResponse> getJsonResponseFromDb(@PathVariable("json_id") UUID jsonID)
      throws IOException {
    var response = csvReadService.getJsonResponseFromDb(jsonID);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
