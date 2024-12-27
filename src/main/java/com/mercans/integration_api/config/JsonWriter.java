package com.mercans.integration_api.config;

import static com.mercans.integration_api.constants.GlobalConstants.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercans.integration_api.actions.Action;
import com.mercans.integration_api.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@JobScope
@Slf4j
public class JsonWriter implements ItemWriter<Action> {

  private final String targetJsonPath;
  private final String csvFileName;
  private final ObjectMapper objectMapper;

  public JsonWriter(
      @Value("#{jobParameters['" + BATCH_JOB_JSON_FILE_PATH + "']}") String targetJsonPath,
      @Value("#{jobParameters['" + BATCH_JOB_CSV_FILE_NAME + "']}") String csvFileName,
      ObjectMapper objectMapper) {

    this.targetJsonPath = targetJsonPath;
    this.csvFileName = csvFileName;
    this.objectMapper = objectMapper;
  }

  // this method will append csv lines in chunks to the same json file
  @Override
  public void write(Chunk<? extends Action> chunk) throws IOException {
    // create directory if missing
    FileUtils.createDirectoryIfMissing(JSON_FILES_UPLOAD_DIRECTORY);

    // todo move this to job param to avoid calling it all the time
    String jsonFileName = targetJsonPath.substring(targetJsonPath.lastIndexOf("/") + 1);

    log.info(
        "Writing {} lines from uploaded CSV file '{}' to JSON file '{}'",
        chunk.size(),
        csvFileName,
        jsonFileName);

    File jsonFilePath = new File(targetJsonPath);

    List<Action> existingRequests = new ArrayList<>();

    if (jsonFilePath.exists()) {
      existingRequests = objectMapper.readValue(jsonFilePath, new TypeReference<>() {});
    }
    existingRequests.addAll(chunk.getItems());

    objectMapper.writeValue(jsonFilePath, existingRequests);
  }
}
