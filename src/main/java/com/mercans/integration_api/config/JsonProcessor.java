package com.mercans.integration_api.config;

import com.mercans.integration_api.model.RequestEntry;
import org.springframework.batch.item.ItemProcessor;

// todo nikola in progress or might even not be needed as reader is working fine
public class JsonProcessor implements ItemProcessor<RequestEntry, RequestEntry> {
  @Override
  public RequestEntry process(RequestEntry item) throws Exception {
    return null;
  }
}
