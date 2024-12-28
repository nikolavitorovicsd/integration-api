package com.mercans.integration_api.config;

import com.mercans.integration_api.actions.Action;
import com.mercans.integration_api.config.listeners.AddStatisticsBeforeJobAndRemoveAfterJobListener;
import com.mercans.integration_api.config.listeners.CompressJsonAndRemoveAfterJobListener;
import com.mercans.integration_api.config.listeners.RemoveUploadedCsvFileAfterJobListener;
import com.mercans.integration_api.model.EmployeeRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class SpringBatchConfiguration {

  // todo refactor steps, separate concerns of uploading removing files differently, files could be
  // removed right after reading
  @Bean
  Job readCsvJob(
      JobRepository jobRepository,
      Step readCsvStep,
      RemoveUploadedCsvFileAfterJobListener removeUploadedCsvFileAfterJobListener,
      CompressJsonAndRemoveAfterJobListener compressJsonAndRemoveAfterJobListener,
      AddStatisticsBeforeJobAndRemoveAfterJobListener
          addStatisticsBeforeJobAndRemoveAfterJobListener) {
    return new JobBuilder("readCsvJob", jobRepository)
        .start(readCsvStep)
        // do not change order, last registered listener is actually called first
        .listener(compressJsonAndRemoveAfterJobListener) // called third
        .listener(removeUploadedCsvFileAfterJobListener) // called second
        .listener(addStatisticsBeforeJobAndRemoveAfterJobListener) // called first
        .build();
  }

  @Bean
  Step readCsvStep(
      JobRepository jobRepository,
      PlatformTransactionManager platformTransactionManager,
      CsvFileReader csvFileReader,
      JsonProcessor jsonProcessor,
      JsonWriter jsonWriter,
      @Value("${batch-config.chunk-size}") int chunkSize,
      @Value("${batch-config.skip-limit}") int skipLimit) { // todo
    return new StepBuilder("readCsvStep", jobRepository)
        .<EmployeeRecord, Action>chunk(chunkSize, platformTransactionManager)
        .reader(csvFileReader)
        .processor(
            jsonProcessor) // todo add skip listener to collect all skipped rows DURING PROCESS INTO
        // JSON ERRORS!!!!
        .writer(jsonWriter)
        .build();
  }
}
