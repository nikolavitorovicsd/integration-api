package com.mercans.integration_api.config.listeners;

import static com.mercans.integration_api.constants.GlobalConstants.BATCH_JOB_JSON_FILE_PATH;

import com.mercans.integration_api.utils.FileUtils;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@JobScope
@Component
@Slf4j
public class CompressJsonAndRemoveAfterJobListener implements JobExecutionListener {

  private final String sourceJsonPath;

  public CompressJsonAndRemoveAfterJobListener(
      @Value("#{jobParameters['" + BATCH_JOB_JSON_FILE_PATH + "']}") String sourceJsonPath) {
    this.sourceJsonPath = sourceJsonPath;
  }

  // method gzips json to reduce memory usage and deletes original json
  @Override
  public void afterJob(JobExecution jobExecution) {
    String gzipJsonPath = sourceJsonPath + ".gz";

    String jsonFileName = FileUtils.getFileNameWithFormat(sourceJsonPath);
    String gzipJsonFileName = FileUtils.getFileNameWithFormat(gzipJsonPath);

    try {
      // compress json
      FileUtils.compressToGzipFile(sourceJsonPath, gzipJsonPath);
      log.info("Compressed json file created: '{}'.", gzipJsonFileName);

      // remove json and leave only gz json
      FileUtils.deleteFile(sourceJsonPath);
      log.info("Deleted json file '{}'.", jsonFileName);
    } catch (IOException e) {
      // todo refactor
      log.error("Exception during compressing and removing of json: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
