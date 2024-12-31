package com.mercans.integration_api.config;

import static com.mercans.integration_api.constants.GlobalConstants.BATCH_JOB_CSV_FILE_PATH;

import com.mercans.integration_api.config.listeners.BatchJobCache;
import com.mercans.integration_api.exception.handlers.CsvReadCustomExceptionHandler;
import com.mercans.integration_api.model.EmployeeRecord;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import lombok.SneakyThrows;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class CsvFileReader implements ItemStreamReader<EmployeeRecord> {

  private final String csvFileName;
  private final BatchJobCache batchJobCache;

  private Iterator<EmployeeRecord> csvIterator;
  private Reader fileReader;

  public CsvFileReader(
      @Value("#{jobParameters['" + BATCH_JOB_CSV_FILE_PATH + "']}") String csvFileName,
      BatchJobCache batchJobCache) {
    this.csvFileName = csvFileName;
    this.batchJobCache = batchJobCache;
  }

  // this method maps csv lines directly to java pojo class using opencsv lib
  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    try {
      fileReader = new FileReader(csvFileName);
      CsvToBean<EmployeeRecord> csvToBean =
          new CsvToBeanBuilder<EmployeeRecord>(fileReader)
              .withType(EmployeeRecord.class)
              // handle white spaces in csv
              .withIgnoreLeadingWhiteSpace(true)
              // handle all kinds of CSV exceptions during read
              .withExceptionHandler(new CsvReadCustomExceptionHandler())
              .build();

      csvIterator = csvToBean.iterator();
    } catch (IOException e) {
      throw new RuntimeException(
          String.format("File with employeeName '%s' doesn't exist!", csvFileName));
    }
  }

  @Override
  public void update(ExecutionContext executionContext) throws ItemStreamException {
    //
  }

  // closing reader
  @SneakyThrows
  @Override
  public void close() throws ItemStreamException {
    fileReader.close();
  }

  @Override
  public EmployeeRecord read() {
    if (csvIterator.hasNext()) {
      // increase csv read lines count
      batchJobCache.getStatistics().updateCsvFileReadLinesCount();
      return csvIterator.next();
    }
    return null;
  }
}
