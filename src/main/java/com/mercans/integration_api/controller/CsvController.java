package com.mercans.integration_api.controller;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/upload")
public class CsvController {

    @GetMapping(value = "/hello")
    public ResponseEntity<String> helloThere() {
        return new ResponseEntity<>("Hello there", HttpStatus.OK);
    }

    @PostMapping(value = "/csv")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Hello there", HttpStatus.OK);
    }

}
