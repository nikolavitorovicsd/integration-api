package com.mercans.integration_api.config;

import java.io.Serializable;
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

  // keep track of existing employees in db
  private final Set<String> employeeCodesThatExistInDb;

  public void updateCsvFileReadLinesCount() {
    csvFileReadLinesCount.incrementAndGet();
  }

  public void updateJsonFileWrittenLinesCount(int linesCount) {
    jsonFileLinesCount.getAndAdd(linesCount);
  }
}
