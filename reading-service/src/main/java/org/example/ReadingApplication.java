package org.example;

import org.example.config.UploadProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(UploadProperties.class)
public class ReadingApplication {
    public static void main(String[] args) {

        SpringApplication.run(ReadingApplication.class, args);
    }
}