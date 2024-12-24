package com.mercans.integration_api.config;

import com.mercans.integration_api.model.RequestEntry;
import jakarta.validation.ValidationException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SpringBatchConfiguration {

  // todo refactor steps, separate concerns of uploading removing files differently, files could be
  // removed right after reading
  @Bean
  Job readCsvJob(
      JobRepository jobRepository,
      Step readCsvStep,
      RemoveUploadedCsvFileAfterJobListener removeUploadedCsvFileAfterJobListener,
      CompressJsonAndRemoveAfterJobListener compressJsonAndRemoveAfterJobListener) {
    return new JobBuilder("readCsvJob", jobRepository)
        .start(readCsvStep)
        // todo better use steps insted of listeners as they somehow are in different order than registered
        .listener(removeUploadedCsvFileAfterJobListener)
        .listener(compressJsonAndRemoveAfterJobListener)
        .build();
  }

  @Bean
  Step readCsvStep(
      JobRepository jobRepository,
      PlatformTransactionManager platformTransactionManager,
      CsvFileReader csvFileReader,
      JsonWriter jsonWriter,
      @Value("${integration.chunk-size}") int chunkSize,
      @Value("${integration.skip-limit}") int skipLimit) {
    return new StepBuilder("readCsvStep", jobRepository)
        .<RequestEntry, RequestEntry>chunk(chunkSize, platformTransactionManager)
        .reader(csvFileReader)
        .processor(item -> item)
        .writer(jsonWriter)
        .faultTolerant()
        .skip(
            ValidationException
                .class) // skip all rows that cause ValidationException // todo nikola think about
        // validating exceptions
        .skipLimit(
            skipLimit) // for now skip limit is higher than anticipated csv lines count to allow
        // whole file to be processed
        // todo add skip listener to collect all skipped rows
        .build();
  }
}
