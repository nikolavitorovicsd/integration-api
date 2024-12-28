package com.mercans.integration_api.config;

import static com.mercans.integration_api.constants.GlobalConstants.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercans.integration_api.actions.Action;
import com.mercans.integration_api.actions.HireAction;
import com.mercans.integration_api.jpa.EmployeeEntity;
import com.mercans.integration_api.jpa.SalaryComponentEntity;
import com.mercans.integration_api.jpa.repository.EmployeeRepository;
import com.mercans.integration_api.model.JsonResponse;
import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.enums.ActionType;
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

    // todo finish logic
    /**
     * // todo we dont care about duplicate terminate/change update operations // TERMINATE/CHANGE
     * will be added here var updateOperations = new ArrayList<Action>();
     *
     * <p>List terminateActions = new ArrayList<Action>(); List changeActions = new
     * ArrayList<Action>();
     *
     * <p>// todo we !!! about !!! duplicate HIRE insert operations and should skip them if any in
     * batches
     *
     * <p>// HIRE // insert operations should be unique as a whole, means no duplicates (mulitple
     * same rows should be avoided by using set) // if there are 2 HIRE operations that have same
     * employeeCode but other fields are different, we should put that one in errors Set
     * insertOperations = new HashSet<Action>();
     *
     * <p>chunk.forEach(action -> { switch (action.getAction()) { case TERMINATE ->
     * terminateActions.add(action); case CHANGE -> changeActions.add(action); case HIRE ->
     * insertOperations.add(action); } });
     */

    //  THIS GOES INTO DB

    var employeeCodesThatExistInDb = batchJobStatistics.getEmployeeCodesThatExistInDb();
    // todo add other type of employees
    var hireEmployees =
        chunk.getItems().stream()
            .filter(action -> action.getAction().equals(ActionType.HIRE))
            .map(HireAction.class::cast)
            // check if exists in db, if it does, skipp it
            .map(
                action -> {
                  // if employee exists already in  db, add it to error and skip it
                  if (employeeCodesThatExistInDb.contains(action.employeeCode())) {
                    Map<String, Action> innerMap =
                        jsonResponse
                            .errors()
                            .computeIfAbsent(action.getAction(), k -> new HashMap<>());
                    // Add the new member to the inner map
                    innerMap.put(action.employeeCode(), action);
                    return null;
                  } else {
                    // if employee doesn't exist in db, add it BUT ALSO add its employeeCode to the
                    // Cache Set
                    employeeCodesThatExistInDb.add(action.employeeCode());
                    return action;
                  }
                })
            .filter(Objects::nonNull) // we filter null values which will be added to error table
            .map(this::buildHirePersonEntity)
            .toList();

    jsonResponse.payload().addAll(chunk.getItems());
    // write json to file
    objectMapper.writeValue(jsonFilePath, jsonResponse);

    batchJobStatistics.updateJsonFileWrittenLinesCount(
        chunk.size()); // increase csv read lines count

    // saving to db
    employeeRepository.saveAll(hireEmployees);

    // just logging todo check later
    if (chunk.isEnd()) {
      log.info(
          "Written '{}' lines to json file  of total '{}' lines from csv file.",
          batchJobStatistics.getJsonFileLinesCount(),
          batchJobStatistics.getCsvFileReadLinesCount());
    }
  }

  private EmployeeEntity buildHirePersonEntity(HireAction hireAction) {
    return EmployeeEntity.builder()
        .employeeCode(hireAction.employeeCode())
        .employeeHireDate(hireAction.employeeHireDate())
        .employeeFullName(hireAction.employeeFullName())
        .employeGender(hireAction.employeGender())
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

  private JsonResponse getOrCreateJson(File jsonFilePath) throws IOException {
    JsonResponse jsonResponse;
    if (jsonFilePath.exists()) {
      // this reads data from file if it exists
      jsonResponse = objectMapper.readValue(jsonFilePath, JsonResponse.class);
    } else {
      // this creates a new json file if it doesn't exist
      jsonResponse =
          new JsonResponse(jsonResponseUUID, csvFileName, new HashMap<>(), new ArrayList<>());
    }
    return jsonResponse;
  }
}
