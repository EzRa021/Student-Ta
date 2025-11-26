package com.lms.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

/**
 * Main JavaFX Application class for Lab Management System.
 * Entry point for the desktop GUI application.
 */
public class LabManagementApplication extends Application {

    private static final String APP_TITLE = "Lab Management System";
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 700;

    @Override
    public void start(Stage stage) throws IOException {
        // Load the login scene
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), WINDOW_WIDTH, WINDOW_HEIGHT);

        // Configure stage
        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        // Set application icon if available
        try {
            Image icon = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/images/app-icon.png")));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("App icon not found, skipping...");
        }

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}