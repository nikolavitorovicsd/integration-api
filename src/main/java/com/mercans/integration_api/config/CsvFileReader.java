package com.mercans.integration_api.config;

import static com.mercans.integration_api.constants.GlobalConstants.CSV_FILE_PATH;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.mercans.integration_api.model.RequestEntry;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Set;
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

  private final Validator validator;
  private Reader fileReader;

  public CsvFileReader(
      @Value("#{jobParameters['" + CSV_FILE_PATH + "']}") String csvFileName, Validator validator) {
    this.csvFileName = csvFileName;
    this.validator = validator;
  }

  @SneakyThrows
  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    // todo Next step is to upload file to somewhere and then work with it
    try {
      fileReader = new FileReader(csvFileName);
      CsvToBean<RequestEntry> csvToBean =
          new CsvToBeanBuilder<RequestEntry>(fileReader)
              .withType(RequestEntry.class)
              .withIgnoreLeadingWhiteSpace(true) // handle white spaces in csv
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

  @SneakyThrows
  @Override
  public void close() throws ItemStreamException {
    fileReader.close();
  }

  @Override
  public RequestEntry read() {
    if (csvIterator.hasNext()) {
      RequestEntry requestEntry = csvIterator.next();

      Set<ConstraintViolation<RequestEntry>> violations = validator.validate(requestEntry);

      if (isNotEmpty(violations)) {
        throw new ValidationException(violations.toString());
      }
      return requestEntry;
    }
    return null;
  }
}
