package com.lms.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.lms.ui.service.AuthenticationService;
import com.lms.ui.service.ApiClient;
import com.lms.ui.model.User;
import com.lms.ui.model.UserRole;

import java.io.IOException;

/**
 * Login Controller - Handles user authentication with backend API.
 * Manages both login and registration with proper error handling and WebSocket
 * setup.
 * Uses ID-based authentication (Student ID or TA ID).
 */
public class LoginController {

    // Login fields
    @FXML
    private TextField idField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberMeCheckbox;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorLabel;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Button goToRegisterButton;
    @FXML
    private Label backendStatusLabel;

    @FXML
    public void initialize() {
        setupUI();
        checkBackendConnection();
        attemptAutoLogin();
    }

    /**
     * Attempt to auto-login using remembered credentials.
     */
    private void attemptAutoLogin() {
        Thread autoLoginThread = new Thread(() -> {
            try {
                User user = AuthenticationService.autoLogin();
                if (user != null) {
                    javafx.application.Platform.runLater(() -> {
                        try {
                            navigateToDashboard(user);
                        } catch (IOException e) {
                            System.err.println("Auto-login dashboard error: " + e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println("Auto-login not available: " + e.getMessage());
            }
        });
        autoLoginThread.setDaemon(true);
        autoLoginThread.start();
    }

    /**
     * Setup UI elements and event handlers.
     */
    private void setupUI() {
        loadingIndicator.setVisible(false);

        // Login button handler
        loginButton.setOnAction(e -> handleLogin());

        // Go to register page handler
        goToRegisterButton.setOnAction(e -> handleGoToRegister());

        // Allow Enter key in login fields
        idField.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());
    }

    /**
     * Navigate to register page.
     */
    @FXML
    private void handleGoToRegister() {
        try {
            Stage stage = (Stage) goToRegisterButton.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 750);
            stage.setScene(scene);
            stage.setTitle("Lab Management System - Register");
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to load register page: " + e.getMessage());
            System.err.println("Navigation error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if backend is available.
     */
    private void checkBackendConnection() {
        Thread backendCheckThread = new Thread(() -> {
            try {
                boolean available = ApiClient.isBackendAvailable();
                javafx.application.Platform.runLater(() -> {
                    if (available) {
                        backendStatusLabel.setText("✓ Backend Connected");
                        backendStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else {
                        backendStatusLabel.setText("✗ Backend Offline");
                        backendStatusLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                        showErrorAlert("Backend Connection Failed",
                                "Cannot connect to backend server at http://localhost:8080\n\n" +
                                        "Make sure the Spring Boot application is running:\n" +
                                        "  mvn spring-boot:run\n\n" +
                                        "Or with Docker:\n" +
                                        "  docker-compose up");
                    }
                });
            } catch (Exception e) {
                System.err.println("Backend check error: " + e.getMessage());
            }
        });
        backendCheckThread.setDaemon(true);
        backendCheckThread.start();
    }

    /**
     * Handle login button click with Remember Me support.
     * Supports login with either username or Student/TA ID.
     */
    @FXML
    private void handleLogin() {
        String username = idField.getText().trim();
        String password = passwordField.getText();
        boolean rememberMe = rememberMeCheckbox.isSelected();

        if (username.isEmpty() || password.isEmpty()) {
            showError(errorLabel, "Username/ID and password are required");
            return;
        }

        // Show loading indicator
        loadingIndicator.setVisible(true);
        loginButton.setDisable(true);
        errorLabel.setText("");

        // Login in background thread
        Thread loginThread = new Thread(() -> {
            try {
                // Backend supports login with either username or Student/TA ID
                User user = AuthenticationService.login(username, password, rememberMe);

                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loginButton.setDisable(false);

                    if (user != null) {
                        try {
                            // Clear sensitive fields
                            passwordField.clear();
                            navigateToDashboard(user);
                        } catch (IOException e) {
                            System.err.println("Failed to load dashboard: " + e.getMessage());
                            e.printStackTrace();
                            showError(errorLabel, "Failed to load dashboard: " + e.getMessage());
                        } catch (Exception e) {
                            System.err.println("Unexpected error loading dashboard: " + e.getMessage());
                            e.printStackTrace();
                            showError(errorLabel, "Unexpected error: " + e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loginButton.setDisable(false);

                    // Show user-friendly error message
                    String errorMessage = e.getMessage();
                    if (errorMessage == null) {
                        errorMessage = "An unknown error occurred";
                    }

                    showError(errorLabel, errorMessage);

                    // Also log to console for debugging
                    System.err.println("Login error: " + errorMessage);
                    e.printStackTrace();
                });
            }
        });
        loginThread.setDaemon(true);
        loginThread.start();
    }

    /**
     * Clear login fields.
     */
    private void clearLoginFields() {
        idField.clear();
        passwordField.clear();
        rememberMeCheckbox.setSelected(false);
        errorLabel.setText("");
    }

    /**
     * Navigate to appropriate dashboard based on user role.
     */
    private void navigateToDashboard(User user) throws IOException {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        FXMLLoader fxmlLoader;

        if (user.getRole() == UserRole.STUDENT) {
            fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/student-dashboard.fxml"));
        } else {
            fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ta-dashboard.fxml"));
        }

        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);

        // Pass user to dashboard controller
        Object controller = fxmlLoader.getController();
        if (controller instanceof DashboardController) {
            ((DashboardController) controller).setCurrentUser(user);
        }

        stage.setScene(scene);
        stage.setTitle("Lab Management System - " + user.getRole().getDisplayName());
    }

    /**
     * Show error message in label.
     */
    private void showError(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: #d32f2f;");
    }

    /**
     * Show error dialog.
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}