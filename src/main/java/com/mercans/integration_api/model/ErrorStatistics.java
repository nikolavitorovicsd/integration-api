package com.mercans.integration_api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
public class ErrorStatistics implements Serializable {

  private final AtomicInteger errorCount = new AtomicInteger();

  private final List<String> errors = new ArrayList<>();

  public void updateErrorCount() {
    errorCount.incrementAndGet();
  }
}
