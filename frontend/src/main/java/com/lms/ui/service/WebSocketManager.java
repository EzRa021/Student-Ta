package com.lms.ui.service;

import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * WebSocket client for real-time updates from Spring Boot backend.
 * Handles connection, subscription, and event broadcasting.
 * IMPROVED: Fixed singleton reusability issue and added proper connection state
 * management.
 */
public class WebSocketManager extends WebSocketClient {

    private static WebSocketManager instance;
    private static final String WS_URL = "ws://localhost:8080/ws";
    private final Gson gson = new Gson();
    private String authToken;
    private Map<String, Consumer<String>> eventListeners = new HashMap<>();
    private boolean isSubscribed = false;
    private boolean isConnecting = false;

    /**
     * Private constructor for singleton pattern.
     */
    private WebSocketManager(String url) throws Exception {
        super(new URI(url));
    }

    /**
     * Get singleton instance.
     * IMPORTANT: Creates a new instance for each connection attempt after
     * disconnection.
     */
    public static WebSocketManager getInstance() {
        // Always return the current instance if it's open
        if (instance != null && instance.isOpen()) {
            return instance;
        }

        // Create a new instance if needed
        try {
            instance = new WebSocketManager(WS_URL);
            return instance;
        } catch (Exception e) {
            System.err.println("Failed to create WebSocket manager: " + e.getMessage());
            return null;
        }
    }

    /**
     * Connect to WebSocket with authentication token.
     * Fixed to prevent reuse issues by creating new instance on reconnect.
     */
    public void connect(String token, Runnable onConnected) {
        if (isConnecting) {
            System.err.println("WebSocket connection already in progress");
            return;
        }

        this.authToken = token;
        final Runnable callback = onConnected; // Make effectively final for lambda

        try {
            // Close existing connection if open
            if (isOpen()) {
                try {
                    close();
                    Thread.sleep(500); // Wait for cleanup
                } catch (Exception e) {
                    System.err.println("Error closing previous connection: " + e.getMessage());
                }
            }

            isConnecting = true;
            super.connect();

            // Wait for connection in background thread
            new Thread(() -> {
                int attempts = 0;
                while (!isOpen() && attempts < 30) {
                    try {
                        Thread.sleep(100);
                        attempts++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                isConnecting = false;

                if (isOpen()) {
                    System.out.println(
                            "WebSocket connected for user: " + token.substring(0, Math.min(10, token.length())));
                    Platform.runLater(callback);
                    subscribeToUpdates();
                } else {
                    System.err.println("WebSocket connection failed after 30 attempts");
                }
            }).start();

        } catch (IllegalStateException e) {
            isConnecting = false;
            System.err.println("WebSocket client state error (may need reconnect): " + e.getMessage());
            // Reset instance for next connection attempt
            instance = null;
        } catch (Exception e) {
            isConnecting = false;
            System.err.println("WebSocket connection failed: " + e.getMessage());
        }
    }

    /**
     * Subscribe to request updates.
     */
    private void subscribeToUpdates() {
        if (isOpen() && !isSubscribed) {
            try {
                // Send STOMP CONNECT frame with token
                String connectFrame = "CONNECT\nlogin:guest\npasscode:guest\nAuthorization:Bearer " + authToken
                        + "\n\n\u0000";
                send(connectFrame);

                // Subscribe to request topic for TAs
                String subscribeTA = "SUBSCRIBE\nid:sub-1\ndestination:/topic/requests\n\n\u0000";
                send(subscribeTA);

                // Subscribe to user queue for students
                String subscribeStudent = "SUBSCRIBE\nid:sub-2\ndestination:/user/queue/requests\n\n\u0000";
                send(subscribeStudent);

                isSubscribed = true;
            } catch (Exception e) {
                System.err.println("Error subscribing to updates: " + e.getMessage());
            }
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("WebSocket connection opened");
    }

    @Override
    public void onMessage(String message) {
        try {
            // Parse STOMP message
            if (message.contains("MESSAGE")) {
                String[] lines = message.split("\n");
                String payload = null;

                for (int i = 0; i < lines.length; i++) {
                    if (lines[i].isEmpty() && i + 1 < lines.length) {
                        payload = lines[i + 1].trim();
                        break;
                    }
                }

                if (payload != null && !payload.isEmpty()) {
                    final String finalPayload = payload; // Make effectively final
                    try {
                        JsonObject jsonMessage = gson.fromJson(payload, JsonObject.class);
                        if (jsonMessage.has("eventType")) {
                            String eventType = jsonMessage.get("eventType").getAsString();

                            // Notify listeners on JavaFX thread
                            Platform.runLater(() -> {
                                Consumer<String> listener = eventListeners.get(eventType);
                                if (listener != null) {
                                    try {
                                        listener.accept(finalPayload);
                                    } catch (Exception e) {
                                        System.err.println("Error in event listener: " + e.getMessage());
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing WebSocket JSON: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing WebSocket message: " + e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket closed: " + reason);
        isSubscribed = false;
        isConnecting = false;
        // Reset instance so next connection creates a new client
        instance = null;
    }

    @Override
    public void onError(Exception ex) {
        if (ex != null) {
            System.err.println("WebSocket error: " + ex.getMessage());
        } else {
            System.err.println("WebSocket error: unknown error");
        }
    }

    /**
     * Register listener for specific event type.
     */
    public void addEventListener(String eventType, Consumer<String> listener) {
        if (eventType != null && listener != null) {
            eventListeners.put(eventType, listener);
        }
    }

    /**
     * Remove listener for event type.
     */
    public void removeEventListener(String eventType) {
        if (eventType != null) {
            eventListeners.remove(eventType);
        }
    }

    /**
     * Disconnect WebSocket.
     * Ensures complete cleanup of WebSocket resources.
     */
    public void disconnect() {
        try {
            System.out.println("[WebSocketManager] Disconnecting WebSocket...");

            // Clear event listeners
            eventListeners.clear();
            isSubscribed = false;
            isConnecting = false;

            // Close the connection
            if (isOpen()) {
                try {
                    close();
                    System.out.println("[WebSocketManager] WebSocket connection closed");
                    // Wait a bit for close to complete
                    Thread.sleep(200);
                } catch (Exception e) {
                    System.err.println("[WebSocketManager] Error closing WebSocket connection: " + e.getMessage());
                }
            } else {
                System.out.println("[WebSocketManager] WebSocket was not open");
            }

            // Reset instance
            instance = null;
            authToken = null;

            System.out.println("[WebSocketManager] WebSocket disconnection completed");
        } catch (Exception e) {
            System.err.println("[WebSocketManager] Error during disconnect: " + e.getMessage());
            // Ensure cleanup happens even on error
            isSubscribed = false;
            isConnecting = false;
            instance = null;
            authToken = null;
        }
    }
}