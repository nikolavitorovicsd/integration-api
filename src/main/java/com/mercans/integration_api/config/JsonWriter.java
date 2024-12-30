package com.mercans.integration_api.config;

import static com.mercans.integration_api.constants.GlobalConstants.*;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercans.integration_api.jpa.EmployeeEntity;
import com.mercans.integration_api.jpa.SalaryComponentEntity;
import com.mercans.integration_api.jpa.repository.EmployeeRepository;
import com.mercans.integration_api.jpa.repository.TestInsertRepository;
import com.mercans.integration_api.model.BatchJobStatistics;
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
  private final BatchJobStatistics batchJobStatistics;
  private final ObjectMapper objectMapper;

  private final EmployeeRepository employeeRepository;
  private final TestInsertRepository testInsertRepository;
  private final BulkInsertService bulkInsertService;

  public JsonWriter(
      @Value("#{jobParameters['" + BATCH_JOB_JSON_FILE_PATH + "']}") String targetJsonPath,
      @Value("#{jobParameters['" + BATCH_JOB_CSV_FILE_NAME + "']}") String csvFileName,
      @Value("#{jobParameters['" + BATCH_JOB_JSON_UUID + "']}") UUID jsonResponseUUID,
      @Value("#{jobExecutionContext['" + BATCH_JOB_STATISTICS + "']}")
          BatchJobStatistics batchJobStatistics,
      ObjectMapper objectMapper,
      EmployeeRepository employeeRepository,
      TestInsertRepository testInsertRepository,
      BulkInsertService bulkInsertService) {

    this.targetJsonPath = targetJsonPath;
    this.csvFileName = csvFileName;
    this.jsonResponseUUID = jsonResponseUUID;
    this.batchJobStatistics = batchJobStatistics;
    this.objectMapper = objectMapper;
    this.employeeRepository = employeeRepository;
    this.testInsertRepository = testInsertRepository;
    this.bulkInsertService = bulkInsertService;
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

    var employeeCodesThatExistInDb = batchJobStatistics.getEmployeeCodesThatExistInDb();

    // todo add other type of employees
    var hireEmployees =
        chunk.getItems().stream()
            .filter(action -> action.getAction().equals(ActionType.HIRE))
            .map(HireAction.class::cast)
            .filter(hireAction -> !hireAction.shouldBeSkippedDuringWrite())
            .map(this::buildHirePersonEntity)
            .toList();

    //    var updateEmployees =
    //        chunk.getItems().stream()
    //            .filter(action -> action.getAction().equals(ActionType.CHANGE))
    //            .map(ChangeAction.class::cast)
    //            .toList();

    //
    File jsonFilePath = new File(targetJsonPath);

    JsonResponse jsonResponse = getOrCreateJson(jsonFilePath);
    // append items to json
    jsonResponse.payload().addAll(chunk.getItems());

    // increase json write lines count
    batchJobStatistics.updateJsonFileWrittenLinesCount(chunk.size());

    // saving to db and updating the list of employeeCodes to have it for next chunk
    if (isNotEmpty(hireEmployees)) {
      //      var addedEmployees = employeeRepository.saveAll(hireEmployees);
      //      employeeCodesThatExistInDb.addAll(
      //          addedEmployees.stream().map(EmployeeEntity::getEmployeeCode).toList());

      log.info("_____________________NEWWWW____BULKKKKKKKKKKKKKKKKKKKKKKKK");
      bulkInsertService.bulkInsert(hireEmployees, batchJobStatistics);
    }

    if (chunk.isEnd()) {
      log.info(
          "Written '{}' lines to json file  of total '{}' lines from csv file.",
          batchJobStatistics.getJsonFileLinesCount(),
          batchJobStatistics.getCsvFileReadLinesCount());
      // add all errors in the last chunk
      jsonResponse =
          jsonResponse.toBuilder().errors(batchJobStatistics.getErrorStatistics()).build();
    }

    // update the json file
    objectMapper.writeValue(jsonFilePath, jsonResponse);
  }

  //  private void doBulkInsert()

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
