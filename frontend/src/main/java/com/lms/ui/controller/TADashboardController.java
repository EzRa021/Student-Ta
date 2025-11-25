package com.lms.ui.controller;

import com.lms.ui.model.Request;
import com.lms.ui.model.RequestStatus;
import com.lms.ui.service.RequestService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * TA Dashboard Controller with sidebar navigation.
 * Allows TAs to view pending requests, assign themselves, and reply in
 * real-time.
 */
public class TADashboardController extends DashboardController {

    // Top Bar - User Profile
    @FXML
    private MenuButton userProfileBtn;
    @FXML
    private MenuItem userNameItem;
    @FXML
    private MenuItem userRoleItem;

    // Sidebar buttons
    @FXML
    private Button allRequestsBtn;
    @FXML
    private Button allPendingBtn;
    @FXML
    private Button inProgressBtn;
    @FXML
    private Button myAssignedBtn;
    @FXML
    private Button resolvedBtn;

    // Content pane and views
    @FXML
    private StackPane contentPane;
    @FXML
    private VBox allPendingView;
    @FXML
    private VBox inProgressView;
    @FXML
    private VBox myAssignedView;
    @FXML
    private VBox allRequestsView;
    @FXML
    private VBox resolvedView;
    @FXML
    private VBox requestDetailView;
    @FXML
    private Button backFromDetailBtn;

    // All Requests View - Advanced Filtering
    @FXML
    private VBox advancedFilterPanel;
    @FXML
    private HBox simpleSearchBox;
    @FXML
    private Button toggleFilterBtn;
    @FXML
    private ComboBox<String> filterStatus;
    @FXML
    private TextField allRequestsSearchFieldSimple;

    // All Pending View
    @FXML
    private TableView<Request> pendingRequestsTable;
    @FXML
    private TableColumn<Request, String> studentColumn;
    @FXML
    private TableColumn<Request, String> titleColumn;
    @FXML
    private TableColumn<Request, String> createdAtColumn;
    @FXML
    private TableColumn<Request, Void> actionColumn;
    @FXML
    private Button refreshPendingBtn;
    @FXML
    private TextField pendingSearchField;

    // Request Details Panel (in All Pending View)
    @FXML
    private Label noSelectionLabel;
    @FXML
    private VBox requestDetailsPanel;
    @FXML
    private Label selectedStudentLabel;
    @FXML
    private Label selectedTitleLabel;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label statusLabel;
    @FXML
    private Label priorityLabel;
    @FXML
    private ListView<String> repliesListView;
    @FXML
    private TextArea replyTextArea;
    @FXML
    private Button sendReplyButton;
    @FXML
    private Button claimButton;
    @FXML
    private Button resolveButton;
    @FXML
    private ProgressIndicator actionProgressIndicator;

    // Request Detail View (Full Page)
    @FXML
    private Label detailTitleLabel;
    @FXML
    private Label detailStudentLabel;
    @FXML
    private Label detailStatusLabel;
    @FXML
    private Label detailPriorityLabel;
    @FXML
    private Label detailAssignedLabel;
    @FXML
    private Label detailTimeLabel;
    @FXML
    private TextArea detailDescriptionArea;
    @FXML
    private ListView<String> detailRepliesListView;
    @FXML
    private Button refreshDetailBtn;

    // Reply Page View (Separate Full-Page for Composing Reply)
    @FXML
    private VBox replyPageView;
    @FXML
    private Button backFromReplyBtn;
    @FXML
    private Label replyPageTitleLabel;
    @FXML
    private Label replyPageStatusLabel;
    @FXML
    private Label replyPageStudentLabel;
    @FXML
    private Label replyPagePriorityLabel;
    @FXML
    private Label replyPageTimeLabel;
    @FXML
    private TextArea replyPageDescriptionArea;
    @FXML
    private ListView<String> replyPageRepliesListView;
    @FXML
    private TextArea replyPageTextArea;
    @FXML
    private Button replyPageSendButton;
    @FXML
    private Button replyPageMarkResolvedButton;
    @FXML
    private ProgressIndicator replyPageProgressIndicator;

    // All Requests View
    @FXML
    private TableView<Request> allRequestsTable;
    @FXML
    private TableColumn<Request, String> allStudentColumn;
    @FXML
    private TableColumn<Request, String> allTitleColumn;
    @FXML
    private TableColumn<Request, String> allStatusColumn;
    @FXML
    private TableColumn<Request, String> allCreatedAtColumn;
    @FXML
    private TableColumn<Request, Void> allActionsColumn;
    @FXML
    private Button refreshAllBtn;
    @FXML
    private TextField allRequestsSearchField;

    // My Assigned View
    @FXML
    private TableView<Request> myAssignedTable;
    @FXML
    private TableColumn<Request, String> assignedStudentColumn;
    @FXML
    private TableColumn<Request, String> assignedTitleColumn;
    @FXML
    private TableColumn<Request, String> assignedCreatedAtColumn;
    @FXML
    private TableColumn<Request, String> assignedStatusColumn;
    @FXML
    private TableColumn<Request, Void> assignedActionsColumn;
    @FXML
    private Button refreshAssignedBtn;
    @FXML
    private TextField assignedSearchField;

    // In Progress View
    @FXML
    private TableView<Request> inProgressTable;
    @FXML
    private TableColumn<Request, String> inProgressStudentColumn;
    @FXML
    private TableColumn<Request, String> inProgressTitleColumn;
    @FXML
    private TableColumn<Request, String> inProgressAssignedColumn;
    @FXML
    private TableColumn<Request, String> inProgressCreatedAtColumn;
    @FXML
    private TableColumn<Request, Void> inProgressActionsColumn;
    @FXML
    private Button refreshInProgressBtn;
    @FXML
    private TextField inProgressSearchField;

    // Resolved Requests View
    @FXML
    private TableView<Request> resolvedTable;
    @FXML
    private TableColumn<Request, String> resolvedStudentColumn;
    @FXML
    private TableColumn<Request, String> resolvedTitleColumn;
    @FXML
    private TableColumn<Request, String> resolvedCreatedAtColumn;
    @FXML
    private TableColumn<Request, String> resolvedResolvedAtColumn;
    @FXML
    private Button refreshResolvedBtn;
    @FXML
    private TextField resolvedSearchField;

    // Statistics
    @FXML
    private Label pendingCountLabel;
    @FXML
    private Label inProgressCountLabel;
    @FXML
    private Label resolvedCountLabel;

    private Request selectedRequest = null;
    private boolean replyWasSentForSelectedRequest = false;
    private Request currentDetailRequest = null;
    private String previousViewName = "allPendingView"; // Track which view to return to
    private boolean showReplyInputInDetail = false; // Track if we're viewing for reply or just viewing
    private boolean replyHasBeenSent = false; // Track if reply has been sent on reply page
    private boolean isLoggingOut = false; // Flag to prevent error dialogs during logout
    private final RequestService requestService = new RequestService();
    private ObservableList<Request> pendingRequests = FXCollections.observableArrayList();
    private ObservableList<Request> inProgressRequests = FXCollections.observableArrayList();
    private ObservableList<Request> myAssignedRequests = FXCollections.observableArrayList();
    private ObservableList<Request> allRequests = FXCollections.observableArrayList();
    private ObservableList<Request> resolvedRequests = FXCollections.observableArrayList();

    // Filtered lists for search
    private javafx.collections.transformation.FilteredList<Request> filteredPendingRequests;
    private javafx.collections.transformation.FilteredList<Request> filteredInProgressRequests;
    private javafx.collections.transformation.FilteredList<Request> filteredAllRequests;
    private javafx.collections.transformation.FilteredList<Request> filteredAssignedRequests;
    private javafx.collections.transformation.FilteredList<Request> filteredResolvedRequests;

    private Thread autoRefreshThread;

    @Override
    protected void initializeDashboard() {
        try {
            setupUserProfile();
            setupAdvancedFilters();
            setupSidebarNavigation();
            setupAllPendingView();
            setupInProgressView();
            setupAllRequestsView();
            setupMyAssignedView();
            setupResolvedView();
            setupRealtimeListeners();
            refreshPendingRequests();
            refreshInProgress();
            refreshAllRequests();
            refreshMyAssigned();
            refreshResolvedRequests();
            updateStatistics();
            startAutoRefresh();
            System.out.println("✓ TA Dashboard initialized successfully");
        } catch (Exception e) {
            System.err.println("ERROR initializing TA Dashboard: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

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

    /**
     * Setup user profile dropdown.
     */
    private void setupUserProfile() {
        if (userProfileBtn != null && currentUser != null) {
            userProfileBtn.setText("👤 " + currentUser.getUsername());
            userNameItem.setText("Name: " + currentUser.getUsername());
            userRoleItem.setText("Role: TA");
        }
    }

    /**
     * Setup advanced filters for All Requests view.
     */
    private void setupAdvancedFilters() {
        // Status filter
        ObservableList<String> statuses = FXCollections.observableArrayList(
                "All", "PENDING", "IN_PROGRESS", "RESOLVED", "CANCELLED");
        filterStatus.setItems(statuses);
        filterStatus.setValue("All");
        filterStatus.valueProperty().addListener((obs, oldVal, newVal) -> {
            applyAdvancedFilters();
        });

        // Link search field in filter panel
        if (allRequestsSearchFieldSimple != null) {
            allRequestsSearchFieldSimple.textProperty().addListener((obs, oldVal, newVal) -> {
                applyAdvancedFilters();
            });
        }
    }

    /**
     * Apply advanced filters (priority, status, search) to all requests table.
     */
    private void applyAdvancedFilters() {
        String selectedStatus = filterStatus.getValue();
        String searchText = allRequestsSearchFieldSimple != null ? allRequestsSearchFieldSimple.getText() : "";

        if ((selectedStatus == null || selectedStatus.equals("All")) &&
                (searchText == null || searchText.trim().isEmpty())) {
            // No filters applied
            filteredAllRequests.setPredicate(null);
        } else {
            filteredAllRequests.setPredicate(request -> {
                // Check status filter
                if (selectedStatus != null && !selectedStatus.equals("All")) {
                    if (request.getStatus() == null || !request.getStatus().name().equals(selectedStatus)) {
                        return false;
                    }
                }

                // Check search text
                if (searchText != null && !searchText.trim().isEmpty()) {
                    String query = searchText.toLowerCase().trim();
                    return request.getStudentUsername().toLowerCase().contains(query) ||
                            request.getTitle().toLowerCase().contains(query) ||
                            request.getDescription().toLowerCase().contains(query) ||
                            (request.getAssignedToUsername() != null &&
                                    request.getAssignedToUsername().toLowerCase().contains(query));
                }

                return true;
            });
        }
    }

    /**
     * Toggle advanced filter panel visibility.
     */
    @FXML
    public void toggleAdvancedFilter() {
        boolean visible = advancedFilterPanel.isVisible();
        advancedFilterPanel.setVisible(!visible);
        simpleSearchBox.setVisible(visible);
        if (visible) {
            // Clear advanced filter when hiding
            filterStatus.setValue("All");
            allRequestsSearchFieldSimple.clear();
        }
    }

    /**
     * Clear all advanced filters.
     */
    @FXML
    public void clearAllFilters() {
        filterStatus.setValue("All");
        allRequestsSearchFieldSimple.clear();
        filterAllRequests("");
    }

    /**
     * Handle settings menu item.
     */
    @FXML
    public void handleSettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText("Settings");
        alert.setContentText("Settings page coming soon!");
        alert.showAndWait();
    }

    /**
     * Handle help menu item.
     */
    @FXML
    public void handleHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Help & Documentation");
        alert.setContentText("For help, please visit the documentation or contact support.");
        alert.showAndWait();
    }

    /**
     * Setup sidebar navigation.
     */
    private void setupSidebarNavigation() {
        // FXML already defines onAction handlers, so we just set the initial active
        // button
        setActiveButton(allPendingBtn);
    }

    /**
     * Setup All Pending Requests view.
     */
    private void setupAllPendingView() {
        studentColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStudentUsername()));
        titleColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        createdAtColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedCreatedAtFull()));

        actionColumn.setCellFactory(col -> new TableCell<Request, Void>() {
            private final HBox actionBox = new HBox(5);
            private final Button replyBtn = new Button("Reply");
            private final Button viewBtn = new Button("View");
            private final Button claimBtn = new Button("Claim");
            {
                replyBtn.setStyle(
                        "-fx-font-size: 11; -fx-padding: 5 8; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 3;");
                replyBtn.setOnAction(e -> {
                    Request request = getTableRow().getItem();
                    if (request != null) {
                        previousViewName = "allPendingView";
                        showReplyPage(request);
                    }
                });

                viewBtn.setStyle(
                        "-fx-font-size: 11; -fx-padding: 5 8; -fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 3;");
                viewBtn.setOnAction(e -> {
                    Request request = getTableRow().getItem();
                    if (request != null) {
                        previousViewName = "allPendingView";
                        showRequestDetail(request);
                    }
                });

                claimBtn.setStyle(
                        "-fx-font-size: 11; -fx-padding: 5 8; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 3;");
                claimBtn.setOnAction(e -> {
                    Request request = getTableRow().getItem();
                    if (request != null) {
                        onClaimRequest(request);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    Request request = getTableRow().getItem();
                    if (request != null && request.getStatus() == RequestStatus.PENDING) {
                        actionBox.getChildren().clear();
                        actionBox.getChildren().addAll(replyBtn, viewBtn);

                        // Can only claim if unassigned OR already assigned to this TA
                        boolean canClaim = request.getAssignedTo() == null ||
                                (request.getAssignedToUsername() != null &&
                                        request.getAssignedToUsername().equals(currentUser.getUsername()));
                        if (canClaim) {
                            actionBox.getChildren().add(claimBtn);
                        }

                        setGraphic(actionBox);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        filteredPendingRequests = new javafx.collections.transformation.FilteredList<>(pendingRequests);
        pendingRequestsTable.setItems(filteredPendingRequests);
        pendingRequestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Setup search listener for pending requests
        pendingSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterPendingRequests(newVal);
        });

        refreshPendingBtn.setOnAction(e -> {
            refreshPendingRequests();
            updateStatistics();
        });
    }

    /**
     * Setup All Requests view.
     */
    private void setupAllRequestsView() {
        allStudentColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStudentUsername()));
        allTitleColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        allStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus().getDisplayName()));
        allCreatedAtColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedCreatedAtFull()));

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
                        "-fx-font-size: 11; -fx-padding: 5 10; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 3;");
                viewBtn.setOnAction(e -> {
                    Request request = getTableRow().getItem();
                    if (request != null) {
                        previousViewName = "allRequestsView";
                        showRequestDetail(request);
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

        filteredAllRequests = new javafx.collections.transformation.FilteredList<>(allRequests);
        allRequestsTable.setItems(filteredAllRequests);
        allRequestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Setup search listener for all requests
        allRequestsSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterAllRequests(newVal);
        });

        refreshAllBtn.setOnAction(e -> refreshAllRequests());
    }

    /**
     * Setup My Assigned Requests view.
     */
    private void setupMyAssignedView() {
        assignedStudentColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStudentUsername()));
        assignedTitleColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        assignedCreatedAtColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedCreatedAtFull()));
        assignedStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus().getDisplayName()));

        assignedStatusColumn.setCellFactory(col -> new TableCell<Request, String>() {
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

        assignedActionsColumn.setCellFactory(col -> new TableCell<Request, Void>() {
            private final Button replyBtn = new Button("Reply");
            private final Button viewBtn = new Button("View");
            {
                replyBtn.setStyle(
                        "-fx-font-size: 11; -fx-padding: 5 10; -fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 3;");
                replyBtn.setOnAction(e -> {
                    Request request = getTableRow().getItem();
                    if (request != null) {
                        previousViewName = "myAssignedView";
                        showReplyPage(request);
                    }
                });
                viewBtn.setStyle(
                        "-fx-font-size: 11; -fx-padding: 5 10; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 3;");
                viewBtn.setOnAction(e -> {
                    Request request = getTableRow().getItem();
                    if (request != null) {
                        previousViewName = "myAssignedView";
                        showRequestDetail(request);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    Request request = getTableRow().getItem();
                    if (request != null && request.getStatus() == RequestStatus.RESOLVED) {
                        setGraphic(viewBtn);
                    } else {
                        setGraphic(replyBtn);
                    }
                }
            }
        });

        filteredAssignedRequests = new javafx.collections.transformation.FilteredList<>(myAssignedRequests);
        myAssignedTable.setItems(filteredAssignedRequests);
        myAssignedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Setup search listener for assigned requests
        assignedSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterAssignedRequests(newVal);
        });

        refreshAssignedBtn.setOnAction(e -> refreshMyAssigned());
    }

    /**
     * Setup In Progress Requests view.
     */
    private void setupInProgressView() {
        inProgressStudentColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStudentUsername()));
        inProgressTitleColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        inProgressAssignedColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAssignedToUsername() != null
                                ? cellData.getValue().getAssignedToUsername()
                                : "Unassigned"));
        inProgressCreatedAtColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedCreatedAtFull()));

        inProgressActionsColumn.setCellFactory(col -> new TableCell<Request, Void>() {
            private final Button replyBtn = new Button("Reply");
            {
                replyBtn.setStyle(
                        "-fx-font-size: 11; -fx-padding: 5 10; -fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 3;");
                replyBtn.setOnAction(e -> {
                    Request request = getTableRow().getItem();
                    if (request != null) {
                        previousViewName = "inProgressView";
                        showReplyPage(request);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(replyBtn);
                }
            }
        });

        filteredInProgressRequests = new javafx.collections.transformation.FilteredList<>(inProgressRequests);
        inProgressTable.setItems(filteredInProgressRequests);
        inProgressTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Setup search listener for in-progress requests
        inProgressSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterInProgressRequests(newVal);
        });

        refreshInProgressBtn.setOnAction(e -> refreshInProgress());
    }

    /**
     * Setup Resolved Requests view.
     */
    private void setupResolvedView() {
        resolvedStudentColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStudentUsername()));
        resolvedTitleColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        resolvedCreatedAtColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedCreatedAt()));
        resolvedResolvedAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getResolvedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getResolvedAt().format(
                                java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        filteredResolvedRequests = new javafx.collections.transformation.FilteredList<>(resolvedRequests);
        resolvedTable.setItems(filteredResolvedRequests);
        resolvedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Setup search listener for resolved requests
        resolvedSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterResolvedRequests(newVal);
        });

        refreshResolvedBtn.setOnAction(e -> refreshResolvedRequests());
    }

    /**
     * Show All Requests view.
     */
    @FXML
    public void showAllRequests() {
        setActiveButton(allRequestsBtn);
        hideAllViews();
        allRequestsView.setVisible(true);
        allRequestsView.setManaged(true);
        refreshAllRequests();
    }

    /**
     * Show All Pending view.
     */
    @FXML
    public void showAllPending() {
        setActiveButton(allPendingBtn);
        hideAllViews();
        allPendingView.setVisible(true);
        allPendingView.setManaged(true);
        refreshPendingRequests();
    }

    /**
     * Show My Assigned view.
     */
    @FXML
    public void showMyAssigned() {
        setActiveButton(myAssignedBtn);
        hideAllViews();
        myAssignedView.setVisible(true);
        myAssignedView.setManaged(true);
        refreshMyAssigned();
    }

    /**
     * Show In Progress Requests view.
     */
    @FXML
    public void showInProgress() {
        setActiveButton(inProgressBtn);
        hideAllViews();
        inProgressView.setVisible(true);
        inProgressView.setManaged(true);
        refreshInProgress();
    }

    /**
     * Show Resolved Requests view.
     */
    @FXML
    public void showResolvedRequests() {
        setActiveButton(resolvedBtn);
        hideAllViews();
        resolvedView.setVisible(true);
        resolvedView.setManaged(true);
        refreshResolvedRequests();
    }

    /**
     * Hide all views.
     */
    private void hideAllViews() {
        allPendingView.setVisible(false);
        allPendingView.setManaged(false);
        inProgressView.setVisible(false);
        inProgressView.setManaged(false);
        myAssignedView.setVisible(false);
        myAssignedView.setManaged(false);
        allRequestsView.setVisible(false);
        allRequestsView.setManaged(false);
        resolvedView.setVisible(false);
        resolvedView.setManaged(false);
        requestDetailView.setVisible(false);
        requestDetailView.setManaged(false);
        replyPageView.setVisible(false);
        replyPageView.setManaged(false);
    }

    /**
     * Set active sidebar button.
     */
    private void setActiveButton(Button activeBtn) {
        allRequestsBtn.setStyle(
                "-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-padding: 12 15; -fx-alignment: center-left; -fx-background-radius: 4; -fx-font-size: 14;");
        allPendingBtn.setStyle(
                "-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-padding: 12 15; -fx-alignment: center-left; -fx-background-radius: 4; -fx-font-size: 14;");
        inProgressBtn.setStyle(
                "-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-padding: 12 15; -fx-alignment: center-left; -fx-background-radius: 4; -fx-font-size: 14;");
        myAssignedBtn.setStyle(
                "-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-padding: 12 15; -fx-alignment: center-left; -fx-background-radius: 4; -fx-font-size: 14;");
        resolvedBtn.setStyle(
                "-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-padding: 12 15; -fx-alignment: center-left; -fx-background-radius: 4; -fx-font-size: 14;");
        activeBtn.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 12 15; -fx-alignment: center-left; -fx-background-radius: 4; -fx-font-size: 14; -fx-font-weight: bold;");
    }

    /**
     * Handle request selection in All Pending view.
     * No longer used - detail view is full-page now.
     */
    private void onRequestSelected(Request request) {
        selectedRequest = request;
        replyWasSentForSelectedRequest = false;
    }

    /**
     * Show request details for reply (from My Assigned view).
     */
    @Deprecated
    private void showRequestDetailsForReply(Request request) {
        // Deprecated: use showReplyPage() instead
        previousViewName = "myAssignedView";
        showReplyPage(request);
    }

    /**
     * Load replies for a request (deprecated - use loadDetailReplies instead).
     */
    @Deprecated
    private void loadReplies(String requestId) {
        // Deprecated: use loadDetailReplies() instead for full-page detail view
    }

    /**
     * Handle send reply (deprecated - use detailOnSendReply instead).
     */
    @Deprecated
    @FXML
    public void onSendReply() {
        // Deprecated: use detailOnSendReply() instead for full-page detail view
    }

    /**
     * Handle claim request (called from FXML) - DEPRECATED.
     * Use detailOnClaimRequest() instead.
     */
    @Deprecated
    @FXML
    public void onClaimRequest() {
        onClaimRequest(selectedRequest);
    }

    /**
     * Handle claim request (internal method with parameter) - DEPRECATED.
     * Use detailOnClaimRequest() instead.
     */
    @Deprecated
    private void onClaimRequest(Request request) {
        if (request == null)
            return;

        // Check assignment rules before attempting to claim
        if (request.getAssignedTo() != null &&
                (request.getAssignedToUsername() == null ||
                        !request.getAssignedToUsername().equals(currentUser.getUsername()))) {
            showError("Cannot claim request: It is already assigned to another TA (" +
                    (request.getAssignedToUsername() != null ? request.getAssignedToUsername() : "Unknown") + ")");
            return;
        }

        Thread claimThread = new Thread(() -> {
            try {
                Request updated = requestService.assignRequest(request.getId());

                javafx.application.Platform.runLater(() -> {
                    if (updated != null) {
                        showInfo("Request claimed successfully!");
                        refreshPendingRequests();
                        refreshAllRequests();
                        refreshMyAssigned();
                        updateStatistics();
                        onRequestSelected(null); // Clear selection
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("already assigned")) {
                        showError("Cannot claim request: It is already assigned to another TA.");
                    } else {
                        showError("Error claiming request: " + errorMsg);
                    }
                });
            }
        });
        claimThread.setDaemon(true);
        claimThread.start();
    }

    /**
     * Handle resolve request (called from FXML) - DEPRECATED.
     * Use detailOnResolveRequest() instead.
     */
    @Deprecated
    @FXML
    public void onResolveRequest() {
        onResolveRequest(selectedRequest);
    }

    /**
     * Handle resolve request (internal method with parameter) - DEPRECATED.
     * Use detailOnResolveRequest() instead.
     */
    @Deprecated
    private void onResolveRequest(Request request) {
        if (request == null)
            return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Mark as Resolved");
        confirmAlert.setContentText("Are you sure you want to mark this request as resolved?");

        if (confirmAlert.showAndWait().get() != ButtonType.OK) {
            return;
        }

        Thread resolveThread = new Thread(() -> {
            try {
                Request updated = requestService.resolveRequest(request.getId());

                javafx.application.Platform.runLater(() -> {
                    if (updated != null) {
                        showInfo("Request resolved successfully!");
                        refreshPendingRequests();
                        refreshMyAssigned();
                        updateStatistics();
                        onRequestSelected(null);
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Error resolving request: " + e.getMessage());
                });
            }
        });
        resolveThread.setDaemon(true);
        resolveThread.start();
    }

    /**
     * Refresh all requests (show all statuses: PENDING, IN_PROGRESS, RESOLVED).
     * PENDING requests at top, then sorted by creation date descending (latest
     * first).
     */
    @FXML
    public void refreshAllRequests() {
        Thread refreshThread = new Thread(() -> {
            try {
                List<Request> allRequestsList = new java.util.ArrayList<>();

                // Fetch requests for each status
                try {
                    String response = com.lms.ui.service.ApiClient.get("/requests?page=0&size=100");
                    com.google.gson.JsonObject jsonResponse = new com.google.gson.Gson().fromJson(response,
                            com.google.gson.JsonObject.class);

                    if (jsonResponse.has("content")) {
                        com.google.gson.JsonArray content = jsonResponse.getAsJsonArray("content");
                        for (var item : content) {
                            try {
                                Request req = requestService.parseRequest(item.toString());
                                allRequestsList.add(req);
                            } catch (Exception e) {
                                System.err.println("Error parsing request: " + e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching all requests: " + e.getMessage());
                }

                // Sort with PENDING first, then by creation date descending (latest first)
                allRequestsList.sort((a, b) -> {
                    // First, prioritize PENDING status (comes first)
                    boolean aIsPending = a.getStatus() == RequestStatus.PENDING;
                    boolean bIsPending = b.getStatus() == RequestStatus.PENDING;

                    if (aIsPending && !bIsPending) {
                        return -1; // a (pending) comes first
                    }
                    if (!aIsPending && bIsPending) {
                        return 1; // b (pending) comes first
                    }

                    // For same status, sort by creation date descending (latest first)
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null)
                        return 0;
                    if (a.getCreatedAt() == null)
                        return 1;
                    if (b.getCreatedAt() == null)
                        return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt()); // Descending (latest first)
                });

                javafx.application.Platform.runLater(() -> {
                    allRequests.setAll(allRequestsList);
                });
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.contains("Authentication failed")) {
                    javafx.application.Platform.runLater(() -> {
                        showError("Session expired. Please log out and log back in.");
                    });
                } else {
                    System.err.println("Error refreshing all requests: " + errorMsg);
                }
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    /**
     * Refresh pending requests.
     */
    @FXML
    public void refreshPendingRequests() {
        Thread refreshThread = new Thread(() -> {
            try {
                List<Request> requests = requestService.getPendingRequests();
                // Sort by createdAt ASC (FIFO - oldest first)
                requests.sort((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null)
                        return 0;
                    if (a.getCreatedAt() == null)
                        return 1;
                    if (b.getCreatedAt() == null)
                        return -1;
                    return a.getCreatedAt().compareTo(b.getCreatedAt());
                });
                javafx.application.Platform.runLater(() -> {
                    pendingRequests.setAll(requests);
                });
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.contains("Authentication failed")) {
                    javafx.application.Platform.runLater(() -> {
                        showError("Session expired. Please log out and log back in.");
                    });
                } else {
                    System.err.println("Error refreshing pending requests: " + errorMsg);
                }
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    /**
     * Refresh my assigned requests.
     */
    @FXML
    public void refreshMyAssigned() {
        Thread refreshThread = new Thread(() -> {
            try {
                // Get ALL requests from RequestService to find those assigned to current TA
                String response = com.lms.ui.service.ApiClient.get("/requests?page=0&size=500");
                com.google.gson.JsonObject jsonResponse = new com.google.gson.Gson().fromJson(response,
                        com.google.gson.JsonObject.class);
                com.google.gson.JsonArray content = jsonResponse.getAsJsonArray("content");

                List<Request> allAssignedRequests = new java.util.ArrayList<>();
                for (var item : content) {
                    // Use RequestService's parseRequest method
                    Request req = requestService.parseRequest(item.toString());

                    // Filter for requests assigned to current TA (regardless of status)
                    if (req.getAssignedToUsername() != null &&
                            req.getAssignedToUsername().equals(currentUser.getUsername())) {
                        allAssignedRequests.add(req);
                    }
                }

                // Sort by status (unresolved first) then by createdAt
                allAssignedRequests.sort((a, b) -> {
                    // Put unresolved requests first
                    boolean aResolved = a.getStatus() == RequestStatus.RESOLVED;
                    boolean bResolved = b.getStatus() == RequestStatus.RESOLVED;

                    if (aResolved != bResolved) {
                        return aResolved ? 1 : -1; // Unresolved first
                    }

                    // Within same resolution status, sort by createdAt ASC (oldest first)
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null)
                        return 0;
                    if (a.getCreatedAt() == null)
                        return 1;
                    if (b.getCreatedAt() == null)
                        return -1;
                    return a.getCreatedAt().compareTo(b.getCreatedAt());
                });

                javafx.application.Platform.runLater(() -> {
                    myAssignedRequests.setAll(allAssignedRequests);
                });
            } catch (com.lms.ui.service.ApiClient.ApiException e) {
                String errorMsg = e.getMessage();
                if (errorMsg != null
                        && (errorMsg.contains("403") || errorMsg.contains("401") || errorMsg.contains("Forbidden"))) {
                    System.err.println("Error refreshing my assigned requests: API Error: 403 - " + errorMsg);
                    javafx.application.Platform.runLater(() -> {
                        showError("Session expired or permission denied. Please log out and log back in.");
                    });
                } else {
                    System.err.println("Error refreshing my assigned requests: " + errorMsg);
                }
            } catch (Exception e) {
                System.err.println("Error refreshing my assigned requests: " + e.getMessage());
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    /**
     * Refresh in-progress requests (all requests with IN_PROGRESS status).
     */
    @FXML
    public void refreshInProgress() {
        Thread refreshThread = new Thread(() -> {
            try {
                String response = com.lms.ui.service.ApiClient.get("/requests?status=IN_PROGRESS&page=0&size=100");
                com.google.gson.JsonObject jsonResponse = new com.google.gson.Gson().fromJson(response,
                        com.google.gson.JsonObject.class);
                com.google.gson.JsonArray content = jsonResponse.getAsJsonArray("content");

                List<Request> inProgress = new java.util.ArrayList<>();
                for (var item : content) {
                    inProgress.add(requestService.parseRequest(item.toString()));
                }

                // Sort by createdAt ASC (FIFO - oldest first)
                inProgress.sort((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null)
                        return 0;
                    if (a.getCreatedAt() == null)
                        return 1;
                    if (b.getCreatedAt() == null)
                        return -1;
                    return a.getCreatedAt().compareTo(b.getCreatedAt());
                });

                javafx.application.Platform.runLater(() -> {
                    inProgressRequests.setAll(inProgress);
                });
            } catch (com.lms.ui.service.ApiClient.ApiException e) {
                String errorMsg = e.getMessage();
                if (errorMsg != null
                        && (errorMsg.contains("403") || errorMsg.contains("401") || errorMsg.contains("Forbidden"))) {
                    System.err.println("Error refreshing in-progress requests: API Error: 403 - " + errorMsg);
                    javafx.application.Platform.runLater(() -> {
                        showError("Session expired or permission denied. Please log out and log back in.");
                    });
                } else {
                    System.err.println("Error refreshing in-progress requests: " + errorMsg);
                }
            } catch (Exception e) {
                System.err.println("Error refreshing in-progress requests: " + e.getMessage());
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    /**
     * Refresh resolved requests.
     */
    @FXML
    public void refreshResolvedRequests() {
        Thread refreshThread = new Thread(() -> {
            try {
                String response = com.lms.ui.service.ApiClient.get("/requests?status=RESOLVED&page=0&size=100");
                com.google.gson.JsonObject jsonResponse = new com.google.gson.Gson().fromJson(response,
                        com.google.gson.JsonObject.class);
                com.google.gson.JsonArray content = jsonResponse.getAsJsonArray("content");

                List<Request> resolved = new java.util.ArrayList<>();
                for (var item : content) {
                    resolved.add(requestService.parseRequest(item.toString()));
                }
                // Sort by resolvedAt DESC (most recently resolved first)
                resolved.sort((a, b) -> {
                    if (a.getResolvedAt() == null && b.getResolvedAt() == null)
                        return 0;
                    if (a.getResolvedAt() == null)
                        return 1;
                    if (b.getResolvedAt() == null)
                        return -1;
                    return b.getResolvedAt().compareTo(a.getResolvedAt());
                });

                javafx.application.Platform.runLater(() -> {
                    resolvedRequests.setAll(resolved);
                });
            } catch (com.lms.ui.service.ApiClient.ApiException e) {
                String errorMsg = e.getMessage();
                if (errorMsg != null
                        && (errorMsg.contains("403") || errorMsg.contains("401") || errorMsg.contains("Forbidden"))) {
                    System.err.println("Error refreshing resolved requests: API Error: 403 - " + errorMsg);
                    javafx.application.Platform.runLater(() -> {
                        showError("Session expired or permission denied. Please log out and log back in.");
                    });
                } else {
                    System.err.println("Error refreshing resolved requests: " + errorMsg);
                }
            } catch (Exception e) {
                System.err.println("Error refreshing resolved requests: " + e.getMessage());
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    /**
     * Update statistics.
     */
    /**
     * Update statistics based on current request data.
     * Calculates pending, in-progress, and resolved counts from local collections.
     */
    private void updateStatistics() {
        Thread statsThread = new Thread(() -> {
            try {
                // Count requests by status from local data
                long pendingCount = pendingRequests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.PENDING)
                        .count();

                long inProgressCount = inProgressRequests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.IN_PROGRESS)
                        .count();

                long resolvedCount = resolvedRequests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.RESOLVED)
                        .count();

                javafx.application.Platform.runLater(() -> {
                    pendingCountLabel.setText(String.valueOf(pendingCount));
                    inProgressCountLabel.setText(String.valueOf(inProgressCount));
                    resolvedCountLabel.setText(String.valueOf(resolvedCount));
                });
            } catch (Exception e) {
                System.err.println("Error updating statistics: " + e.getMessage());
            }
        });
        statsThread.setDaemon(true);
        statsThread.start();
    }

    /**
     * Setup real-time listeners.
     */
    private void setupRealtimeListeners() {
        requestService.addListener(new RequestService.RequestChangeListener() {
            @Override
            public void onRequestCreated(Request request) {
                javafx.application.Platform.runLater(() -> {
                    refreshPendingRequests();
                    refreshAllRequests();
                    updateStatistics();
                });
            }

            @Override
            public void onRequestAssigned(Request request) {
                javafx.application.Platform.runLater(() -> {
                    refreshPendingRequests();
                    refreshInProgress();
                    refreshAllRequests();
                    refreshMyAssigned();
                    updateStatistics();
                    if (selectedRequest != null && selectedRequest.getId().equals(request.getId())) {
                        onRequestSelected(null);
                    }
                });
            }

            @Override
            public void onRequestResolved(Request request) {
                javafx.application.Platform.runLater(() -> {
                    refreshPendingRequests();
                    refreshInProgress();
                    refreshAllRequests();
                    refreshMyAssigned();
                    refreshResolvedRequests();
                    updateStatistics();
                    if (selectedRequest != null && selectedRequest.getId().equals(request.getId())) {
                        onRequestSelected(null);
                    }
                });
            }

            @Override
            public void onRequestUpdated(Request request) {
                javafx.application.Platform.runLater(() -> {
                    refreshPendingRequests();
                    refreshInProgress();
                    refreshAllRequests();
                    refreshMyAssigned();
                    updateStatistics();
                });
            }
        });

        requestService.setupRealtimeUpdates();
    }

    /**
     * Start auto-refresh.
     */
    private void startAutoRefresh() {
        autoRefreshThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(3000);
                    javafx.application.Platform.runLater(() -> {
                        if (allPendingView.isVisible()) {
                            refreshPendingRequests();
                            updateStatistics();
                        } else if (inProgressView.isVisible()) {
                            refreshInProgress();
                            updateStatistics();
                        } else if (myAssignedView.isVisible()) {
                            refreshMyAssigned();
                            updateStatistics();
                        } else if (allRequestsView.isVisible()) {
                            refreshAllRequests();
                            updateStatistics();
                        } else if (resolvedView.isVisible()) {
                            refreshResolvedRequests();
                            updateStatistics();
                        }
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
            System.out.println("[TADashboardController] Suppressing error during logout: " + message);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Filter pending requests based on search query.
     */
    private void filterPendingRequests(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            filteredPendingRequests.setPredicate(null);
        } else {
            String query = searchQuery.toLowerCase().trim();
            filteredPendingRequests
                    .setPredicate(request -> request.getStudentUsername().toLowerCase().contains(query) ||
                            request.getTitle().toLowerCase().contains(query) ||
                            request.getDescription().toLowerCase().contains(query));
        }
    }

    /**
     * Filter all requests based on search query.
     */
    private void filterAllRequests(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            filteredAllRequests.setPredicate(null);
        } else {
            String query = searchQuery.toLowerCase().trim();
            filteredAllRequests.setPredicate(request -> request.getStudentUsername().toLowerCase().contains(query) ||
                    request.getTitle().toLowerCase().contains(query) ||
                    request.getDescription().toLowerCase().contains(query) ||
                    (request.getStatus() != null && request.getStatus().getDisplayName().toLowerCase().contains(query))
                    ||
                    (request.getAssignedToUsername() != null
                            && request.getAssignedToUsername().toLowerCase().contains(query)));
        }
    }

    /**
     * Filter assigned requests based on search query.
     */
    private void filterAssignedRequests(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            filteredAssignedRequests.setPredicate(null);
        } else {
            String query = searchQuery.toLowerCase().trim();
            filteredAssignedRequests
                    .setPredicate(request -> request.getStudentUsername().toLowerCase().contains(query) ||
                            request.getTitle().toLowerCase().contains(query) ||
                            request.getDescription().toLowerCase().contains(query));
        }
    }

    /**
     * Filter resolved requests based on search query.
     */
    private void filterResolvedRequests(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            filteredResolvedRequests.setPredicate(null);
        } else {
            String query = searchQuery.toLowerCase().trim();
            filteredResolvedRequests
                    .setPredicate(request -> request.getStudentUsername().toLowerCase().contains(query) ||
                            request.getTitle().toLowerCase().contains(query) ||
                            request.getDescription().toLowerCase().contains(query));
        }
    }

    /**
     * Filter in-progress requests based on search query.
     */
    private void filterInProgressRequests(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            filteredInProgressRequests.setPredicate(null);
        } else {
            String query = searchQuery.toLowerCase().trim();
            filteredInProgressRequests
                    .setPredicate(request -> request.getStudentUsername().toLowerCase().contains(query) ||
                            request.getTitle().toLowerCase().contains(query) ||
                            request.getDescription().toLowerCase().contains(query) ||
                            (request.getAssignedToUsername() != null &&
                                    request.getAssignedToUsername().toLowerCase().contains(query)));
        }
    }

    /**
     * Show request detail view (full page) - read only view.
     */
    private void showRequestDetail(Request request) {
        if (request == null)
            return;

        System.out.println("[TADashboard] Showing request detail view for: " + request.getTitle());
        currentDetailRequest = request;

        // Update detail view with request information
        detailTitleLabel.setText("Title: " + request.getTitle());
        detailStudentLabel.setText("Student: " + request.getStudentUsername());
        detailStatusLabel.setText(request.getStatus().getDisplayName());
        detailPriorityLabel.setText("Priority: " + request.getPriority());
        detailAssignedLabel.setText("Assigned to: " +
                (request.getAssignedToUsername() != null ? request.getAssignedToUsername() : "Unassigned"));
        detailTimeLabel.setText("Submitted: " + request.getFormattedCreatedAtFull());
        detailDescriptionArea.setText(request.getDescription());
        detailDescriptionArea.setWrapText(true);
        detailDescriptionArea.setEditable(false);

        // Load replies
        loadDetailReplies(request.getId());

        // Show the detail view
        hideAllViews();
        requestDetailView.setVisible(true);
        requestDetailView.setManaged(true);
        System.out.println("[TADashboard] requestDetailView is now visible");
    }

    /**
     * Load replies for the detail view.
     */
    private void loadDetailReplies(String requestId) {
        Thread repliesThread = new Thread(() -> {
            try {
                List<com.lms.ui.model.ReplyDto> replies = requestService.getReplies(requestId);
                javafx.application.Platform.runLater(() -> {
                    detailRepliesListView.getItems().clear();
                    if (replies.isEmpty()) {
                        detailRepliesListView.getItems().add("No replies yet.");
                    } else {
                        for (com.lms.ui.model.ReplyDto reply : replies) {
                            String replyText = String.format("[%s] %s: %s",
                                    reply.getFormattedCreatedAt(),
                                    reply.getTaUsername(),
                                    reply.getMessage());
                            detailRepliesListView.getItems().add(replyText);
                        }
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    detailRepliesListView.getItems().clear();
                    detailRepliesListView.getItems().add("Error loading replies: " + e.getMessage());
                });
            }
        });
        repliesThread.setDaemon(true);
        repliesThread.start();
    }

    /**
     * Send reply from detail view.
     */
    /**
     * Refresh request detail view.
     */
    @FXML
    public void refreshRequestDetail() {
        if (currentDetailRequest != null) {
            // Reload the request from server
            Thread refreshThread = new Thread(() -> {
                try {
                    String response = com.lms.ui.service.ApiClient.get("/requests/" + currentDetailRequest.getId());
                    Request updated = requestService.parseRequest(response);
                    javafx.application.Platform.runLater(() -> {
                        currentDetailRequest = updated;
                        showRequestDetail(updated);
                    });
                } catch (Exception e) {
                    System.err.println("Error refreshing request: " + e.getMessage());
                }
            });
            refreshThread.setDaemon(true);
            refreshThread.start();
        }
    }

    /**
     * Show previous view and hide detail view.
     */
    @FXML
    public void showPreviousView() {
        hideAllViews();

        // Show the previous view based on tracking
        switch (previousViewName) {
            case "allPendingView":
                allPendingView.setVisible(true);
                allPendingView.setManaged(true);
                setActiveButton(allPendingBtn);
                break;
            case "inProgressView":
                inProgressView.setVisible(true);
                inProgressView.setManaged(true);
                setActiveButton(inProgressBtn);
                break;
            case "myAssignedView":
                myAssignedView.setVisible(true);
                myAssignedView.setManaged(true);
                setActiveButton(myAssignedBtn);
                break;
            case "allRequestsView":
                allRequestsView.setVisible(true);
                allRequestsView.setManaged(true);
                setActiveButton(allRequestsBtn);
                break;
            case "resolvedView":
                resolvedView.setVisible(true);
                resolvedView.setManaged(true);
                setActiveButton(resolvedBtn);
                break;
            default:
                showAllPending();
        }
    }

    /**
     * Show the reply page for composing a reply to a request.
     */
    private void showReplyPage(Request request) {
        System.out.println("[TADashboard] Showing reply page for request: " + request.getTitle());

        // Reset reply sent flag and disable resolve button
        replyHasBeenSent = false;
        replyPageMarkResolvedButton.setDisable(true);

        // Update reply page with request information
        replyPageTitleLabel.setText("Title: " + request.getTitle());
        replyPageStudentLabel.setText("Student: " + request.getStudentUsername());
        replyPageStatusLabel.setText(request.getStatus().getDisplayName());
        replyPagePriorityLabel.setText("Priority: " + request.getPriority());
        replyPageTimeLabel.setText("Submitted: " + request.getFormattedCreatedAtFull());
        replyPageDescriptionArea.setText(request.getDescription());
        replyPageDescriptionArea.setWrapText(true);
        replyPageDescriptionArea.setEditable(false);

        // Clear the reply text area
        replyPageTextArea.clear();
        replyPageTextArea.setWrapText(true);

        // Store current request for reply submission
        currentDetailRequest = request;

        // Load replies for this request
        loadReplyPageReplies(request.getId());

        // Show the reply page
        hideAllViews();
        replyPageView.setVisible(true);
        replyPageView.setManaged(true);

        System.out.println("[TADashboard] Reply page is now visible");
    }

    /**
     * Load replies for the reply page view.
     */
    private void loadReplyPageReplies(String requestId) {
        Thread repliesThread = new Thread(() -> {
            try {
                List<com.lms.ui.model.ReplyDto> replies = requestService.getReplies(requestId);
                javafx.application.Platform.runLater(() -> {
                    replyPageRepliesListView.getItems().clear();
                    if (replies.isEmpty()) {
                        replyPageRepliesListView.getItems().add("No replies yet.");
                    } else {
                        for (com.lms.ui.model.ReplyDto reply : replies) {
                            String replyText = String.format("[%s] %s: %s",
                                    reply.getFormattedCreatedAt(),
                                    reply.getTaUsername(),
                                    reply.getMessage());
                            replyPageRepliesListView.getItems().add(replyText);
                        }
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    replyPageRepliesListView.getItems().clear();
                    replyPageRepliesListView.getItems().add("Error loading replies: " + e.getMessage());
                    System.err.println("[TADashboard] Error loading replies: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
        repliesThread.setDaemon(true);
        repliesThread.start();
    }

    /**
     * Send reply from the reply page.
     */
    @FXML
    public void sendReplyFromReplyPage() {
        if (currentDetailRequest == null) {
            System.err.println("[TADashboard] ERROR: currentDetailRequest is null!");
            return;
        }

        String replyText = replyPageTextArea.getText().trim();
        if (replyText.isEmpty()) {
            System.out.println("[TADashboard] Reply text is empty, not sending");
            return;
        }

        System.out.println("[TADashboard] Sending reply to request: " + currentDetailRequest.getId());
        replyPageProgressIndicator.setVisible(true);
        replyPageSendButton.setDisable(true);

        // Send reply in background thread
        Thread sendThread = new Thread(() -> {
            try {
                requestService.createReply(currentDetailRequest.getId(), replyText);
                javafx.application.Platform.runLater(() -> {
                    System.out.println("[TADashboard] Reply sent successfully!");
                    replyHasBeenSent = true;
                    replyPageProgressIndicator.setVisible(false);
                    replyPageSendButton.setDisable(false);
                    replyPageMarkResolvedButton.setDisable(false);
                    replyPageTextArea.clear();

                    // Reload the replies
                    loadReplyPageReplies(currentDetailRequest.getId());

                    // Show success message
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Reply Sent");
                    successAlert.setContentText(
                            "Your reply has been sent successfully. You can now mark the request as resolved if needed.");
                    successAlert.showAndWait();
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    System.err.println("[TADashboard] Error sending reply: " + e.getMessage());
                    e.printStackTrace();
                    replyPageProgressIndicator.setVisible(false);
                    replyPageSendButton.setDisable(false);

                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Failed to Send Reply");
                    errorAlert.setContentText("Error: " + e.getMessage());
                    errorAlert.showAndWait();
                });
            }
        });
        sendThread.setDaemon(true);
        sendThread.start();
    }

    /**
     * Mark request as resolved from the reply page.
     */
    @FXML
    public void markAsResolvedFromReplyPage() {
        if (currentDetailRequest == null) {
            System.err.println("[TADashboard] ERROR: currentDetailRequest is null!");
            return;
        }

        if (!replyHasBeenSent) {
            System.err.println("[TADashboard] ERROR: Cannot mark as resolved - reply has not been sent!");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cannot Resolve");
            alert.setHeaderText("Reply Required");
            alert.setContentText("You must send a reply before marking the request as resolved.");
            alert.showAndWait();
            return;
        }

        System.out.println("[TADashboard] Marking request as resolved: " + currentDetailRequest.getId());
        replyPageProgressIndicator.setVisible(true);
        replyPageMarkResolvedButton.setDisable(true);

        // Mark as resolved in background thread
        Thread resolveThread = new Thread(() -> {
            try {
                requestService.resolveRequest(currentDetailRequest.getId());
                javafx.application.Platform.runLater(() -> {
                    System.out.println("[TADashboard] Request marked as resolved!");
                    replyPageProgressIndicator.setVisible(false);

                    // Show success message
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Request Resolved");
                    successAlert.setContentText("The request has been marked as resolved.");
                    successAlert.showAndWait();

                    // Return to previous view
                    showPreviousView();
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    System.err.println("[TADashboard] Error marking request as resolved: " + e.getMessage());
                    e.printStackTrace();
                    replyPageProgressIndicator.setVisible(false);
                    replyPageMarkResolvedButton.setDisable(false);

                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Failed to Resolve Request");
                    errorAlert.setContentText("Error: " + e.getMessage());
                    errorAlert.showAndWait();
                });
            }
        });
        resolveThread.setDaemon(true);
        resolveThread.start();
    }
}
