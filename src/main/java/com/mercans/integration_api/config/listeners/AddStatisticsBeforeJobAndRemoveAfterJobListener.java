package com.mercans.integration_api.config.listeners;

import com.mercans.integration_api.cache.BatchJobCache;
import com.mercans.integration_api.jpa.repository.EmployeeRepository;
import com.mercans.integration_api.model.BatchJobStatistics;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

@JobScope
@Component
@Slf4j
@RequiredArgsConstructor
public class AddStatisticsBeforeJobAndRemoveAfterJobListener implements JobExecutionListener {

  private final EmployeeRepository employeeRepository;
  private final BatchJobCache batchJobCache;
  private final Semaphore batchSemaphore;

  // load all existing employees to cache
  @Override
  public void beforeJob(JobExecution jobExecution) {
    var employeeCodes = employeeRepository.getAllEmployeeCodes();

    batchJobCache.putStatistics(new BatchJobStatistics(employeeCodes));
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    log.info(
        "Written '{}' payloads to json file of total '{}' rows from csv file.",
        batchJobCache.getStatistics().getJsonFileLinesCount(),
        batchJobCache.getStatistics().getCsvFileReadLinesCount());

    // clear cache
    batchJobCache.clearStatistics();

    // release semaphore
    batchSemaphore.release();
  }
}
