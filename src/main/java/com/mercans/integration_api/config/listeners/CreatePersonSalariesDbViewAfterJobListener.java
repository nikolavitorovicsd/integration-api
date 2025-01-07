package com.mercans.integration_api.config.listeners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@JobScope
@Component
@RequiredArgsConstructor
@Slf4j
public class CreatePersonSalariesDbViewAfterJobListener implements JobExecutionListener {

  private final JdbcTemplate jdbcTemplate;

  // method creates a view of employees and salaries after job completes
  @Override
  public void afterJob(JobExecution jobExecution) {
    String sqlQuery;
    try {
      Resource classPathResource =
          new ClassPathResource("sql/create_employees_current_salaries_view.sql");
      sqlQuery = Files.readString(Paths.get(classPathResource.getURI()));
    } catch (IOException e) {
      throw new RuntimeException("View sql script is missing!");
    }
    jdbcTemplate.execute(sqlQuery);

    log.info("Successfully created 'employees_current_salaries' in db.");
  }
}
