package com.mercans.integration_api.controller;

import com.mercans.integration_api.service.CsvReadService;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
@RequestMapping(path = "/upload")
@RequiredArgsConstructor
public class CsvController {

  private final CsvReadService csvReadService;

  @GetMapping(value = "/hello")
  public ResponseEntity<String> helloThere() {
    return new ResponseEntity<>("Hello there", HttpStatus.OK);
  }

  @PostMapping(value = "/csv")
  public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file)
      throws JobInstanceAlreadyCompleteException,
          JobExecutionAlreadyRunningException,
          JobParametersInvalidException,
          JobRestartException {

    if (file.isEmpty()) {
      return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
    }
    var jsonId = csvReadService.saveCsvData(file);

    return new ResponseEntity<>(
        String.format("Json response with id: '%s' saved.", jsonId), HttpStatus.OK);
  }

  @GetMapping(value = "/csv/{json_id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getJsonResponse(@PathVariable("json_id") UUID jsonID)
      throws IOException {
    var response = csvReadService.getJsonResponse(jsonID);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
