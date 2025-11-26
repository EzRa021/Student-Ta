package com.lms.ui.service;

/**
 * Network retry policy with exponential backoff for resilient API calls.
 * Automatically retries transient network failures (connection errors,
 * timeouts).
 */
public class NetworkRetryPolicy {

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_DELAY = 1000; // 1 second
    private static final double BACKOFF_MULTIPLIER = 2.0;

    /**
     * Execute operation with automatic retry on transient failures.
     */
    public static <T> T executeWithRetry(ApiOperation<T> operation, String operationName)
            throws Exception {

        int attempt = 0;
        long delay = INITIAL_DELAY;
        Exception lastException = null;

        while (attempt < MAX_RETRIES) {
            try {
                System.out.println("DEBUG: Executing operation: " + operationName + " (attempt " + (attempt + 1) + "/"
                        + MAX_RETRIES + ")");
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                attempt++;

                // Check if error is retryable
                if (!isRetryableError(e)) {
                    throw e; // Non-retryable error, throw immediately
                }

                if (attempt < MAX_RETRIES) {
                    System.out.println("WARN: Network error on attempt " + attempt + "/" + MAX_RETRIES +
                            ": " + e.getMessage() + ". Retrying in " + delay + "ms...");

                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ApiClient.ApiException("Operation interrupted: " + operationName);
                    }

                    delay = (long) (delay * BACKOFF_MULTIPLIER); // Exponential backoff
                }
            }
        }

        throw new ApiClient.ApiException(
                "Network operation failed after " + MAX_RETRIES + " retries: " + operationName,
                lastException);
    }

    /**
     * Check if error is retryable (transient).
     */
    private static boolean isRetryableError(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "";

        // Retryable errors: connection issues, timeouts, network problems
        return message.contains("Connection refused") ||
                message.contains("Connection reset") ||
                message.contains("Connection timeout") ||
                message.contains("Read timed out") ||
                message.contains("SocketTimeoutException") ||
                message.contains("ConnectException") ||
                message.contains("IOException") ||
                message.contains("Unable to resolve host") ||
                e instanceof java.net.ConnectException ||
                e instanceof java.net.SocketTimeoutException ||
                e instanceof java.io.IOException;
    }

    /**
     * Functional interface for retryable operations.
     */
    @FunctionalInterface
    public interface ApiOperation<T> {
        T execute() throws Exception;
    }
}
