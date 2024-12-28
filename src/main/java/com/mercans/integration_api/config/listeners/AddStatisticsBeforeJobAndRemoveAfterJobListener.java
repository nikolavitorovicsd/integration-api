package com.mercans.integration_api.config.listeners;

import static com.mercans.integration_api.constants.GlobalConstants.*;

import com.mercans.integration_api.config.BatchJobStatistics;
import com.mercans.integration_api.jpa.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

@JobScope
@Component
@Slf4j
public class AddStatisticsBeforeJobAndRemoveAfterJobListener implements JobExecutionListener {

  private final EmployeeRepository employeeRepository;

  public AddStatisticsBeforeJobAndRemoveAfterJobListener(EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
  }

  @Override
  public void beforeJob(JobExecution jobExecution) {
    // put statistics into context so it can be reused later to track how many lines were written to
    // json
    var employeeCodes = employeeRepository.getAllEmployeeCodes();

    // todo its better to load <employeeCode, Entity> to cache so we can update them as well faster

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
