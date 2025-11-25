package com.lms.ui.controller;

import com.lms.ui.model.User;
import com.lms.ui.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Base Dashboard Controller
 * Abstract class that provides common functionality for both
 * StudentDashboardController
 * and TADashboardController.
 */
public abstract class DashboardController {

    protected User currentUser;

    @FXML
    protected Label userGreetingLabel;

    /**
     * Set the current user and initialize dashboard.
     */
    public void setCurrentUser(User user) {
        try {
            System.out.println("[DashboardController] Setting current user: " + user.getUsername() + " (Role: "
                    + user.getRole() + ")");
            this.currentUser = user;
            if (userGreetingLabel != null) {
                userGreetingLabel
                        .setText("Welcome, " + user.getUsername() + " (" + user.getRole().getDisplayName() + ")");
                System.out.println("[DashboardController] User greeting label updated");
            } else {
                System.err.println("[DashboardController] WARNING: userGreetingLabel is null!");
            }
            System.out.println("[DashboardController] Calling initializeDashboard()...");
            initializeDashboard();
            System.out.println("[DashboardController] Dashboard initialization completed successfully");
        } catch (Exception e) {
            System.err.println("[DashboardController] ERROR in setCurrentUser: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Initialize dashboard - must be implemented by subclasses.
     */
    protected abstract void initializeDashboard();

    /**
     * Handle logout button click.
     */
    @FXML
    protected void handleLogout() throws IOException {
        try {
            System.out.println("[DashboardController] Logging out user: "
                    + (currentUser != null ? currentUser.getUsername() : "Unknown"));

            // Perform logout cleanup
            AuthenticationService.logout();

            System.out.println("[DashboardController] Logout successful");
        } catch (Exception e) {
            System.err.println("[DashboardController] Error during logout: " + e.getMessage());
            e.printStackTrace();
            // Continue with navigation to login even if logout fails
        }

        // Navigate to login screen
        try {
            Stage stage = (Stage) userGreetingLabel.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
            stage.setScene(scene);
            stage.setTitle("Lab Management System - Login");

            System.out.println("[DashboardController] Navigated to login screen");
        } catch (Exception e) {
            System.err.println("[DashboardController] Error navigating to login: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to navigate to login screen", e);
        }
    }

    /**
     * Get current user.
     */
    public User getCurrentUser() {
        return currentUser;
    }
}