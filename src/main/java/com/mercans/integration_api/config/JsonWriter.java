package com.mercans.integration_api.config;

import static com.mercans.integration_api.constants.GlobalConstants.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercans.integration_api.cache.BatchJobCache;
import com.mercans.integration_api.jpa.EmployeeEntity;
import com.mercans.integration_api.jpa.SalaryComponentEntity;
import com.mercans.integration_api.model.ErrorResponse;
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
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
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
    var xx = System.currentTimeMillis();
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
    var actionsToBeWrittenToJson =
        chunk.getItems().stream().filter(action -> !action.shouldBeSkippedDuringWrite()).toList();
    jsonResponse.payload().addAll(actionsToBeWrittenToJson);

    List<Action> changeActions = new ArrayList<>();
    List<Action> terminateActions = new ArrayList<>();
    List<EmployeeEntity> hireEmployees = buildInsertList(chunk, changeActions, terminateActions);

    // todo remove
    log.info("PREPARED FILE AND HIRE EMPLOYEES IN '{}' ms", System.currentTimeMillis() - xx);

    bulkInsertService.bulkInsert(hireEmployees);
    bulkInsertService.bulkUpdate(changeActions);
    bulkInsertService.bulkTerminate(terminateActions);

    // update the json file
    objectMapper.writeValue(jsonFilePath, jsonResponse.toBuilder().errors(getErrors()).build());

    // increase json write lines count
    batchJobCache.getStatistics().updateJsonFileWrittenLinesCount(actionsToBeWrittenToJson.size());
  }

  // this method separates HIRE actions from TERMINATE/CHANGE actions
  private List<EmployeeEntity> buildInsertList(
      Chunk<? extends Action> chunk, List<Action> changeActions, List<Action> terminateActions) {
    return chunk.getItems().stream()
        // first filter out all actions that shouldn't be processed to db
        .filter(action -> !action.shouldBeSkippedDuringWrite())
        // second return action if its HIRE, otherwise return null and update updateActions list
        .map(action -> getHireActionOrUpdateLists(action, changeActions, terminateActions))
        .filter(Objects::nonNull)
        .map(HireAction.class::cast)
        // third build new entities to be inserted into db
        .map(this::buildHirePersonEntity)
        .toList();
  }

  // if action is HIRE, we return it, if its not, we add it to changeActions
  // or terminateActions list to use it later for bulkUpdate/bulkTerminate
  private Action getHireActionOrUpdateLists(
      Action action, List<Action> changeActions, List<Action> terminateActions) {
    if (action.getAction().equals(ActionType.HIRE)) {
      return action;
    } else if (action.getAction().equals(ActionType.CHANGE)) {
      // CHANGE action is skipped but added to list
      changeActions.add(action);
      return null;
    } else {
      // TERMINATE action is skipped but added to list
      terminateActions.add(action);
      return null;
    }
  }

  private EmployeeEntity buildHirePersonEntity(HireAction hireAction) {
    return EmployeeEntity.builder()
        .employeeCode(hireAction.employeeCode())
        .employeeHireDate(hireAction.employeeHireDate())
        .employeeFullName(hireAction.employeeFullName())
        .employeGender(hireAction.employeGender())
        .employeeBirthDate(hireAction.employeeBirthDate())
        .salaryComponents(getSalaryComponents(hireAction))
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

  private ErrorResponse getErrors() {
    return ErrorResponse.builder()
        .errorCount(batchJobCache.getStatistics().getErrorStatistics().getErrorCount().get())
        .errors(batchJobCache.getStatistics().getErrorStatistics().getErrors())
        .build();
  }
}
