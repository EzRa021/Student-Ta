package com.lms.ui.service;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP API Client for communicating with Spring Boot backend.
 * Handles all REST API calls with timeout, retry logic, error handling and
 * token refresh.
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)) // ✅ CONNECTION TIMEOUT
            .version(HttpClient.Version.HTTP_2)
            .build();
    private static final Gson gson = new Gson();
    private static String authToken = null;
    private static String refreshToken = null;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30); // ✅ REQUEST TIMEOUT

    /**
     * Set authentication token and refresh token for subsequent requests.
     */
    public static void setAuthToken(String token) {
        authToken = token;
    }

    /**
     * Set refresh token for token refresh mechanism.
     */
    public static void setRefreshToken(String token) {
        refreshToken = token;
    }

    /**
     * Get current authentication token.
     */
    public static String getAuthToken() {
        return authToken;
    }

    /**
     * Get current refresh token.
     */
    public static String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Make POST request to backend with retry and timeout support.
     */
    public static String post(String endpoint, Object body) throws Exception {
        return NetworkRetryPolicy.executeWithRetry(
                () -> executePost(endpoint, body),
                "POST " + endpoint);
    }

    /**
     * Internal POST execution with timeout.
     */
    private static String executePost(String endpoint, Object body) throws Exception {
        String jsonBody = gson.toJson(body);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .timeout(REQUEST_TIMEOUT) // ✅ ADD TIMEOUT
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        // Try token refresh if unauthorized
        if (response.statusCode() == 401 && refreshToken != null) {
            try {
                refreshAuthToken();
                // Retry request with new token
                requestBuilder = HttpRequest.newBuilder()
                        .uri(new URI(BASE_URL + endpoint))
                        .header("Content-Type", "application/json")
                        .timeout(REQUEST_TIMEOUT) // ✅ ADD TIMEOUT
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
                if (authToken != null) {
                    requestBuilder.header("Authorization", "Bearer " + authToken);
                }
                request = requestBuilder.build();
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                throw new ApiException("Token refresh failed", new TokenExpiredException());
            }
        }

        if (response.statusCode() == 401) {
            // Try to extract error message from response body
            try {
                com.google.gson.JsonObject errorJson = gson.fromJson(response.body(), com.google.gson.JsonObject.class);
                String errorMessage = errorJson.has("message") ? errorJson.get("message").getAsString()
                        : "Invalid username or password";
                throw new ApiException("401: " + errorMessage);
            } catch (Exception e) {
                throw new ApiException("401: Unauthorized - Invalid credentials");
            }
        }

        if (response.statusCode() >= 400) {
            // Try to extract error message from response
            try {
                com.google.gson.JsonObject errorJson = gson.fromJson(response.body(), com.google.gson.JsonObject.class);
                String errorMessage = errorJson.has("message") ? errorJson.get("message").getAsString()
                        : response.body();
                throw new ApiException("API Error " + response.statusCode() + ": " + errorMessage);
            } catch (Exception e) {
                throw new ApiException("API Error: " + response.statusCode() + " - " + response.body());
            }
        }

        return response.body();
    }

    /**
     * Make GET request to backend with retry and timeout support.
     */
    public static String get(String endpoint) throws Exception {
        return NetworkRetryPolicy.executeWithRetry(
                () -> executeGet(endpoint),
                "GET " + endpoint);
    }

    /**
     * Internal GET execution with timeout.
     */
    private static String executeGet(String endpoint) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .timeout(REQUEST_TIMEOUT) // ✅ ADD TIMEOUT
                .GET();

        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        // Try token refresh if unauthorized
        if (response.statusCode() == 401 && refreshToken != null) {
            try {
                refreshAuthToken();
                // Retry request with new token
                requestBuilder = HttpRequest.newBuilder()
                        .uri(new URI(BASE_URL + endpoint))
                        .header("Content-Type", "application/json")
                        .timeout(REQUEST_TIMEOUT) // ✅ ADD TIMEOUT
                        .GET();
                if (authToken != null) {
                    requestBuilder.header("Authorization", "Bearer " + authToken);
                }
                request = requestBuilder.build();
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                throw new ApiException("Token refresh failed", new TokenExpiredException());
            }
        }

        if (response.statusCode() == 401) {
            // Try to extract error message from response body
            try {
                com.google.gson.JsonObject errorJson = gson.fromJson(response.body(), com.google.gson.JsonObject.class);
                String errorMessage = errorJson.has("message") ? errorJson.get("message").getAsString()
                        : "Invalid username or password";
                throw new ApiException("401: " + errorMessage);
            } catch (Exception e) {
                throw new ApiException("401: Unauthorized - Invalid credentials");
            }
        }

        if (response.statusCode() >= 400) {
            try {
                com.google.gson.JsonObject errorJson = gson.fromJson(response.body(), com.google.gson.JsonObject.class);
                String errorMessage = errorJson.has("message") ? errorJson.get("message").getAsString()
                        : response.body();
                throw new ApiException("API Error " + response.statusCode() + ": " + errorMessage);
            } catch (Exception e) {
                throw new ApiException("API Error: " + response.statusCode() + " - " + response.body());
            }
        }

        return response.body();
    }

    /**
     * Make PUT request to backend with retry and timeout support.
     */
    public static String put(String endpoint, Object body) throws Exception {
        return NetworkRetryPolicy.executeWithRetry(
                () -> executePut(endpoint, body),
                "PUT " + endpoint);
    }

    /**
     * Internal PUT execution with timeout.
     */
    private static String executePut(String endpoint, Object body) throws Exception {
        String jsonBody = gson.toJson(body);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .timeout(REQUEST_TIMEOUT) // ✅ ADD TIMEOUT
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        // Try token refresh if unauthorized
        if (response.statusCode() == 401 && refreshToken != null) {
            try {
                refreshAuthToken();
                // Retry request with new token
                requestBuilder = HttpRequest.newBuilder()
                        .uri(new URI(BASE_URL + endpoint))
                        .header("Content-Type", "application/json")
                        .timeout(REQUEST_TIMEOUT) // ✅ ADD TIMEOUT
                        .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
                if (authToken != null) {
                    requestBuilder.header("Authorization", "Bearer " + authToken);
                }
                request = requestBuilder.build();
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                throw new ApiException("Token refresh failed", new TokenExpiredException());
            }
        }

        if (response.statusCode() == 401) {
            // Try to extract error message from response body
            try {
                com.google.gson.JsonObject errorJson = gson.fromJson(response.body(), com.google.gson.JsonObject.class);
                String errorMessage = errorJson.has("message") ? errorJson.get("message").getAsString()
                        : "Invalid username or password";
                throw new ApiException("401: " + errorMessage);
            } catch (Exception e) {
                throw new ApiException("401: Unauthorized - Invalid credentials");
            }
        }

        if (response.statusCode() >= 400) {
            try {
                com.google.gson.JsonObject errorJson = gson.fromJson(response.body(), com.google.gson.JsonObject.class);
                String errorMessage = errorJson.has("message") ? errorJson.get("message").getAsString()
                        : response.body();
                throw new ApiException("API Error " + response.statusCode() + ": " + errorMessage);
            } catch (Exception e) {
                throw new ApiException("API Error: " + response.statusCode() + " - " + response.body());
            }
        }

        return response.body();
    }

    /**
     * Make DELETE request to backend with automatic token refresh on 401.
     */
    public static void delete(String endpoint) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .DELETE();

        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        // Try token refresh if unauthorized
        if (response.statusCode() == 401 && refreshToken != null) {
            try {
                refreshAuthToken();
                // Retry request with new token
                requestBuilder = HttpRequest.newBuilder()
                        .uri(new URI(BASE_URL + endpoint))
                        .header("Content-Type", "application/json")
                        .DELETE();
                if (authToken != null) {
                    requestBuilder.header("Authorization", "Bearer " + authToken);
                }
                request = requestBuilder.build();
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                throw new ApiException("Token refresh failed", new TokenExpiredException());
            }
        }

        if (response.statusCode() == 401) {
            // Try to extract error message from response body
            try {
                com.google.gson.JsonObject errorJson = gson.fromJson(response.body(), com.google.gson.JsonObject.class);
                String errorMessage = errorJson.has("message") ? errorJson.get("message").getAsString()
                        : "Invalid username or password";
                throw new ApiException("401: " + errorMessage);
            } catch (Exception e) {
                throw new ApiException("401: Unauthorized - Invalid credentials");
            }
        }

        if (response.statusCode() >= 400) {
            try {
                com.google.gson.JsonObject errorJson = gson.fromJson(response.body(), com.google.gson.JsonObject.class);
                String errorMessage = errorJson.has("message") ? errorJson.get("message").getAsString()
                        : response.body();
                throw new ApiException("API Error " + response.statusCode() + ": " + errorMessage);
            } catch (Exception e) {
                throw new ApiException("API Error: " + response.statusCode() + " - " + response.body());
            }
        }
    }

    /**
     * Refresh authentication token using refresh token.
     */
    private static void refreshAuthToken() throws Exception {
        if (refreshToken == null) {
            throw new TokenExpiredException();
        }

        com.google.gson.JsonObject refreshRequest = new com.google.gson.JsonObject();
        refreshRequest.addProperty("refreshToken", refreshToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + "/auth/refresh"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(refreshRequest)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new TokenExpiredException();
        }

        com.google.gson.JsonObject jsonResponse = gson.fromJson(response.body(), com.google.gson.JsonObject.class);
        String newAccessToken = jsonResponse.get("accessToken").getAsString();
        authToken = newAccessToken;
    }

    /**
     * Check if backend is reachable.
     */
    public static boolean isBackendAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL.replace("/api", "") + "/actuator/health"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Custom exception for API errors.
     */
    public static class ApiException extends Exception {
        public ApiException(String message) {
            super(message);
        }

        public ApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Exception to indicate token expiration.
     */
    public static class TokenExpiredException extends Exception {
        public TokenExpiredException() {
            super("Token expired");
        }
    }
}