package com.lms.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.lms.ui.service.AuthenticationService;
import com.lms.ui.model.User;
import com.lms.ui.model.UserRole;

import java.io.IOException;

/**
 * Register Controller - Handles user registration for both Students and TAs.
 * Provides separate registration flows with ID format validation.
 */
public class RegisterController {

    @FXML
    private TextField idField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button registerButton;
    @FXML
    private Button studentTabButton;
    @FXML
    private Button taTabButton;
    @FXML
    private Button backToLoginButton;
    @FXML
    private Label errorLabel;
    @FXML
    private Label idLabel;
    @FXML
    private Label idHintLabel;
    @FXML
    private ProgressIndicator loadingIndicator;

    private UserRole selectedRole = UserRole.STUDENT;

    @FXML
    public void initialize() {
        setupUI();
    }

    /**
     * Setup UI elements and event handlers.
     */
    private void setupUI() {
        loadingIndicator.setVisible(false);

        // Tab button handlers
        studentTabButton.setOnAction(e -> switchToStudentTab());
        taTabButton.setOnAction(e -> switchToTATab());

        // Register button handler
        registerButton.setOnAction(e -> handleRegister());

        // Back to login button handler
        backToLoginButton.setOnAction(e -> handleBackToLogin());

        // Allow Enter key in fields
        idField.setOnAction(e -> handleRegister());
        usernameField.setOnAction(e -> handleRegister());
        emailField.setOnAction(e -> handleRegister());
        passwordField.setOnAction(e -> handleRegister());
        confirmPasswordField.setOnAction(e -> handleRegister());
    }

    /**
     * Switch to Student registration tab.
     */
    @FXML
    private void switchToStudentTab() {
        selectedRole = UserRole.STUDENT;
        updateTabStyles();
        idLabel.setText("Student ID *");
        idHintLabel.setText("Your unique student identifier (e.g., STU001)");
        idField.setPromptText("e.g., STU001");
        errorLabel.setText("");
        clearFields();
    }

    /**
     * Switch to TA registration tab.
     */
    @FXML
    private void switchToTATab() {
        selectedRole = UserRole.TA;
        updateTabStyles();
        idLabel.setText("TA ID *");
        idHintLabel.setText("Your unique TA identifier (e.g., TA001)");
        idField.setPromptText("e.g., TA001");
        errorLabel.setText("");
        clearFields();
    }

    /**
     * Update tab button styles based on selected role.
     */
    private void updateTabStyles() {
        if (selectedRole == UserRole.STUDENT) {
            studentTabButton.setStyle(
                    "-fx-font-size: 12; -fx-padding: 10 20; -fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 3;");
            taTabButton.setStyle(
                    "-fx-font-size: 12; -fx-padding: 10 20; -fx-background-color: #cccccc; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-border-radius: 3;");
        } else {
            studentTabButton.setStyle(
                    "-fx-font-size: 12; -fx-padding: 10 20; -fx-background-color: #cccccc; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-border-radius: 3;");
            taTabButton.setStyle(
                    "-fx-font-size: 12; -fx-padding: 10 20; -fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 3;");
        }
    }

    /**
     * Handle registration button click.
     * Validates input and registers user based on selected role.
     */
    @FXML
    private void handleRegister() {
        String id = idField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate input
        if (id.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("All fields are required");
            return;
        }

        if (!isValidIdFormat(id, selectedRole)) {
            String format = selectedRole == UserRole.STUDENT ? "STU###" : "TA###";
            showError("ID must be in format: " + format + " (e.g., "
                    + (selectedRole == UserRole.STUDENT ? "STU001" : "TA001") + ")");
            return;
        }

        if (!isValidUsername(username)) {
            showError("Username must be 3-20 characters, letters, numbers, and underscores only");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Please enter a valid email address");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }

        // Show loading indicator
        loadingIndicator.setVisible(true);
        registerButton.setDisable(true);
        errorLabel.setText("");

        // Register in background thread
        Thread registerThread = new Thread(() -> {
            try {
                User user = new User();
                user.setUsername(username); // Use custom username for backend
                user.setStudentId(id); // Keep ID for record
                user.setEmail(email);
                user.setPassword(password);
                user.setRole(selectedRole);

                User registeredUser = AuthenticationService.register(user);

                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    registerButton.setDisable(false);

                    if (registeredUser != null) {
                        String roleName = selectedRole == UserRole.STUDENT ? "Student" : "TA";
                        showInfoDialog(
                                roleName + " account created successfully!\n\nPlease log in with your username and password.",
                                "Registration Successful");
                        handleBackToLogin();
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    registerButton.setDisable(false);

                    String errorMessage = e.getMessage();
                    if (errorMessage == null) {
                        errorMessage = "An unknown error occurred";
                    }

                    showError(errorMessage);
                    System.err.println("Registration error: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
        registerThread.setDaemon(true);
        registerThread.start();
    }

    /**
     * Validate ID format based on role.
     * Student IDs: STU###
     * TA IDs: TA###
     */
    private boolean isValidIdFormat(String id, UserRole role) {
        if (role == UserRole.STUDENT) {
            return id.matches("^STU\\d{3,}$");
        } else {
            return id.matches("^TA\\d{3,}$");
        }
    }

    /**
     * Validate username format.
     * 3-20 characters, letters, numbers, and underscores only.
     */
    private boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    /**
     * Navigate back to login page.
     */
    @FXML
    private void handleBackToLogin() {
        try {
            Stage stage = (Stage) backToLoginButton.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 700);
            stage.setScene(scene);
            stage.setTitle("Lab Management System - Login");
        } catch (IOException e) {
            showErrorDialog("Failed to load login page: " + e.getMessage(), "Navigation Error");
            System.err.println("Navigation error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show error message in label.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #d32f2f;");
    }

    /**
     * Show info dialog.
     */
    private void showInfoDialog(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error dialog.
     */
    private void showErrorDialog(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Clear all fields.
     */
    private void clearFields() {
        idField.clear();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        errorLabel.setText("");
    }
}
