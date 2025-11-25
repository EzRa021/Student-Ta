package com.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Lab Management System.
 * Spring Boot application with WebSocket and JPA support.
 */
@SpringBootApplication
@EnableScheduling
public class LabManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabManagementSystemApplication.class, args);
    }
}