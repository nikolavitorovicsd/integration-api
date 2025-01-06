package com.mercans.integration_api.config.listeners;

import static com.mercans.integration_api.constants.GlobalConstants.BATCH_JOB_JSON_FILE_PATH;
import static com.mercans.integration_api.constants.GlobalConstants.BATCH_JOB_JSON_UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercans.integration_api.jpa.JsonResponseEntity;
import com.mercans.integration_api.jpa.repository.JsonResponseRepository;
import com.mercans.integration_api.model.JsonResponse;
import com.mercans.integration_api.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@JobScope
@Component
@Slf4j
public class SaveJsonToDbAndRemoveAfterJobListener implements JobExecutionListener {

  private final String sourceJsonPath;
  private final UUID jsonId;
  private final ObjectMapper objectMapper;
  private final JsonResponseRepository jsonResponseRepository;

  public SaveJsonToDbAndRemoveAfterJobListener(
      @Value("#{jobParameters['" + BATCH_JOB_JSON_FILE_PATH + "']}") String sourceJsonPath,
      @Value("#{jobParameters['" + BATCH_JOB_JSON_UUID + "']}") UUID jsonId,
      ObjectMapper objectMapper,
      JsonResponseRepository jsonResponseRepository) {
    this.sourceJsonPath = sourceJsonPath;
    this.jsonId = jsonId;
    this.objectMapper = objectMapper;
    this.jsonResponseRepository = jsonResponseRepository;
  }

  @Override
  public void afterJob(JobExecution jobExecution) {

    try {
      String jsonFileName = FileUtils.getFileNameWithFormat(sourceJsonPath);

      // saving json to db
      JsonResponse jsonResponse =
          objectMapper.readValue(new File(sourceJsonPath), JsonResponse.class);
      jsonResponseRepository.save(new JsonResponseEntity(jsonId, jsonResponse));

      log.info("Saved JsonResponse to db with id = '{}'.", jsonId);

      // remove json
      FileUtils.deleteFile(sourceJsonPath);
      log.info("Deleted json file '{}'.", jsonFileName);
    } catch (IOException e) {
      log.error("Exception during compressing and removing of json: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
