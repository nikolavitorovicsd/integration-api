package com.mercans.integration_api.service;

import static com.mercans.integration_api.constants.GlobalConstants.*;

import com.mercans.integration_api.constants.GlobalConstants;
import com.mercans.integration_api.jpa.JsonResponseEntity;
import com.mercans.integration_api.jpa.repository.JsonResponseRepository;
import com.mercans.integration_api.model.JsonResponse;
import java.io.*;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvReadService {

  private final JobLauncher jobLauncher;
  private final Job readCsvJob;
  private final FileService fileService;
  private final JsonResponseRepository jsonResponseRepository;

  public UUID saveCsvData(MultipartFile file)
      throws JobInstanceAlreadyCompleteException,
          JobExecutionAlreadyRunningException,
          JobParametersInvalidException,
          JobRestartException {

    // todo refactor this into new step
    var pathToStoredCsvFile =
        Optional.ofNullable(fileService.saveFileToLocalDirectory(file))
            .orElseThrow(() -> new RuntimeException("DOESNT WORK!"));

    var jsonResponseUuid = UUID.randomUUID();

    var jsonFilePath = JSON_FILES_UPLOAD_DIRECTORY + jsonResponseUuid + ".json";

    JobParameters jobParameters =
        new JobParametersBuilder()
            // passing csv path so i can reuse it later during reading of file in CsvFileReader
            .addJobParameter(
                BATCH_JOB_CSV_FILE_PATH,
                new JobParameter<>(pathToStoredCsvFile.toString(), String.class))
            .addJobParameter(
                GlobalConstants.BATCH_JOB_DATE, new JobParameter<>(new Date(), Date.class))
            .addJobParameter(
                GlobalConstants.BATCH_JOB_CSV_FILE_NAME,
                new JobParameter<>(file.getOriginalFilename(), String.class))
            .addJobParameter(
                GlobalConstants.BATCH_JOB_JSON_UUID,
                new JobParameter<>(jsonResponseUuid, UUID.class))
            .addJobParameter(
                GlobalConstants.BATCH_JOB_JSON_FILE_PATH,
                new JobParameter<>(jsonFilePath, String.class))
            .toJobParameters();

    jobLauncher.run(readCsvJob, jobParameters);

    return jsonResponseUuid;
  }

  public JsonResponse getJsonResponseFromDb(UUID jsonID) {
    JsonResponseEntity jsonResponseEntity =
        jsonResponseRepository
            .findById(jsonID)
            .orElseThrow(() -> new RuntimeException("NOT FOUND!"));
    return jsonResponseEntity.getPayload();
  }
}
