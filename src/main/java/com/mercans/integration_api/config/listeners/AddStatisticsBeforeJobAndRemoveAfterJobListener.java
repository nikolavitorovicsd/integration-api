package com.mercans.integration_api.config.listeners;

import static com.mercans.integration_api.constants.GlobalConstants.*;

import com.mercans.integration_api.jpa.repository.EmployeeRepository;
import com.mercans.integration_api.model.BatchJobStatistics;
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

  @Override
  public void beforeJob(JobExecution jobExecution) {
    var employeeCodes = employeeRepository.getAllEmployeeCodes();

    // todo this should be refactored, takes too much space
    jobExecution
        .getExecutionContext()
        .put(BATCH_JOB_STATISTICS, new BatchJobStatistics(employeeCodes));
  }

  // remove unnecessary data
  @Override
  public void afterJob(JobExecution jobExecution) {
    jobExecution.getExecutionContext().remove(BATCH_JOB_STATISTICS);
  }
}
