package com.mercans.integration_api.integration;

import static com.mercans.integration_api.constants.GlobalConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.batch.core.BatchStatus.COMPLETED;

import com.mercans.integration_api.IntegrationApiApplication;
import com.mercans.integration_api.cache.BatchJobCache;
import com.mercans.integration_api.config.SpringBatchConfiguration;
import com.mercans.integration_api.constants.GlobalConstants;
import com.mercans.integration_api.jpa.EmployeeEntity;
import com.mercans.integration_api.jpa.SalaryComponentEntity;
import com.mercans.integration_api.jpa.repository.EmployeeRepository;
import com.mercans.integration_api.jpa.repository.JsonResponseRepository;
import com.mercans.integration_api.model.JsonResponse;
import com.mercans.integration_api.model.actions.Action;
import com.mercans.integration_api.model.enums.Currency;
import com.mercans.integration_api.model.enums.Gender;
import com.mercans.integration_api.service.CsvReadService;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBatchTest
@ActiveProfiles("test")
@SpringJUnitConfig({SpringBatchConfiguration.class, IntegrationApiApplication.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DirtiesContext
public class ReadCsvBatchJobIntegrationTest {

  public static final String CSV_FILES_UPLOAD_DIRECTORY = "src/test/resources/tmp/csv_files/";
  public static final String JSON_FILES_UPLOAD_DIRECTORY = "src/test/resources/tmp/json_files/";
  public static final String CSV_FILES_SOURCE_DIRECTORY = "src/test/resources/csv_source/";

  @Autowired private JobLauncherTestUtils jobLauncherTestUtils;
  @Autowired private JobRepositoryTestUtils jobRepositoryTestUtils;
  @Autowired private JobLauncher asyncJobLauncher;
  @Autowired private PlatformTransactionManager platformTransactionManager;
  @Autowired private EmployeeRepository employeeRepository;
  @Autowired private JsonResponseRepository jsonResponseRepository;
  @Autowired private BatchJobCache batchJobCache;
  @Autowired private CsvReadService csvReadService;

  @Container
  private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
      new PostgreSQLContainer<>("postgres:latest")
          .withDatabaseName("test_db")
          .withUsername("testuser")
          .withPassword("testpass");

  @BeforeAll
  static void startContainer() {
    System.setProperty("spring.datasource.url", POSTGRE_SQL_CONTAINER.getJdbcUrl());
    System.setProperty("spring.datasource.username", POSTGRE_SQL_CONTAINER.getUsername());
    System.setProperty("spring.datasource.password", POSTGRE_SQL_CONTAINER.getPassword());
    System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
    System.setProperty("spring.batch.job.enabled", "true");
  }

  @BeforeEach
  void batchSetup() {
    jobLauncherTestUtils.setJobLauncher(asyncJobLauncher);
    jobRepositoryTestUtils.removeJobExecutions();
  }

  @AfterEach
  void cleanDb() {
    // important to remove previous data
    employeeRepository.deleteAll();
  }

  @Test
  // e2e test for provided "input_01.csv" file
  public void readCsvJobShouldProcessCsvFileSuccessfully_input01file() throws Exception {
    // given

    String fileName = "input_01.csv";

    // source path of csv file that needs to be copied to csv_files directory to be picked up by job
    // copying is done because job removes processed csv and jsons after finishing
    Path csvSourcePath = Paths.get(CSV_FILES_SOURCE_DIRECTORY + fileName);
    Path csvTargetPath = Paths.get(CSV_FILES_UPLOAD_DIRECTORY + fileName);
    Files.copy(csvSourcePath, csvTargetPath);

    // give json an UUID to start a job, it will be used later
    var jsonResponseUuid = UUID.randomUUID();
    var jsonFilePath = JSON_FILES_UPLOAD_DIRECTORY + jsonResponseUuid + JSON;

    JobParameters jobParameters =
        new JobParametersBuilder()
            // passing csv path so i can reuse it later during reading of file in CsvFileReader
            .addJobParameter(
                BATCH_JOB_CSV_FILE_PATH, // source file
                new JobParameter<>(csvTargetPath.toString(), String.class))
            .addJobParameter(
                GlobalConstants.BATCH_JOB_DATE, new JobParameter<>(new Date(), Date.class))
            .addJobParameter(
                GlobalConstants.BATCH_JOB_CSV_FILE_NAME, new JobParameter<>(fileName, String.class))
            .addJobParameter(
                GlobalConstants.BATCH_JOB_JSON_UUID,
                new JobParameter<>(jsonResponseUuid, UUID.class))
            .addJobParameter(
                BATCH_JOB_JSON_FILE_PATH, new JobParameter<>(jsonFilePath, String.class))
            .toJobParameters();

    // when
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

    // then
    assertEquals(BatchStatus.STARTING, jobExecution.getStatus());

    await()
        .atMost(60, TimeUnit.SECONDS)
        .pollInterval(2, TimeUnit.SECONDS)
        .until(
            () ->
                COMPLETED
                    == jobLauncherTestUtils
                        .getJobRepository()
                        .getLastJobExecution(READ_CSV_JOB, jobExecution.getJobParameters())
                        .getStatus());

    assertEquals(COMPLETED, jobExecution.getStatus());

    List<EmployeeEntity> actualList = employeeRepository.getAllEmployees();
    List<EmployeeEntity> expectedList = buildEntityList();

    assertThat(actualList)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields(
            "creationDate", "modificationDate", "employeeCode")
        .containsExactlyInAnyOrder(expectedList.toArray(EmployeeEntity[]::new));
    assertEquals(4, actualList.size());

    // assert cache was cleared after job completed
    assertTrue(batchJobCache.getDataMap().isEmpty());

    // assert json was stored into db
    JsonResponse jsonResponse = csvReadService.getJsonResponseFromDb(jsonResponseUuid);
    assertEquals(jsonResponseUuid, jsonResponse.uuid());
    assertEquals(fileName, jsonResponse.fname());
    assertEquals(4, jsonResponse.payload().size());
    assertEquals(6, jsonResponse.errors().errorCount());

    // check that actual list in db matches list of valid actions in json
    assertEquals(
        actualList.stream().map(EmployeeEntity::getEmployeeCode).toList(),
        jsonResponse.payload().stream().map(Action::getEmployeeCode).toList());

    // check that temporary files were removed
    assertFalse(Files.exists(csvTargetPath), "File should not exist");
    assertFalse(Files.exists(Path.of(jsonFilePath)), "File should not exist");
  }

  @Test
  // e2e test for provided "input_02.csv" file
  public void readCsvJobShouldProcessCsvFileSuccessfully_input02file() throws Exception {
    // given

    String fileName = "input_02.csv";

    // source path of csv file that needs to be copied to csv_files directory to be picked up by job
    // copying is done because job removes processed csv and jsons after finishing
    Path sourcePath = Paths.get(CSV_FILES_SOURCE_DIRECTORY + fileName);
    Path targetPath = Paths.get(CSV_FILES_UPLOAD_DIRECTORY + fileName);
    Files.copy(sourcePath, targetPath);

    // give json an UUID to start a job, it will be used later
    var jsonResponseUuid = UUID.randomUUID();
    var jsonFilePath = JSON_FILES_UPLOAD_DIRECTORY + jsonResponseUuid + JSON;

    JobParameters jobParameters =
        new JobParametersBuilder()
            // passing csv path so i can reuse it later during reading of file in CsvFileReader
            .addJobParameter(
                BATCH_JOB_CSV_FILE_PATH, // source file
                new JobParameter<>(targetPath.toString(), String.class))
            .addJobParameter(
                GlobalConstants.BATCH_JOB_DATE, new JobParameter<>(new Date(), Date.class))
            .addJobParameter(
                GlobalConstants.BATCH_JOB_CSV_FILE_NAME, new JobParameter<>(fileName, String.class))
            .addJobParameter(
                GlobalConstants.BATCH_JOB_JSON_UUID,
                new JobParameter<>(jsonResponseUuid, UUID.class))
            .addJobParameter(
                BATCH_JOB_JSON_FILE_PATH, new JobParameter<>(jsonFilePath, String.class))
            .toJobParameters();

    // when
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

    // then
    assertEquals(BatchStatus.STARTING, jobExecution.getStatus());

    await()
        .atMost(60, TimeUnit.SECONDS)
        .pollInterval(2, TimeUnit.SECONDS)
        .until(
            () ->
                COMPLETED
                    == jobLauncherTestUtils
                        .getJobRepository()
                        .getLastJobExecution(READ_CSV_JOB, jobExecution.getJobParameters())
                        .getStatus());

    assertEquals(COMPLETED, jobExecution.getStatus());

    List<EmployeeEntity> actualList = employeeRepository.getAllEmployees();
    List<EmployeeEntity> expectedList = buildEntityList();

    assertThat(actualList)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields(
            "creationDate", "modificationDate", "employeeCode")
        .containsExactlyInAnyOrder(expectedList.toArray(EmployeeEntity[]::new));
    assertEquals(4, actualList.size());

    // assert cache was cleared after job completed
    assertTrue(batchJobCache.getDataMap().isEmpty());

    // assert json was stored into db
    JsonResponse jsonResponse = csvReadService.getJsonResponseFromDb(jsonResponseUuid);
    assertEquals(jsonResponseUuid, jsonResponse.uuid());
    assertEquals(fileName, jsonResponse.fname());
    assertEquals(4, jsonResponse.payload().size());
    assertEquals(7, jsonResponse.errors().errorCount());

    assertEquals(
        actualList.stream().map(EmployeeEntity::getEmployeeCode).toList(),
        jsonResponse.payload().stream().map(Action::getEmployeeCode).toList());
  }

  private List<EmployeeEntity> buildEntityList() {
    EmployeeEntity employee1 =
        EmployeeEntity.builder()
            .id(1L)
            .employeeCode("611207BE")
            .employeeHireDate(LocalDate.parse("1992-07-01"))
            .employeeFullName("Alberto Leonard")
            .employeGender(Gender.M)
            .employeeBirthDate(LocalDate.parse("1961-12-07"))
            .employeeTerminationDate(null)
            .salaryComponents(
                List.of(
                    SalaryComponentEntity.builder()
                        .id(1L)
                        .amount(BigDecimal.valueOf(3300))
                        .currency(Currency.USD)
                        .startDate(LocalDate.parse("2022-01-01"))
                        .endDate(LocalDate.parse("2022-01-31"))
                        .deleteDate(null)
                        .build()))
            .build();

    EmployeeEntity employee2 =
        EmployeeEntity.builder()
            .id(2L)
            .employeeCode("80072480")
            .employeeHireDate(LocalDate.parse("2013-05-31"))
            .employeeFullName("Brock Salazar")
            .employeGender(Gender.M)
            .employeeBirthDate(LocalDate.parse("1980-07-24"))
            .employeeTerminationDate(null)
            .salaryComponents(
                List.of(
                    SalaryComponentEntity.builder()
                        .id(2L)
                        .amount(BigDecimal.valueOf(1400))
                        .currency(Currency.EUR)
                        .startDate(LocalDate.parse("2022-01-01"))
                        .endDate(LocalDate.parse("2022-01-31"))
                        .deleteDate(null)
                        .build(),
                    SalaryComponentEntity.builder()
                        .id(3L)
                        .amount(BigDecimal.valueOf(100))
                        .currency(Currency.EUR)
                        .startDate(LocalDate.parse("2022-01-01"))
                        .endDate(LocalDate.parse("2022-01-31"))
                        .deleteDate(null)
                        .build()))
            .build();

    EmployeeEntity employee3 =
        EmployeeEntity.builder()
            .id(3L)
            .employeeCode("020121F8")
            .employeeHireDate(LocalDate.parse("2022-03-01"))
            .employeeFullName("Zaiden Arnold")
            .employeGender(null)
            .employeeBirthDate(LocalDate.parse("2002-01-21"))
            .employeeTerminationDate(null)
            .salaryComponents(List.of())
            .build();
    EmployeeEntity employee4 =
        EmployeeEntity.builder()
            .id(4L)
            .employeeCode("22010158")
            .employeeHireDate(LocalDate.parse("2022-01-01"))
            .employeeFullName("Norah Church")
            .employeGender(Gender.F)
            .employeeBirthDate(LocalDate.parse("2003-03-19"))
            .employeeTerminationDate(null)
            .salaryComponents(List.of())
            .build();
    return List.of(employee1, employee2, employee3, employee4);
  }
}
