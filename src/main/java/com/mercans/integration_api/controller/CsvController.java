package com.mercans.integration_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/upload")
public class CsvController {

    @GetMapping(value = "/hello")
    public ResponseEntity<String> helloThere() {
        return new ResponseEntity<>("Hello there", HttpStatus.OK);
    }

}
