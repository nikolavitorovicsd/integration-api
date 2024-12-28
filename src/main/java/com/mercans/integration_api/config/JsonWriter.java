package com.mercans.integration_api.config;

import static com.mercans.integration_api.constants.GlobalConstants.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercans.integration_api.actions.Action;
import com.mercans.integration_api.actions.HireAction;
import com.mercans.integration_api.jpa.EmployeeEntity;
import com.mercans.integration_api.jpa.repository.EmployeeRepository;
import com.mercans.integration_api.model.JsonResponse;
import com.mercans.integration_api.model.enums.ActionType;
import com.mercans.integration_api.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
  private final UUID jsonResponseUUID;
  private final BatchJobStatistics batchJobStatistics;
  private final ObjectMapper objectMapper;

  private final EmployeeRepository employeeRepository;

  public JsonWriter(
      @Value("#{jobParameters['" + BATCH_JOB_JSON_FILE_PATH + "']}") String targetJsonPath,
      @Value("#{jobParameters['" + BATCH_JOB_CSV_FILE_NAME + "']}") String csvFileName,
      @Value("#{jobParameters['" + BATCH_JOB_JSON_UUID + "']}") UUID jsonResponseUUID,
      @Value("#{jobExecutionContext['" + BATCH_JOB_STATISTICS + "']}")
          BatchJobStatistics batchJobStatistics,
      ObjectMapper objectMapper,
      EmployeeRepository employeeRepository) {

    this.targetJsonPath = targetJsonPath;
    this.csvFileName = csvFileName;
    this.jsonResponseUUID = jsonResponseUUID;
    this.batchJobStatistics = batchJobStatistics;
    this.objectMapper = objectMapper;
    this.employeeRepository = employeeRepository;
  }

  // this method will append csv lines in chunks to the same json file
  @Override
  public void write(Chunk<? extends Action> chunk) throws IOException {
    // create directory if missing
    FileUtils.createDirectoryIfMissing(JSON_FILES_UPLOAD_DIRECTORY);

    // todo move this to job param to avoid calling it all the time
    String jsonFileName = targetJsonPath.substring(targetJsonPath.lastIndexOf("/") + 1);

    log.info(
        "Writing chunk of '{}' lines from uploaded CSV file '{}' to JSON file '{}'",
        chunk.size(),
        csvFileName,
        jsonFileName);

    File jsonFilePath = new File(targetJsonPath);

    JsonResponse jsonResponse = getOrCreateJson(jsonFilePath);

    // add chunk to the payload
    jsonResponse.payload().addAll(chunk.getItems());

    // write json to file
    objectMapper.writeValue(jsonFilePath, jsonResponse);

    batchJobStatistics.updateJsonFileWrittenLinesCount(
        chunk.size()); // increase csv read lines count

    if (chunk.isEnd()) {
      log.info(
          "Written '{}' lines of total '{}'.",
          batchJobStatistics.getJsonFileLinesCount(),
          batchJobStatistics.getCsvFileReadLinesCount());
    }
    // todo add other type of employees
    var hireEmployees =
        chunk.getItems().stream()
            .filter(action -> action.getAction().equals(ActionType.HIRE))
            .map(HireAction.class::cast)
            .map(JsonWriter::buildHirePersonEntity)
            .toList();

    // todo handle if this throws exceptions
    employeeRepository.saveAll(hireEmployees);
  }

  private static EmployeeEntity buildHirePersonEntity(HireAction hireAction) {
    return EmployeeEntity.builder()
        .employeeCode(hireAction.employeeCode())
        .employeeHireDate(hireAction.employeeHireDate())
        .employeeFullName(hireAction.employeeFullName())
        .employeGender(hireAction.employeGender())
        .build();
  }

  private JsonResponse getOrCreateJson(File jsonFilePath) throws IOException {
    JsonResponse jsonResponse;
    if (jsonFilePath.exists()) {
      // this reads data from file if it exists
      jsonResponse = objectMapper.readValue(jsonFilePath, JsonResponse.class);
    } else {
      // this creates a new json file if it doesn't exist
      // todo finish errors
      jsonResponse =
          new JsonResponse(
              jsonResponseUUID, csvFileName, List.of("FINISH ERRORS"), new ArrayList<>());
    }
    return jsonResponse;
  }
}
