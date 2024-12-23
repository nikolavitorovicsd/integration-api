package com.mercans.integration_api.config;

import com.mercans.integration_api.model.RequestEntry;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@JobScope
public class JsonWriter implements ItemWriter<RequestEntry> {

  @Override
  public void write(Chunk<? extends RequestEntry> chunk) {
    // todo Next step is saving to db
    var xx = chunk.getItems().stream().toList();
    System.out.printf("Workingggg");
  }
}
