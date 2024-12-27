package com.mercans.integration_api.config.listeners;

import static com.mercans.integration_api.constants.GlobalConstants.*;

import com.mercans.integration_api.config.BatchJobStatistics;
import com.mercans.integration_api.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@JobScope
@Component
@Slf4j
public class RemoveUploadedCsvFileAfterJobListener implements JobExecutionListener {

  private final String pathToUploadedCsvFile;
  private final String csvFileName;

  public RemoveUploadedCsvFileAfterJobListener(
      @Value("#{jobParameters['" + BATCH_JOB_CSV_FILE_PATH + "']}") String pathToUploadedCsvFile,
      @Value("#{jobParameters['" + BATCH_JOB_CSV_FILE_NAME + "']}") String csvFileName) {
    this.pathToUploadedCsvFile = pathToUploadedCsvFile;
    this.csvFileName = csvFileName;
  }

  @Override
  public void beforeJob(JobExecution jobExecution) {
    // put statistics into context so it can be reused later to track how many lines were written to
    // json
    jobExecution.getExecutionContext().put(BATCH_JOB_STATISTICS, new BatchJobStatistics());
  }

  // after converting CSV to required JSON response, we remove it
  @Override
  public void afterJob(JobExecution jobExecution) {
    FileUtils.deleteFile(pathToUploadedCsvFile);
    log.info("Deleted csv file '{}'.", csvFileName);
  }
}
