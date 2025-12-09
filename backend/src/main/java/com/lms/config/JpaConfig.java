package com.lms.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA and Hibernate configuration for the Lab Management System.
 * Configures entity manager factory, transaction management, and auditing.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
@EntityScan(basePackages = "com.lms.entity")
public class JpaConfig {
    // Spring Boot auto-configuration will handle EntityManagerFactory creation
    // This class is here for future customizations like JPA event listeners,
    // custom entity scanning, or transaction management customizations
}
