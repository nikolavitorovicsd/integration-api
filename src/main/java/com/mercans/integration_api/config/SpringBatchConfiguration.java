package com.mercans.integration_api.config;

import com.mercans.integration_api.config.listeners.AddStatisticsBeforeJobAndRemoveAfterJobListener;
import com.mercans.integration_api.config.listeners.CreatePersonSalariesDbViewAfterJobListener;
import com.mercans.integration_api.config.listeners.RemoveUploadedCsvFileAfterJobListener;
import com.mercans.integration_api.config.listeners.SaveJsonToDbAndRemoveAfterJobListener;
import com.mercans.integration_api.constants.GlobalConstants;
import com.mercans.integration_api.model.EmployeeRecord;
import com.mercans.integration_api.model.actions.Action;
import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class SpringBatchConfiguration {

  @Bean
  Job readCsvJob(
      JobRepository jobRepository,
      Step readCsvStep,
      CreatePersonSalariesDbViewAfterJobListener createPersonSalariesDbViewAfterJobListener,
      SaveJsonToDbAndRemoveAfterJobListener saveJsonToDbAndRemoveAfterJobListener,
      RemoveUploadedCsvFileAfterJobListener removeUploadedCsvFileAfterJobListener,
      AddStatisticsBeforeJobAndRemoveAfterJobListener
          addStatisticsBeforeJobAndRemoveAfterJobListener) {
    return new JobBuilder(GlobalConstants.READ_CSV_JOB, jobRepository)
        .start(readCsvStep)
        // do not change order, last registered listener is actually called first
        .listener(createPersonSalariesDbViewAfterJobListener) // called last
        .listener(saveJsonToDbAndRemoveAfterJobListener) // called third
        .listener(removeUploadedCsvFileAfterJobListener) // called second
        .listener(addStatisticsBeforeJobAndRemoveAfterJobListener) // called first
        .build();
  }

  @Bean
  Step readCsvStep(
      JobRepository jobRepository,
      PlatformTransactionManager platformTransactionManager,
      CsvFileReader csvFileReader,
      CsvLinesProcessor csvLinesProcessor,
      JsonAndDatabaseWriter jsonAndDatabaseWriter,
      // chunkSize = 1 is used during e2e testing
      @Value("${batch-config.chunk-size: 1}") int chunkSize) {
    return new StepBuilder("readCsvStep", jobRepository)
        .<EmployeeRecord, Action>chunk(chunkSize, platformTransactionManager)
        .reader(csvFileReader)
        .processor(csvLinesProcessor)
        .writer(jsonAndDatabaseWriter)
        .build();
  }

  // using task executor with thread pool to minimize resource consumption
  @Bean
  public TaskExecutor threadPoolTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setMaxPoolSize(1);
    executor.setCorePoolSize(1);
    executor.setQueueCapacity(0);

    return executor;
  }

  // asynchronous launcher that allows to return job info instantly, while job is running in
  // background
  @Bean(name = "asyncJobLauncher")
  public JobLauncher asyncJobLauncher(
      JobRepository jobRepository, TaskExecutor threadPoolTaskExecutor) throws Exception {
    TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();

    jobLauncher.setJobRepository(jobRepository);
    jobLauncher.setTaskExecutor(threadPoolTaskExecutor);
    jobLauncher.afterPropertiesSet();
    return jobLauncher;
  }

  @Bean
  Semaphore batchSemaphore() {
    return new Semaphore(1);
  }
}
