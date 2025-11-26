package com.lms.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Properties;

/**
 * EnvironmentPostProcessor that loads .env file during application startup.
 * This allows using a .env file in development while environment variables work
 * in production.
 */
@Slf4j
public class EnvironmentConfig implements EnvironmentPostProcessor {

    private static final String ENV_FILE = ".env";
    private static final String DEV_PROFILE = "local";
    private static final String PROD_PROFILE = "prod";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            String activeProfile = environment.getProperty("spring.profiles.active", "local");

            // Only load .env file in development/local profiles
            if (activeProfile.contains(DEV_PROFILE) || activeProfile.contains("dev")) {
                loadEnvFile(environment);
            }

            // For production (Render), environment variables are already set by the
            // platform
            if (activeProfile.contains(PROD_PROFILE)) {
                log.info("Running in production mode - using system environment variables");
                validateProductionEnvironment();
            }
        } catch (Exception e) {
            log.warn("Could not load .env file: {}", e.getMessage());
        }
    }

    private void loadEnvFile(ConfigurableEnvironment environment) {
        File envFile = new File(ENV_FILE);

        if (!envFile.exists()) {
            log.warn(".env file not found at: {}", envFile.getAbsolutePath());
            log.info("Using default values or system environment variables");
            return;
        }

        try {
            // Load .env file using dotenv-java
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .filename(".env")
                    .load();

            // Convert dotenv values to Properties
            Properties props = new Properties();
            for (var entry : dotenv.entries()) {
                String key = entry.getKey();
                String value = entry.getValue();
                props.put(key, value);
                log.debug("Loaded from .env: {} (value hidden for security)", key);
            }

            // Add as highest priority property source
            PropertiesPropertySource propertySource = new PropertiesPropertySource("dotenv", props);
            environment.getPropertySources().addFirst(propertySource);

            log.info("Successfully loaded .env file for development environment");
        } catch (Exception e) {
            log.error("Error loading .env file: {}", e.getMessage(), e);
        }
    }

    private void validateProductionEnvironment() {
        String[] requiredProps = {
                "JWT_SECRET",
                "REFRESH_SECRET",
                "DB_URL",
                "DB_USER",
                "DB_PASS"
        };

        for (String prop : requiredProps) {
            String value = System.getenv(prop);
            if (value == null || value.trim().isEmpty()) {
                log.warn("Required production environment variable not set: {}", prop);
            }
        }
    }
}
