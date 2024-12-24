package com.mercans.integration_api.config;

import static com.mercans.integration_api.constants.GlobalConstants.BATCH_JOB_CSV_FILE_NAME;
import static com.mercans.integration_api.constants.GlobalConstants.BATCH_JOB_CSV_FILE_PATH;

import com.mercans.integration_api.service.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@JobScope
@Slf4j
@Component
public class RemoveUploadedCsvFileAfterJobListener implements JobExecutionListener {

  private final String pathToUploadedCsvFile;
  private final String csvFileName;

  public RemoveUploadedCsvFileAfterJobListener(
      @Value("#{jobParameters['" + BATCH_JOB_CSV_FILE_PATH + "']}") String pathToUploadedCsvFile,
      @Value("#{jobParameters['" + BATCH_JOB_CSV_FILE_NAME + "']}") String csvFileName) {
    this.pathToUploadedCsvFile = pathToUploadedCsvFile;
    this.csvFileName = csvFileName;
  }

  // after converting CSV to required JSON response, we remove it
  @Override
  public void afterJob(JobExecution jobExecution) {
    FileUtils.deleteFile(pathToUploadedCsvFile);
    log.info("Uploaded csv file '{}' deleted.", csvFileName);
  }
}
