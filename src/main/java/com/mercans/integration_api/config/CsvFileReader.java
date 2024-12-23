package com.mercans.integration_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercans.integration_api.model.RequestEntry;
import lombok.SneakyThrows;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

import static com.mercans.integration_api.service.CsvReadService.CSV_FILE_NAME;

@Component
@StepScope
public class CsvFileReader implements ItemStreamReader<RequestEntry> {

    private final String csvFileName;
    private final ObjectMapper objectMapper;

    private Iterator<String> linesIterator;
    private Stream<String> linesStream;

    public CsvFileReader(@Value("#{jobParameters['" + CSV_FILE_NAME + "']}" ) String csvFileName,
                         ObjectMapper objectMapper){
        this.csvFileName = csvFileName;
        this.objectMapper = objectMapper;
    }


    @SneakyThrows
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Path filePath = Paths.get(csvFileName); // todo need to copy csv file first somewhere and then read it here

        if (Files.notExists(filePath)) {
            throw new RuntimeException(String.format("File with name %s doesnt exist", filePath));
        }

        try {
            linesStream = Files.lines(filePath);

        } catch (IOException e) {
            throw new RuntimeException("File not found on the path!" );
        }
        linesIterator = linesStream.iterator();

        // Skip the header line manually
        if (linesIterator.hasNext()) {
            linesIterator.next(); // Skip the header line
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        //
    }

    @Override
    public void close() throws ItemStreamException {
        linesStream.close();
    }

    @Override
    public RequestEntry read() throws Exception {

        if (linesIterator.hasNext()) {
            var nextLine = linesIterator.next();
            //02EDC235D00C4FF1A7245550E4EC3216,add,Alberto Leonard,611207NCLTAGZQ8U-NJFVQ5OWYFG,male,0,Gallagher,Sexton,160692,010792,indefinite,,611207BE,3300,USD,010122,310122,,,,,
// i dont want to read whole line, only what i need and map it directly to my class
            return objectMapper.readValue(nextLine, RequestEntry.class);
        }

        return null;
    }


}
