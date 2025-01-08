package com.mercans.integration_api.cache;

import static com.mercans.integration_api.constants.GlobalConstants.BATCH_JOB_STATISTICS;

import com.mercans.integration_api.model.BatchJobStatistics;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// cache class, currently used only for BatchJobStatistics
@Component
@Slf4j
public class BatchJobCache {

  @Getter private final ConcurrentHashMap<String, Object> dataMap = new ConcurrentHashMap<>();

  public void putStatistics(BatchJobStatistics statistics) {
    dataMap.put(BATCH_JOB_STATISTICS, statistics);
  }

  public BatchJobStatistics getStatistics() {
    return (BatchJobStatistics) dataMap.get(BATCH_JOB_STATISTICS);
  }

  public void clearStatistics() {
    dataMap.clear();
    log.info("BatchJobCache cleared successfully.");
  }
}
