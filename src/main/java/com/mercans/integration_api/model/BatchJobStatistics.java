package com.mercans.integration_api.model;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class BatchJobStatistics {

  // how many lines there were in csv
  private final AtomicInteger csvFileReadLinesCount = new AtomicInteger();
  // how many lines were written to json
  private final AtomicInteger jsonFileLinesCount = new AtomicInteger();

  // errors statistics
  private final ErrorStatistics errorStatistics = new ErrorStatistics();

  // keep track of already processed employeeCodes in one job
  private final Set<String> hireEmployeesThatWereAlreadyProcessed = new HashSet<>();

  // keep track of existing employees in db
  private final Set<String> employeeCodesThatExistInDb;

  // keep track of 'person' table ID sequence
  private final AtomicLong personSequence = new AtomicLong();
  // keep track of 'salary_component' table ID sequence
  private final AtomicLong componentSequence = new AtomicLong();

  public void updatePersonSequence(long personCount) {
    personSequence.getAndAdd(personCount);
  }

  public void updateComponentSequence(long componentCount) {
    componentSequence.getAndAdd(componentCount);
  }

  public void updateCsvFileReadLinesCount() {
    csvFileReadLinesCount.incrementAndGet();
  }

  public void updateJsonFileWrittenLinesCount(int linesCount) {
    jsonFileLinesCount.getAndAdd(linesCount);
  }

  public boolean isEmployeeInDb(String employeeCode) {
    return employeeCodesThatExistInDb.contains(employeeCode);
  }

  public boolean isHireEmployeeAlreadyProcessed(String employeeCode) {
    return hireEmployeesThatWereAlreadyProcessed.contains(employeeCode);
  }

  public void addToAlreadyProcessedHireEmployees(String employeeCode) {
    hireEmployeesThatWereAlreadyProcessed.add(employeeCode);
  }

  // this method checks whether employee exists in db or in already processed Hire employees
  // in order to avoid doing redundant work and sending this employee to be saved in writer
  // example: we receieve CSV row that alters employee that is not existing in db
  // example: we receieve CSV row that alters employee that is not yet inserted but will be inserted
  // in some later row
  public boolean isNotPresentInDbAndNotProcessed(String employeeCode) {
    return !isEmployeeInDb(employeeCode) && !isHireEmployeeAlreadyProcessed(employeeCode);
  }
}
