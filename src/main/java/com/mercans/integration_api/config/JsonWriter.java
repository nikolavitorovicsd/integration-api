package com.mercans.integration_api.config;

import static com.mercans.integration_api.constants.GlobalConstants.*;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercans.integration_api.config.listeners.BatchJobCache;
import com.mercans.integration_api.jpa.EmployeeEntity;
import com.mercans.integration_api.jpa.SalaryComponentEntity;
import com.mercans.integration_api.model.JsonResponse;
import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.actions.Action;
import com.mercans.integration_api.model.actions.HireAction;
import com.mercans.integration_api.model.enums.ActionType;
import com.mercans.integration_api.service.BulkInsertService;
import com.mercans.integration_api.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.*;
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

  private final ObjectMapper objectMapper;
  private final BulkInsertService bulkInsertService;
  private final BatchJobCache batchJobCache;

  public JsonWriter(
      @Value("#{jobParameters['" + BATCH_JOB_JSON_FILE_PATH + "']}") String targetJsonPath,
      @Value("#{jobParameters['" + BATCH_JOB_CSV_FILE_NAME + "']}") String csvFileName,
      @Value("#{jobParameters['" + BATCH_JOB_JSON_UUID + "']}") UUID jsonResponseUUID,
      ObjectMapper objectMapper,
      BulkInsertService bulkInsertService,
      BatchJobCache batchJobCache) {

    this.targetJsonPath = targetJsonPath;
    this.csvFileName = csvFileName;
    this.jsonResponseUUID = jsonResponseUUID;
    this.objectMapper = objectMapper;
    this.bulkInsertService = bulkInsertService;
    this.batchJobCache = batchJobCache;
  }

  // this method will append csv lines in chunks to the same json file and in same time save/update
  // data to db
  @Override
  public void write(Chunk<? extends Action> chunk) throws IOException {
    // create directory if missing
    FileUtils.createDirectoryIfMissing(JSON_FILES_UPLOAD_DIRECTORY);

    if (log.isDebugEnabled()) {
      String jsonFileName = FileUtils.getFileNameWithFormat(targetJsonPath);
      log.info(
          "Writing chunk of '{}' lines from uploaded CSV file '{}' to JSON file '{}'",
          chunk.size(),
          csvFileName,
          jsonFileName);
    }

    File jsonFilePath = new File(targetJsonPath);
    JsonResponse jsonResponse = getOrCreateJson(jsonFilePath);
    // append items to json
    jsonResponse.payload().addAll(chunk.getItems());
    // increase json write lines count
    batchJobCache.getStatistics().updateJsonFileWrittenLinesCount(chunk.size());

    List<EmployeeEntity> hireEmployees = buildInsertList(chunk);
    // includes only insert changes
    if (isNotEmpty(hireEmployees)) {
      // saving to db
      var insertedEmployees = bulkInsertService.bulkInsert(hireEmployees);
      // updating the list employeeCodesThatExistInDb to have track in next chunk what was added
      batchJobCache.getStatistics().getEmployeeCodesThatExistInDb().addAll(insertedEmployees);
    }

    // todo
    //    List<EmployeeEntity> updateEmployees = buildUpdateList(chunk);
    //    // includes only update changes
    //    if (isNotEmpty(updateEmployees)) {
    //      // todo
    //    }

    // problematic corner case that rarely happens: work when last chunk size matches provided job
    // chunk size
    // for example when used 1 chunk size for 2 same csv rows, chunk.isEnd() returns false even
    // thought its really last
    if (chunk.isEnd()) {
      log.info(
          "Written '{}' lines to json file  of total '{}' lines from csv file.",
          batchJobCache.getStatistics().getJsonFileLinesCount(),
          batchJobCache.getStatistics().getCsvFileReadLinesCount());
      // add all errors in the last chunk
      jsonResponse =
          jsonResponse.toBuilder()
              .errors(batchJobCache.getStatistics().getErrorStatistics())
              .build();
    }

    // update the json file
    objectMapper.writeValue(jsonFilePath, jsonResponse);
  }

  //  private List<EmployeeEntity> buildUpdateList(Chunk<? extends Action> chunk) {
  //
  //  }

  private List<EmployeeEntity> buildInsertList(Chunk<? extends Action> chunk) {
    return chunk.getItems().stream()
        .filter(action -> action.getAction().equals(ActionType.HIRE))
        .map(HireAction.class::cast)
        .filter(hireAction -> !hireAction.shouldBeSkippedDuringWrite())
        .map(this::buildHirePersonEntity)
        .toList();
  }

  private EmployeeEntity buildHirePersonEntity(HireAction hireAction) {
    return EmployeeEntity.builder()
        .employeeCode(hireAction.employeeCode())
        .employeeHireDate(hireAction.employeeHireDate())
        .employeeFullName(hireAction.employeeFullName())
        .employeGender(hireAction.employeGender())
        .employeeBirthDate(hireAction.employeeBirthDate())
        .salaryComponentEntities(getSalaryComponents(hireAction))
        .build();
  }

  private List<SalaryComponentEntity> getSalaryComponents(HireAction hireAction) {
    return hireAction.payComponents().stream().map(this::buildSalaryComponentEntity).toList();
  }

  private SalaryComponentEntity buildSalaryComponentEntity(PayComponent payComponent) {
    return SalaryComponentEntity.builder()
        .amount(payComponent.amount())
        .currency(payComponent.currency())
        .startDate(payComponent.startDate())
        .endDate(payComponent.endDate())
        .build();
  }

  // if json is missing, application should break because some corner case was missed during
  // handling
  private JsonResponse getOrCreateJson(File jsonFilePath) throws IOException {
    JsonResponse jsonResponse;
    if (jsonFilePath.exists()) {
      // this reads data from file if it exists
      jsonResponse = objectMapper.readValue(jsonFilePath, JsonResponse.class);
    } else {
      // this creates a new json file if it doesn't exist
      jsonResponse = new JsonResponse(jsonResponseUUID, csvFileName, null, new ArrayList<>());
    }
    return jsonResponse;
  }
}
