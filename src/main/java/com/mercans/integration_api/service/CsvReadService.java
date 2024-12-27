package com.mercans.integration_api.service;

import static com.mercans.integration_api.constants.GlobalConstants.*;

import com.mercans.integration_api.constants.GlobalConstants;
import com.mercans.integration_api.exception.JsonFileNotFoundException;
import com.mercans.integration_api.utils.FileUtils;
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

  public UUID saveCsvData(MultipartFile file)
      throws JobInstanceAlreadyCompleteException,
          JobExecutionAlreadyRunningException,
          JobParametersInvalidException,
          JobRestartException {

    // todo refactor this into new step
    var pathToStoredCsvFile =
        Optional.ofNullable(fileService.saveFileToLocalDirectory(file))
            .orElseThrow(() -> new RuntimeException("DOESNT WORK!")); // todo

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

  public String getJsonResponse(UUID jsonID) throws IOException {
    var gzipFilePathPlaceHolder = JSON_FILES_UPLOAD_DIRECTORY + "%s.json.gz";

    String filePath = gzipFilePathPlaceHolder.formatted(jsonID);
    File gzippedFile = new File(filePath);

    if (!gzippedFile.exists()) {
      throw new JsonFileNotFoundException(
          String.format("Json report with id = '%s' not found!", jsonID));
    }

    // Decompress the gzipped file and get its content as a String
    return FileUtils.decompressGzippedFile(gzippedFile);
  }
}
