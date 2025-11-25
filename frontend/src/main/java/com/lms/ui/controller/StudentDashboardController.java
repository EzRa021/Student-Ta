package com.lms.ui.controller;

import com.lms.ui.model.Request;
import com.lms.ui.model.RequestStatus;
import com.lms.ui.service.RequestService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Student Dashboard Controller with sidebar navigation.
 * Supports multiple views: All Requests, Pending, Assigned, Answered, Create
 * Request, and Request Details.
 */
public class StudentDashboardController extends DashboardController {

    // Sidebar buttons
    @FXML
    private Button allRequestsBtn;
    @FXML
    private Button pendingRequestsBtn;
    @FXML
    private Button assignedRequestsBtn;
    @FXML
    private Button answeredRequestsBtn;
    @FXML
    private Button createRequestBtn;

    // Content pane and views
    @FXML
    private StackPane contentPane;
    @FXML
    private VBox allRequestsView;
    @FXML
    private VBox pendingRequestsView;
    @FXML
    private VBox assignedRequestsView;
    @FXML
    private VBox answeredRequestsView;
    @FXML
    private VBox createRequestView;
    @FXML
    private VBox requestDetailsView;

    // All Requests View
    @FXML
    private TableView<Request> allRequestsTable;
    @FXML
    private TableColumn<Request, String> allTitleColumn;
    @FXML
    private TableColumn<Request, String> allStatusColumn;
    @FXML
    private TableColumn<Request, String> allCreatedAtColumn;
    @FXML
    private TableColumn<Request, String> allTaColumn;
    @FXML
    private TableColumn<Request, Void> allActionsColumn;
    @FXML
    private Button refreshAllBtn;

    // Pending Requests View
    @FXML
    private TableView<Request> pendingRequestsTable;
    @FXML
    private TableColumn<Request, String> pendingTitleColumn;
    @FXML
    private TableColumn<Request, String> pendingCreatedAtColumn;
    @FXML
    private TableColumn<Request, String> pendingWaitTimeColumn;
    @FXML
    private TableColumn<Request, Void> pendingActionsColumn;
    @FXML
    private Button refreshPendingBtn;

    // Assigned Requests View
    @FXML
    private TableView<Request> assignedRequestsTable;
    @FXML
    private TableColumn<Request, String> assignedTitleColumn;
    @FXML
    private TableColumn<Request, String> assignedTaColumn;
    @FXML
    private TableColumn<Request, String> assignedCreatedAtColumn;
    @FXML
    private TableColumn<Request, Void> assignedActionsColumn;
    @FXML
    private Button refreshAssignedBtn;

    // Answered Requests View
    @FXML
    private TableView<Request> answeredRequestsTable;
    @FXML
    private TableColumn<Request, String> answeredTitleColumn;
    @FXML
    private TableColumn<Request, String> answeredTaColumn;
    @FXML
    private TableColumn<Request, String> answeredResolvedAtColumn;
    @FXML
    private TableColumn<Request, Void> answeredActionsColumn;
    @FXML
    private Button refreshAnsweredBtn;

    // Create Request View
    @FXML
    private TextField requestTitleField;
    @FXML
    private TextArea requestDescriptionArea;
    @FXML
    private Button submitButton;
    @FXML
    private Label submitStatusLabel;
    @FXML
    private ProgressIndicator submitProgressIndicator;

    // Request Details View
    @FXML
    private Label detailTitleLabel;
    @FXML
    private Label detailStatusLabel;
    @FXML
    private Label detailTALabel;
    @FXML
    private Label detailCreatedAtLabel;
    @FXML
    private TextArea detailDescriptionArea;
    @FXML
    private ListView<String> repliesListView;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    private Request selectedRequestForDetails = null;
    private String currentView = "all"; // Track current view for back navigation
    private boolean isLoggingOut = false; // Flag to prevent error dialogs during logout

    private final RequestService requestService = new RequestService();
    private ObservableList<Request> allRequestsList = FXCollections.observableArrayList();
    private ObservableList<Request> pendingRequestsList = FXCollections.observableArrayList();
    private ObservableList<Request> assignedRequestsList = FXCollections.observableArrayList();
    private ObservableList<Request> answeredRequestsList = FXCollections.observableArrayList();
    private Thread autoRefreshThread;

    @Override
    protected void handleLogout() throws java.io.IOException {
        // Set flag to prevent error dialogs during logout
        isLoggingOut = true;

        // Stop auto-refresh thread
        if (autoRefreshThread != null && autoRefreshThread.isAlive()) {
            autoRefreshThread.interrupt();
        }

        // Call parent logout implementation
        super.handleLogout();
    }

    @Override
    protected void initializeDashboard() {
        try {
            System.out.println("[StudentDashboard] Starting initialization...");

            System.out.println("[StudentDashboard] Setting up sidebar navigation...");
            setupSidebarNavigation();

            System.out.println("[StudentDashboard] Setting up All Requests view...");
            setupAllRequestsView();

            System.out.println("[StudentDashboard] Setting up Pending Requests view...");
            setupPendingRequestsView();

            System.out.println("[StudentDashboard] Setting up Assigned Requests view...");
            setupAssignedRequestsView();

            System.out.println("[StudentDashboard] Setting up Answered Requests view...");
            setupAnsweredRequestsView();

            System.out.println("[StudentDashboard] Setting up Create Request view...");
            setupCreateRequestView();

            System.out.println("[StudentDashboard] Setting up Request Details view...");
            setupRequestDetailsView();

            System.out.println("[StudentDashboard] Setting up realtime listeners...");
            setupRealtimeListeners();

            // Load initial data
            System.out.println("[StudentDashboard] Refreshing all requests...");
            refreshAllRequests();

            // Start auto-refresh
            System.out.println("[StudentDashboard] Starting auto-refresh...");
            startAutoRefresh();

            System.out.println("[StudentDashboard] ✓ Student Dashboard initialized successfully");
        } catch (Exception e) {
            System.err.println("[StudentDashboard] ERROR initializing dashboard: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Setup sidebar navigation buttons.
     */
    private void setupSidebarNavigation() {
        // FXML already defines onAction handlers, so we just set the initial active
        // button
        setActiveButton(allRequestsBtn);
    }

    /**
     * Setup All Requests view.
     */
    private void setupAllRequestsView() {
        allTitleColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        allStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus().getDisplayName()));
        allCreatedAtColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedCreatedAt()));
        allTaColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getAssignedToUsername() != null ? cellData.getValue().getAssignedToUsername()
                        : "Unassigned"));

        allStatusColumn.setCellFactory(col -> new TableCell<Request, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Request request = getTableRow().getItem();
                    if (request != null) {
                        setText(item);
                        String color = request.getStatus().getColorHex();
                        setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                    }
                }
            }
        });

        allActionsColumn.setCellFactory(col -> new TableCell<Request, Void>() {
            private final Button viewBtn = new Button("View");
            {
                viewBtn.setStyle(
                        "-fx-font-size: 11; -fx-padding: 4 8; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 3;");
                viewBtn.setOnAction(e -> {
                    Request request = getTableRow().getItem();
                    if (request != null) {
                        showRequestDetails(request);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                }
            }
        });

        allRequestsTable.setItems(allRequestsList);
        refreshAllBtn.setOnAction(e -> refreshAllRequests());
    }

    /**
     * Setup Pending Requests view.
     */
    private void setupPendingRequestsView() {
        pendingTitleColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        pendingCreatedAtColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedCreatedAt()));
        pendingWaitTimeColumn.setCellValueFactory(cellData -> {
            long seconds = cellData.getValue().getWaitTimeSeconds();
            return new javafx.beans.property.SimpleStringProperty(formatWaitTime(seconds));
        });

        pendingActionsColumn.setCellFactory(col -> new TableCell<Request, Void>() {
            private final Button viewBtn = new Button("View");
            {
                viewBtn.setStyle(
                        "-fx-font-size: 11; -fx-padding: 4 8; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 3;");
                viewBtn.setOnAction(e -> {
                    Request request = getTableRow().getItem();
                    if (request != null) {
                        showRequestDetails(request);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                }
            }
        });

        pendingRequestsTable.setItems(pendingRequestsList);
        refreshPendingBtn.setOnAction(e -> refreshPendingRequests());
    }

    /**
     * Setup Assigned Requests view.
     */
    private void setupAssignedRequestsView() {
        assignedTitleColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        assignedTaColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getAssignedToUsername() != null ? cellData.getValue().getAssignedToUsername()
                        : "Unassigned"));
        assignedCreatedAtColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedCreatedAt()));

        assignedActionsColumn.setCellFactory(col -> new TableCell<Request, Void>() {
            private final Button viewBtn = new Button("View");
            {
                viewBtn.setStyle(
                        "-fx-font-size: 11; -fx-padding: 4 8; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 3;");
                viewBtn.setOnAction(e -> {
                    Request request = getTableRow().getItem();
                    if (request != null) {
                        showRequestDetails(request);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                }
            }
        });

        assignedRequestsTable.setItems(assignedRequestsList);
        refreshAssignedBtn.setOnAction(e -> refreshAssignedRequests());
    }

    /**
     * Setup Answered Requests view.
     */
    private void setupAnsweredRequestsView() {
        answeredTitleColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        answeredTaColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getAssignedToUsername() != null ? cellData.getValue().getAssignedToUsername()
                        : "N/A"));
        answeredResolvedAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getResolvedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getResolvedAt().format(
                                java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        answeredActionsColumn.setCellFactory(col -> new TableCell<Request, Void>() {
            private final Button viewBtn = new Button("View");
            {
                viewBtn.setStyle(
                        "-fx-font-size: 11; -fx-padding: 4 8; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 3;");
                viewBtn.setOnAction(e -> {
                    Request request = getTableRow().getItem();
                    if (request != null) {
                        showRequestDetails(request);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                }
            }
        });

        answeredRequestsTable.setItems(answeredRequestsList);
        refreshAnsweredBtn.setOnAction(e -> refreshAnsweredRequests());
    }

    /**
     * Setup Create Request view.
     */
    private void setupCreateRequestView() {
        submitButton.setOnAction(e -> handleSubmitRequest());
        submitProgressIndicator.setVisible(false);
        requestDescriptionArea.setWrapText(true);
    }

    /**
     * Setup Request Details view.
     */
    private void setupRequestDetailsView() {
        editButton.setOnAction(e -> handleEditRequest());
        deleteButton.setOnAction(e -> handleDeleteRequest());
        detailDescriptionArea.setWrapText(true);
        detailDescriptionArea.setEditable(false);
    }

    /**
     * Show All Requests view.
     */
    @FXML
    public void showAllRequests() {
        currentView = "all";
        setActiveButton(allRequestsBtn);
        hideAllViews();
        allRequestsView.setVisible(true);
        allRequestsView.setManaged(true);
        refreshAllRequests();
    }

    /**
     * Show Pending Requests view.
     */
    @FXML
    public void showPendingRequests() {
        currentView = "pending";
        setActiveButton(pendingRequestsBtn);
        hideAllViews();
        pendingRequestsView.setVisible(true);
        pendingRequestsView.setManaged(true);
        refreshPendingRequests();
    }

    /**
     * Show Assigned Requests view.
     */
    @FXML
    public void showAssignedRequests() {
        currentView = "assigned";
        setActiveButton(assignedRequestsBtn);
        hideAllViews();
        assignedRequestsView.setVisible(true);
        assignedRequestsView.setManaged(true);
        refreshAssignedRequests();
    }

    /**
     * Show Answered Requests view.
     */
    @FXML
    public void showAnsweredRequests() {
        currentView = "answered";
        setActiveButton(answeredRequestsBtn);
        hideAllViews();
        answeredRequestsView.setVisible(true);
        answeredRequestsView.setManaged(true);
        refreshAnsweredRequests();
    }

    /**
     * Show Create Request view.
     */
    @FXML
    public void showCreateRequest() {
        currentView = "create";
        setActiveButton(createRequestBtn);
        hideAllViews();
        createRequestView.setVisible(true);
        createRequestView.setManaged(true);
    }

    /**
     * Hide all views.
     */
    private void hideAllViews() {
        allRequestsView.setVisible(false);
        allRequestsView.setManaged(false);
        pendingRequestsView.setVisible(false);
        pendingRequestsView.setManaged(false);
        assignedRequestsView.setVisible(false);
        assignedRequestsView.setManaged(false);
        answeredRequestsView.setVisible(false);
        answeredRequestsView.setManaged(false);
        createRequestView.setVisible(false);
        createRequestView.setManaged(false);
        requestDetailsView.setVisible(false);
        requestDetailsView.setManaged(false);
    }

    /**
     * Set active sidebar button style.
     */
    private void setActiveButton(Button activeBtn) {
        // Reset all buttons
        allRequestsBtn.setStyle(
                "-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-padding: 12 15; -fx-alignment: center-left; -fx-background-radius: 4; -fx-font-size: 14;");
        pendingRequestsBtn.setStyle(
                "-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-padding: 12 15; -fx-alignment: center-left; -fx-background-radius: 4; -fx-font-size: 14;");
        assignedRequestsBtn.setStyle(
                "-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-padding: 12 15; -fx-alignment: center-left; -fx-background-radius: 4; -fx-font-size: 14;");
        answeredRequestsBtn.setStyle(
                "-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-padding: 12 15; -fx-alignment: center-left; -fx-background-radius: 4; -fx-font-size: 14;");
        createRequestBtn.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 12 15; -fx-alignment: center-left; -fx-background-radius: 4; -fx-font-size: 14; -fx-font-weight: bold;");

        // Set active button
        if (activeBtn != createRequestBtn) {
            activeBtn.setStyle(
                    "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 12 15; -fx-alignment: center-left; -fx-background-radius: 4; -fx-font-size: 14; -fx-font-weight: bold;");
        }
    }

    /**
     * Refresh All Requests.
     */
    @FXML
    public void refreshAllRequests() {
        Thread refreshThread = new Thread(() -> {
            try {
                List<Request> requests = requestService.getStudentRequests();
                javafx.application.Platform.runLater(() -> {
                    allRequestsList.setAll(requests);
                });
            } catch (Exception e) {
                System.err.println("Error refreshing all requests: " + e.getMessage());
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    /**
     * Refresh Pending Requests.
     */
    @FXML
    public void refreshPendingRequests() {
        Thread refreshThread = new Thread(() -> {
            try {
                List<Request> allRequests = requestService.getStudentRequests();
                List<Request> pending = allRequests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.PENDING)
                        .collect(Collectors.toList());
                javafx.application.Platform.runLater(() -> {
                    pendingRequestsList.setAll(pending);
                });
            } catch (Exception e) {
                System.err.println("Error refreshing pending requests: " + e.getMessage());
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    /**
     * Refresh Assigned Requests.
     */
    @FXML
    public void refreshAssignedRequests() {
        Thread refreshThread = new Thread(() -> {
            try {
                List<Request> allRequests = requestService.getStudentRequests();
                List<Request> assigned = allRequests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.IN_PROGRESS && r.getAssignedTo() != null)
                        .collect(Collectors.toList());
                javafx.application.Platform.runLater(() -> {
                    assignedRequestsList.setAll(assigned);
                });
            } catch (Exception e) {
                System.err.println("Error refreshing assigned requests: " + e.getMessage());
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    /**
     * Refresh Answered Requests.
     */
    @FXML
    public void refreshAnsweredRequests() {
        Thread refreshThread = new Thread(() -> {
            try {
                List<Request> allRequests = requestService.getStudentRequests();
                List<Request> answered = allRequests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.RESOLVED)
                        .collect(Collectors.toList());
                javafx.application.Platform.runLater(() -> {
                    answeredRequestsList.setAll(answered);
                });
            } catch (Exception e) {
                System.err.println("Error refreshing answered requests: " + e.getMessage());
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    /**
     * Show request details.
     */
    private void showRequestDetails(Request request) {
        selectedRequestForDetails = request;
        currentView = "details";

        detailTitleLabel.setText(request.getTitle());
        detailStatusLabel.setText("Status: " + request.getStatus().getDisplayName());
        detailTALabel.setText("Assigned TA: "
                + (request.getAssignedToUsername() != null ? request.getAssignedToUsername() : "Unassigned"));
        detailCreatedAtLabel.setText("Created: " + request.getFormattedCreatedAt());
        detailDescriptionArea.setText(request.getDescription());

        // Load replies
        loadReplies(request.getId());

        // Enable/disable edit button based on assignment status
        // SECURITY: Students cannot edit requests once assigned to TA
        boolean canEdit = request.getAssignedTo() == null &&
                request.getStatus() != RequestStatus.RESOLVED &&
                request.getStatus() != RequestStatus.CANCELLED;
        editButton.setDisable(!canEdit);
        deleteButton.setDisable(request.getStatus() == RequestStatus.RESOLVED);

        hideAllViews();
        requestDetailsView.setVisible(true);
        requestDetailsView.setManaged(true);
    }

    /**
     * Go back from details view.
     */
    @FXML
    public void goBackFromDetails() {
        selectedRequestForDetails = null;
        switch (currentView) {
            case "all":
                showAllRequests();
                break;
            case "pending":
                showPendingRequests();
                break;
            case "assigned":
                showAssignedRequests();
                break;
            case "answered":
                showAnsweredRequests();
                break;
            default:
                showAllRequests();
        }
    }

    /**
     * Load replies for a request.
     */
    private void loadReplies(String requestId) {
        Thread repliesThread = new Thread(() -> {
            try {
                List<com.lms.ui.model.ReplyDto> replies = requestService.getReplies(requestId);
                javafx.application.Platform.runLater(() -> {
                    repliesListView.getItems().clear();
                    if (replies.isEmpty()) {
                        repliesListView.getItems().add("No replies yet.");
                    } else {
                        for (com.lms.ui.model.ReplyDto reply : replies) {
                            String replyText = String.format("[%s] %s: %s",
                                    reply.getFormattedCreatedAt(),
                                    reply.getTaUsername(),
                                    reply.getMessage());
                            repliesListView.getItems().add(replyText);
                        }
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    repliesListView.getItems().clear();
                    repliesListView.getItems().add("Error loading replies: " + e.getMessage());
                });
            }
        });
        repliesThread.setDaemon(true);
        repliesThread.start();
    }

    /**
     * Handle submit request.
     */
    @FXML
    public void handleSubmitRequest() {
        String title = requestTitleField.getText().trim();
        String description = requestDescriptionArea.getText().trim();

        if (title.isEmpty() || description.isEmpty()) {
            showStatus("Please fill in all fields", true);
            return;
        }

        submitButton.setDisable(true);
        submitProgressIndicator.setVisible(true);

        Thread submitThread = new Thread(() -> {
            try {
                requestService.createRequest(title, description);

                javafx.application.Platform.runLater(() -> {
                    submitButton.setDisable(false);
                    submitProgressIndicator.setVisible(false);
                    showStatus("Request submitted successfully!", false);

                    requestTitleField.clear();
                    requestDescriptionArea.clear();

                    // Refresh all views
                    refreshAllRequests();
                    refreshPendingRequests();

                    // Switch to All Requests view
                    showAllRequests();
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    submitButton.setDisable(false);
                    submitProgressIndicator.setVisible(false);
                    showStatus("Error submitting request: " + e.getMessage(), true);
                });
            }
        });
        submitThread.setDaemon(true);
        submitThread.start();
    }

    /**
     * Handle edit request.
     */
    @FXML
    public void handleEditRequest() {
        if (selectedRequestForDetails == null)
            return;

        // Check if request is assigned (security check)
        if (selectedRequestForDetails.getAssignedTo() != null) {
            showError("Cannot edit request: It has been assigned to a TA. Please contact the TA for changes.");
            return;
        }

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Edit Request");
        dialog.setHeaderText("Update your request");

        javafx.scene.control.ButtonType saveButtonType = new javafx.scene.control.ButtonType("Save",
                javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, javafx.scene.control.ButtonType.CANCEL);

        TextField titleField = new TextField(selectedRequestForDetails.getTitle());
        TextArea descriptionArea = new TextArea(selectedRequestForDetails.getDescription());
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(5);

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Description:"), descriptionArea);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Pair<>(titleField.getText().trim(), descriptionArea.getText().trim());
            }
            return null;
        });

        java.util.Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            if (!pair.getKey().isEmpty() && !pair.getValue().isEmpty()) {
                Thread updateThread = new Thread(() -> {
                    try {
                        requestService.updateRequest(selectedRequestForDetails.getId(), pair.getKey(), pair.getValue());
                        javafx.application.Platform.runLater(() -> {
                            showInfo("Request updated successfully!");
                            refreshAllRequests();
                            refreshPendingRequests();
                            refreshAssignedRequests();
                            goBackFromDetails();
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            showError("Failed to update request: " + e.getMessage());
                        });
                    }
                });
                updateThread.setDaemon(true);
                updateThread.start();
            }
        });
    }

    /**
     * Handle delete request.
     */
    @FXML
    public void handleDeleteRequest() {
        if (selectedRequestForDetails == null)
            return;

        // Check if request is assigned (security check)
        if (selectedRequestForDetails.getAssignedTo() != null) {
            showError("Cannot delete request: It has been assigned to a TA. Please contact the TA for deletion.");
            return;
        }

        // Check if request is resolved
        if (selectedRequestForDetails.getStatus() == RequestStatus.RESOLVED) {
            showError("Cannot delete request: It has been resolved.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Request");
        confirmAlert.setHeaderText("Are you sure you want to delete this request?");
        confirmAlert.setContentText("This action cannot be undone.");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            Thread deleteThread = new Thread(() -> {
                try {
                    requestService.deleteRequest(selectedRequestForDetails.getId());
                    javafx.application.Platform.runLater(() -> {
                        showInfo("Request deleted successfully!");
                        refreshAllRequests();
                        refreshPendingRequests();
                        refreshAssignedRequests();
                        refreshAnsweredRequests();
                        goBackFromDetails();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        String errorMsg = e.getMessage();
                        if (errorMsg != null && errorMsg.contains("assigned")) {
                            showError("Cannot delete request: It has been assigned to a TA.");
                        } else {
                            showError("Failed to delete request: " + errorMsg);
                        }
                    });
                }
            });
            deleteThread.setDaemon(true);
            deleteThread.start();
        }
    }

    /**
     * Setup real-time WebSocket listeners.
     */
    private void setupRealtimeListeners() {
        requestService.addListener(new RequestService.RequestChangeListener() {
            @Override
            public void onRequestCreated(Request request) {
                if (request.getStudentId().equals(currentUser.getId())) {
                    javafx.application.Platform.runLater(() -> {
                        refreshAllRequests();
                        refreshPendingRequests();
                    });
                }
            }

            @Override
            public void onRequestAssigned(Request request) {
                if (request.getStudentId().equals(currentUser.getId())) {
                    javafx.application.Platform.runLater(() -> {
                        refreshAllRequests();
                        refreshPendingRequests();
                        refreshAssignedRequests();
                    });
                }
            }

            @Override
            public void onRequestResolved(Request request) {
                if (request.getStudentId().equals(currentUser.getId())) {
                    javafx.application.Platform.runLater(() -> {
                        refreshAllRequests();
                        refreshAssignedRequests();
                        refreshAnsweredRequests();
                    });
                }
            }

            @Override
            public void onRequestUpdated(Request request) {
                if (request.getStudentId().equals(currentUser.getId())) {
                    javafx.application.Platform.runLater(() -> {
                        refreshAllRequests();
                        refreshPendingRequests();
                        refreshAssignedRequests();
                    });
                }
            }
        });

        requestService.setupRealtimeUpdates();
    }

    /**
     * Start auto-refresh timer.
     */
    private void startAutoRefresh() {
        autoRefreshThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000); // Refresh every 5 seconds
                    javafx.application.Platform.runLater(() -> {
                        if (allRequestsView.isVisible())
                            refreshAllRequests();
                        else if (pendingRequestsView.isVisible())
                            refreshPendingRequests();
                        else if (assignedRequestsView.isVisible())
                            refreshAssignedRequests();
                        else if (answeredRequestsView.isVisible())
                            refreshAnsweredRequests();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        autoRefreshThread.setDaemon(true);
        autoRefreshThread.start();
    }

    /**
     * Format wait time.
     */
    private String formatWaitTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        } else {
            return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
        }
    }

    /**
     * Show status message.
     */
    private void showStatus(String message, boolean isError) {
        submitStatusLabel.setText(message);
        if (isError) {
            submitStatusLabel.setStyle("-fx-text-fill: #d32f2f;");
        } else {
            submitStatusLabel.setStyle("-fx-text-fill: #388e3c;");
        }

        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(e -> submitStatusLabel.setText(""));
        delay.play();
    }

    /**
     * Show info dialog.
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error dialog.
     */
    private void showError(String message) {
        // Skip error dialogs if we're in the process of logging out
        if (isLoggingOut) {
            System.out.println("[StudentDashboardController] Suppressing error during logout: " + message);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
