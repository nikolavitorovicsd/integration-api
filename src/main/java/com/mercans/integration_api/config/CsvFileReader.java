package com.mercans.integration_api.config;

import static com.mercans.integration_api.service.CsvReadService.CSV_FILE_NAME;

import com.mercans.integration_api.model.RequestEntry;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.FileReader;
import java.io.IOException;
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
public class CsvFileReader implements ItemStreamReader<RequestEntry> {

  private final String csvFileName;

  private Iterator<RequestEntry> csvIterator;

  public CsvFileReader(@Value("#{jobParameters['" + CSV_FILE_NAME + "']}") String csvFileName) {
    this.csvFileName = csvFileName;
  }

  @SneakyThrows
  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    // todo Next step is to upload file to somewhere and then work with it
    try {
      CsvToBean<RequestEntry> csvToBean =
          new CsvToBeanBuilder<RequestEntry>(new FileReader(csvFileName))
              .withType(RequestEntry.class)
              .build();

      csvIterator = csvToBean.iterator();
    } catch (IOException e) {
      throw new RuntimeException(String.format("File with name '%s' doesn't exist!", csvFileName));
    }
  }

  @Override
  public void update(ExecutionContext executionContext) throws ItemStreamException {
    //
  }

  @Override
  public void close() throws ItemStreamException {
    //
  }

  @Override
  public RequestEntry read() {
    if (csvIterator.hasNext()) {
      return csvIterator.next();
    }
    return null;
  }
}
