package com.mercans.integration_api.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class BatchJobStatistics implements Serializable {

  // how many lines there were in csv
  private final AtomicInteger csvFileReadLinesCount = new AtomicInteger();
  // how many lines were written to json
  private final AtomicInteger jsonFileLinesCount = new AtomicInteger();

  // errors statistics
  private final ErrorStatistics errorStatistics = new ErrorStatistics();

  // keep track of already processed employees in one job
  private final Set<String> hireEmployeesThatWereAlreadyProcessed = new HashSet<>();

  // keep track of existing employees in db
  private final Set<String> employeeCodesThatExistInDb;

  public void updateCsvFileReadLinesCount() {
    csvFileReadLinesCount.incrementAndGet();
  }

  public void updateJsonFileWrittenLinesCount(int linesCount) {
    jsonFileLinesCount.getAndAdd(linesCount);
  }
}
