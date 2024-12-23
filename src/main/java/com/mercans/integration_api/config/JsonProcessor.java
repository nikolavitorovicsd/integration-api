package com.mercans.integration_api.config;

import com.mercans.integration_api.model.Request;
import com.mercans.integration_api.model.RequestEntry;
import org.springframework.batch.item.ItemProcessor;

public class JsonProcessor implements ItemProcessor<RequestEntry, Request> {
    @Override
    public Request process(RequestEntry item) throws Exception {
        return null;
    }
}
