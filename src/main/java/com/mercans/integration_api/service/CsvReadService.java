package com.mercans.integration_api.service;

import static com.mercans.integration_api.constants.GlobalConstants.CSV_FILE_PATH;

import com.mercans.integration_api.constants.GlobalConstants;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CsvReadService {

  @Autowired JobLauncher jobLauncher;

  @Autowired Job readCsvJob;

  // todo nikola batching should be done, from file name that will be stored in app memory and after
  // job is completed file
  //  should be deleted but there should be result json saved to db with uuiid and file name that
  // was passed and date
  //  and spring batch shouldnt save anything when returning result, it should return uuid of json
  // saved to db so user can fetch it
  // todo nikola errors response in json sould probably contain array of employee ids which were
  // skipped because they were missing some fields with validation message included
  public UUID saveCsvData(MultipartFile fileName)
      throws JobInstanceAlreadyCompleteException,
          JobExecutionAlreadyRunningException,
          JobParametersInvalidException,
          JobRestartException {

    var jsonResponseUuid = UUID.randomUUID();

    JobParameters jobParameters =
        new JobParametersBuilder()
            // passing csv path so i can reuse it later during reading of file in CsvFileReader
            .addJobParameter(
                CSV_FILE_PATH,
                new JobParameter<>(
                    "src/main/resources/csv_files/input_01.csv", // todo hardcoded, needs to be
                    // handled differently
                    String.class)) // path must be full from the source
            .addJobParameter(GlobalConstants.DATE, new JobParameter<>(new Date(), Date.class))
            .addJobParameter(
                GlobalConstants.CSV_FILE_NAME, new JobParameter<>(fileName.getName(), String.class))
            .addJobParameter(
                GlobalConstants.JSON_UUID, new JobParameter<>(jsonResponseUuid, UUID.class))
            .toJobParameters();

    jobLauncher.run(readCsvJob, jobParameters);

    return jsonResponseUuid;
  }
}
