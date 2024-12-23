package com.mercans.integration_api.config;

import com.mercans.integration_api.model.RequestEntry;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class SpringBatchConfiguration {


    @Bean
    Job readCsvJob(JobRepository jobRepository,
                   Step readCsvStep) {
        return new JobBuilder("readCsvJob", jobRepository).start(readCsvStep).build();
    }

    @Bean
    Step readCsvStep(JobRepository jobRepository,
                     PlatformTransactionManager platformTransactionManager,
                     CsvFileReader csvFileReader,
                     JsonWriter jsonWriter,
                     @Value("${integration.chunk-size}" ) int chunkSize) // map from yaml)
    {
        return new StepBuilder("readCsvStep", jobRepository).
                <RequestEntry, RequestEntry>chunk(chunkSize, platformTransactionManager)
                .reader(csvFileReader)
                .processor(item -> item)
                .writer(jsonWriter)
                .build();
    }

}
