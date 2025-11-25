package com.lms.ui.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lms.ui.model.User;
import com.lms.ui.model.UserRole;
import java.util.prefs.Preferences;
import java.util.UUID;

/**
 * Authentication service that connects to Spring Boot backend API.
 * Handles user login, registration, token management, and Remember Me
 * functionality.
 */
public class AuthenticationService {

    private static final Gson gson = new Gson();
    private static User currentUser = null;
    private static final Preferences prefs = Preferences.userNodeForPackage(AuthenticationService.class);
    private static final String PREF_USERNAME = "remember_me_username";
    private static final String PREF_AUTH_TOKEN = "remember_me_auth_token";
    private static final String PREF_REFRESH_TOKEN = "remember_me_refresh_token";

    /**
     * Login user with backend API.
     * Supports Remember Me functionality.
     */
    public static User login(String username, String password, boolean rememberMe) throws Exception {
        // Check if backend is available
        if (!ApiClient.isBackendAvailable()) {
            throw new Exception(
                    "Backend server is not available. Make sure the Spring Boot application is running on http://localhost:8080");
        }

        JsonObject loginRequest = new JsonObject();
        loginRequest.addProperty("username", username);
        loginRequest.addProperty("password", password);

        try {
            String response = ApiClient.post("/auth/login", loginRequest);
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

            // Extract token and user data
            String accessToken = jsonResponse.get("accessToken").getAsString();
            String refreshTokenResponse = jsonResponse.has("refreshToken")
                    ? jsonResponse.get("refreshToken").getAsString()
                    : null;
            String userId = jsonResponse.get("userId").getAsString();
            String userUsername = jsonResponse.get("username").getAsString();

            String roleString = jsonResponse.getAsJsonArray("roles").get(0).getAsString();
            UserRole role = UserRole.valueOf(roleString);

            // Save tokens for future requests
            ApiClient.setAuthToken(accessToken);
            if (refreshTokenResponse != null) {
                ApiClient.setRefreshToken(refreshTokenResponse);
            }

            // Handle Remember Me
            if (rememberMe) {
                prefs.put(PREF_USERNAME, username);
                prefs.put(PREF_AUTH_TOKEN, accessToken);
                if (refreshTokenResponse != null) {
                    prefs.put(PREF_REFRESH_TOKEN, refreshTokenResponse);
                }
            } else {
                clearRememberedCredentials();
            }

            // Create user object
            User user = new User();
            user.setId(userId);
            user.setUsername(userUsername);
            user.setRole(role);
            user.setSessionId(UUID.randomUUID().toString()); // Generate unique session ID

            currentUser = user;

            // Connect WebSocket for real-time updates
            WebSocketManager wsManager = WebSocketManager.getInstance();
            if (wsManager != null) {
                wsManager.connect(accessToken,
                        () -> System.out.println("WebSocket connected for user: " + userUsername));
            }

            return user;
        } catch (ApiClient.ApiException e) {
            // Extract user-friendly error message
            String errorMsg = e.getMessage();

            if (errorMsg.contains("401") || errorMsg.contains("Unauthorized") || errorMsg.contains("Bad credentials")) {
                throw new Exception("Invalid username or password. Please check and try again.");
            } else if (errorMsg.contains("400") || errorMsg.contains("Bad Request")) {
                throw new Exception("Invalid login request. Please enter valid credentials.");
            } else if (errorMsg.contains("500") || errorMsg.contains("Internal Server")) {
                throw new Exception("Server error. Please try again later.");
            } else if (errorMsg.contains("Connection refused")) {
                throw new Exception("Cannot connect to backend server. Make sure it's running.");
            }

            throw new Exception("Login failed: " + errorMsg);
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")
                    || e.getMessage().contains("unable to find valid certification path")) {
                throw new Exception(
                        "Cannot connect to backend server. Make sure it's running on http://localhost:8080");
            }
            throw new Exception("Login error: " + e.getMessage());
        }
    }

    /**
     * Attempt auto-login using remembered credentials.
     */
    public static User autoLogin() throws Exception {
        String username = prefs.get(PREF_USERNAME, null);
        String authToken = prefs.get(PREF_AUTH_TOKEN, null);
        String refreshToken = prefs.get(PREF_REFRESH_TOKEN, null);

        if (username == null || authToken == null) {
            return null; // No remembered credentials
        }

        try {
            // Verify token is still valid by making a test request
            ApiClient.setAuthToken(authToken);
            if (refreshToken != null) {
                ApiClient.setRefreshToken(refreshToken);
            }

            // Test token by getting user info
            String response = ApiClient.get("/auth/me");
            JsonObject userInfo = gson.fromJson(response, JsonObject.class);

            String userId = userInfo.get("id").getAsString();
            String userUsername = userInfo.get("username").getAsString();
            String roleString = userInfo.getAsJsonArray("roles").get(0).getAsString();
            UserRole role = UserRole.valueOf(roleString);

            // Create user object
            User user = new User();
            user.setId(userId);
            user.setUsername(userUsername);
            user.setRole(role);
            user.setSessionId(UUID.randomUUID().toString()); // Generate unique session ID

            currentUser = user;

            // Connect WebSocket for real-time updates
            WebSocketManager wsManager = WebSocketManager.getInstance();
            if (wsManager != null) {
                wsManager.connect(authToken,
                        () -> System.out.println("WebSocket auto-connected for user: " + userUsername));
            }

            return user;
        } catch (Exception e) {
            // Token expired or invalid, clear remembered credentials
            clearRememberedCredentials();
            return null;
        }
    }

    /**
     * Clear remembered credentials from storage.
     */
    public static void clearRememberedCredentials() {
        prefs.remove(PREF_USERNAME);
        prefs.remove(PREF_AUTH_TOKEN);
        prefs.remove(PREF_REFRESH_TOKEN);
    }

    /**
     * Register a new user with backend API.
     * Calls /auth/register for students and /auth/register-ta for TAs.
     */
    public static User register(User user) throws Exception {
        // Check if backend is available
        if (!ApiClient.isBackendAvailable()) {
            throw new Exception(
                    "Backend server is not available. Make sure the Spring Boot application is running on http://localhost:8080");
        }

        JsonObject registerRequest = new JsonObject();
        registerRequest.addProperty("username", user.getUsername());
        registerRequest.addProperty("email", user.getEmail());
        registerRequest.addProperty("password", user.getPassword());
        registerRequest.addProperty("role", user.getRole().name());
        if (user.getStudentId() != null) {
            registerRequest.addProperty("studentId", user.getStudentId());
        }

        try {
            // Choose endpoint based on role
            String endpoint = user.getRole() == UserRole.TA ? "/auth/register-ta" : "/auth/register";
            String response = ApiClient.post(endpoint, registerRequest);
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

            // Extract registered user data
            String userId = jsonResponse.get("userId").getAsString();
            user.setId(userId);
            // Role is already set from user object

            return user;
        } catch (ApiClient.ApiException e) {
            String errorMsg = e.getMessage();

            if (errorMsg.contains("already exists")) {
                throw new Exception("Username or email already exists. Please choose a different one.");
            } else if (errorMsg.contains("409")) {
                throw new Exception("User already registered. Please try a different username or email.");
            } else if (errorMsg.contains("400")) {
                throw new Exception("Invalid registration data. Please check all fields.");
            } else if (errorMsg.contains("500")) {
                throw new Exception("Server error during registration. Please try again later.");
            }

            throw new Exception("Registration failed: " + errorMsg);
        } catch (Exception e) {
            throw new Exception("Registration error: " + e.getMessage());
        }
    }

    /**
     * Get current logged-in user.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Get current authentication token.
     */
    public static String getAuthToken() {
        return ApiClient.getAuthToken();
    }

    /**
     * Logout current user.
     * Ensures complete session cleanup including WebSocket disconnection and token
     * removal.
     */
    public static void logout() {
        try {
            System.out.println("[AuthenticationService] Starting logout process...");

            // Disconnect WebSocket first
            try {
                WebSocketManager wsManager = WebSocketManager.getInstance();
                if (wsManager != null) {
                    System.out.println("[AuthenticationService] Disconnecting WebSocket...");
                    wsManager.disconnect();
                    System.out.println("[AuthenticationService] WebSocket disconnected");
                }
            } catch (Exception e) {
                System.err.println("[AuthenticationService] Error disconnecting WebSocket: " + e.getMessage());
                // Continue with logout even if WebSocket disconnect fails
            }

            // Clear authentication data
            currentUser = null;
            ApiClient.setAuthToken(null);
            ApiClient.setRefreshToken(null);
            clearRememberedCredentials();

            System.out.println("[AuthenticationService] Logout completed successfully");
        } catch (Exception e) {
            System.err.println("[AuthenticationService] Error during logout: " + e.getMessage());
            e.printStackTrace();
            // Ensure cleanup happens even if there's an exception
            currentUser = null;
            try {
                ApiClient.setAuthToken(null);
                ApiClient.setRefreshToken(null);
            } catch (Exception ex) {
                System.err.println("[AuthenticationService] Error clearing tokens: " + ex.getMessage());
            }
        }
    }

    /**
     * Refresh authentication token.
     */
    public static String refreshToken(String refreshToken) throws Exception {
        JsonObject refreshRequest = new JsonObject();
        refreshRequest.addProperty("refreshToken", refreshToken);

        try {
            String response = ApiClient.post("/auth/refresh", refreshRequest);
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

            String newAccessToken = jsonResponse.get("accessToken").getAsString();
            ApiClient.setAuthToken(newAccessToken);

            return newAccessToken;
        } catch (Exception e) {
            throw new Exception("Token refresh failed: " + e.getMessage());
        }
    }
}