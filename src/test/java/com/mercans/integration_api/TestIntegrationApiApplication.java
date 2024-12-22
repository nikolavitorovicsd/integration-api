package com.mercans.integration_api;

import org.springframework.boot.SpringApplication;

public class TestIntegrationApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(IntegrationApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
