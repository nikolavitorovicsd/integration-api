package com.mercans.integration_api.exception.handlers;

import com.mercans.integration_api.cache.BatchJobCache;
import com.mercans.integration_api.model.ErrorStatistics;
import com.opencsv.bean.exceptionhandler.CsvExceptionHandler;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvReadCustomExceptionHandler implements CsvExceptionHandler {

  private final BatchJobCache batchJobCache;

  @Override
  public CsvException handleException(CsvException exception) throws CsvException {

    // if row has fewer fields than header columns we skip it
    if (exception instanceof CsvRequiredFieldEmptyException) {
      var message = exception.getMessage();
      var errorMessage =
          String.format(
              "Failed to read line '%s', reason: %s",
              Arrays.toString(exception.getLine()), message);
      saveException(errorMessage);

      if (log.isDebugEnabled()) {
        log.error(errorMessage);
      }
      return null;
    }
    return exception;
  }

  private void saveException(String exceptionMessage) {
    ErrorStatistics errorStatistics = batchJobCache.getStatistics().getErrorStatistics();
    // increase error count
    errorStatistics.updateErrorCount();
    // add to error list
    errorStatistics.getErrors().add(exceptionMessage);

    // increase csv lines count
    batchJobCache.getStatistics().updateCsvFileReadLinesCount();
  }
}
