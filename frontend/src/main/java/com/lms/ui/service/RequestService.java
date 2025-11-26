package com.lms.ui.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.lms.ui.model.ReplyDto;
import com.lms.ui.model.Request;
import com.lms.ui.model.RequestStatus;
import javafx.application.Platform;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Request service that connects to Spring Boot backend API.
 * Handles all request CRUD operations and real-time updates.
 * IMPROVED: Better error handling, null safety, and parse error prevention.
 */
public class RequestService {

    private static final Gson gson = new Gson();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final List<RequestChangeListener> listeners = new ArrayList<>();

    /**
     * Listener interface for request changes.
     */
    public interface RequestChangeListener {
        void onRequestCreated(Request request);

        void onRequestAssigned(Request request);

        void onRequestResolved(Request request);

        void onRequestUpdated(Request request);
    }

    /**
     * Add listener for request changes.
     */
    public void addListener(RequestChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Create a new request via API.
     */
    public Request createRequest(String title, String description) throws Exception {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }

        JsonObject createRequest = new JsonObject();
        createRequest.addProperty("title", title.trim());
        createRequest.addProperty("description", description.trim());
        createRequest.addProperty("labSessionId", "DEFAULT"); // Use a default lab session

        try {
            String response = ApiClient.post("/requests", createRequest);
            return parseRequest(response);
        } catch (ApiClient.ApiException e) {
            throw new Exception("Failed to create request: " + extractErrorMessage(e));
        } catch (Exception e) {
            throw new Exception("Failed to create request: " + e.getMessage());
        }
    }

    /**
     * Get all requests (for TAs) via API.
     * Returns requests sorted by createdAt ASC (FIFO - oldest first).
     */
    public List<Request> getAllRequests() throws Exception {
        try {
            String response = ApiClient.get("/requests?page=0&size=100&sort=createdAt,asc");
            JsonObject jsonResponse = safeParseObject(response);

            if (!jsonResponse.has("content")) {
                throw new Exception("Invalid response format: missing 'content' field");
            }

            JsonArray content = jsonResponse.getAsJsonArray("content");
            List<Request> requests = new ArrayList<>();

            for (int i = 0; i < content.size(); i++) {
                try {
                    requests.add(parseRequest(content.get(i).toString()));
                } catch (Exception e) {
                    System.err.println("Error parsing request at index " + i + ": " + e.getMessage());
                }
            }

            // Ensure FIFO sorting (oldest first)
            requests.sort((a, b) -> {
                if (a.getCreatedAt() == null && b.getCreatedAt() == null)
                    return 0;
                if (a.getCreatedAt() == null)
                    return 1;
                if (b.getCreatedAt() == null)
                    return -1;
                return a.getCreatedAt().compareTo(b.getCreatedAt());
            });

            return requests;
        } catch (ApiClient.ApiException e) {
            handleAuthError(e);
            throw new Exception("Failed to fetch all requests: " + extractErrorMessage(e));
        }
    }

    /**
     * Get all pending requests (for TAs) via API.
     * Returns requests sorted by createdAt ASC (FIFO - oldest first).
     */
    public List<Request> getPendingRequests() throws Exception {
        try {
            String response = ApiClient.get("/requests?status=PENDING&page=0&size=100&sort=createdAt,asc");
            JsonObject jsonResponse = safeParseObject(response);

            if (!jsonResponse.has("content")) {
                throw new Exception("Invalid response format: missing 'content' field");
            }

            JsonArray content = jsonResponse.getAsJsonArray("content");
            List<Request> requests = new ArrayList<>();

            for (int i = 0; i < content.size(); i++) {
                try {
                    requests.add(parseRequest(content.get(i).toString()));
                } catch (Exception e) {
                    System.err.println("Error parsing pending request at index " + i + ": " + e.getMessage());
                }
            }

            // Ensure FIFO sorting (oldest first)
            requests.sort((a, b) -> {
                if (a.getCreatedAt() == null && b.getCreatedAt() == null)
                    return 0;
                if (a.getCreatedAt() == null)
                    return 1;
                if (b.getCreatedAt() == null)
                    return -1;
                return a.getCreatedAt().compareTo(b.getCreatedAt());
            });

            return requests;
        } catch (ApiClient.ApiException e) {
            handleAuthError(e);
            throw new Exception("Failed to fetch pending requests: " + extractErrorMessage(e));
        }
    }

    /**
     * Get student's requests via API.
     */
    public List<Request> getStudentRequests() throws Exception {
        try {
            String response = ApiClient.get("/requests/my?page=0&size=100");
            JsonObject jsonResponse = safeParseObject(response);

            if (!jsonResponse.has("content")) {
                throw new Exception("Invalid response format: missing 'content' field");
            }

            JsonArray content = jsonResponse.getAsJsonArray("content");
            List<Request> requests = new ArrayList<>();

            for (int i = 0; i < content.size(); i++) {
                try {
                    requests.add(parseRequest(content.get(i).toString()));
                } catch (Exception e) {
                    System.err.println("Error parsing student request at index " + i + ": " + e.getMessage());
                }
            }

            return requests;
        } catch (ApiClient.ApiException e) {
            handleAuthError(e);
            throw new Exception("Failed to fetch your requests: " + extractErrorMessage(e));
        }
    }

    /**
     * Get request by ID via API.
     */
    public Request getRequestById(String id) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be empty");
        }

        try {
            String response = ApiClient.get("/requests/" + id.trim());
            return parseRequest(response);
        } catch (ApiClient.ApiException e) {
            throw new Exception("Failed to fetch request: " + extractErrorMessage(e));
        }
    }

    /**
     * Assign request to TA via API.
     * Improved error handling for concurrent assignment scenarios.
     */
    public Request assignRequest(String requestId) throws Exception {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be empty");
        }

        try {
            String response = ApiClient.put("/requests/" + requestId.trim() + "/assign", new JsonObject());
            Request updated = parseRequest(response);
            notifyListeners(listener -> listener.onRequestAssigned(updated));
            return updated;
        } catch (ApiClient.ApiException e) {
            String errorMsg = extractErrorMessage(e);

            // Handle specific error cases
            if (errorMsg.contains("already assigned") || errorMsg.contains("already claimed")) {
                throw new Exception(
                        "This request has already been assigned to another TA. Please refresh and try again.");
            } else if (errorMsg.contains("409")) {
                throw new Exception("Conflict: Request was modified by another user. Please refresh and try again.");
            } else if (errorMsg.contains("400")) {
                throw new Exception("Invalid request: Cannot assign this request.");
            }

            throw new Exception("Failed to assign request: " + errorMsg);
        }
    }

    /**
     * Resolve request via API.
     */
    public Request resolveRequest(String requestId) throws Exception {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be empty");
        }

        try {
            String response = ApiClient.put("/requests/" + requestId.trim() + "/resolve", new JsonObject());
            Request updated = parseRequest(response);
            notifyListeners(listener -> listener.onRequestResolved(updated));
            return updated;
        } catch (ApiClient.ApiException e) {
            String errorMsg = extractErrorMessage(e);

            if (errorMsg.contains("Can only resolve IN_PROGRESS requests")) {
                throw new Exception("Can only resolve requests that are in progress.");
            } else if (errorMsg.contains("You can only resolve requests assigned to you")) {
                throw new Exception("You can only resolve requests assigned to you.");
            }

            throw new Exception("Failed to resolve request: " + errorMsg);
        }
    }

    /**
     * Update request priority via API.
     */
    public Request updatePriority(String requestId, Long priority) throws Exception {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be empty");
        }
        if (priority == null || priority < 0) {
            throw new IllegalArgumentException("Priority must be a positive number");
        }

        JsonObject priorityRequest = new JsonObject();
        priorityRequest.addProperty("priority", priority);

        try {
            String response = ApiClient.put("/requests/" + requestId.trim() + "/priority", priorityRequest);
            Request updated = parseRequest(response);
            notifyListeners(listener -> listener.onRequestUpdated(updated));
            return updated;
        } catch (ApiClient.ApiException e) {
            throw new Exception("Failed to update priority: " + extractErrorMessage(e));
        }
    }

    /**
     * Update request via API.
     */
    public Request updateRequest(String requestId, String title, String description) throws Exception {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be empty");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }

        JsonObject updateRequest = new JsonObject();
        updateRequest.addProperty("title", title.trim());
        updateRequest.addProperty("description", description.trim());

        try {
            String response = ApiClient.put("/requests/" + requestId.trim(), updateRequest);
            Request updated = parseRequest(response);
            notifyListeners(listener -> listener.onRequestUpdated(updated));
            return updated;
        } catch (ApiClient.ApiException e) {
            String errorMsg = extractErrorMessage(e);

            if (errorMsg.contains("assigned to a TA")) {
                throw new Exception(
                        "Cannot update: Request has been assigned to a TA. Please contact the TA.");
            }

            throw new Exception("Failed to update request: " + errorMsg);
        }
    }

    /**
     * Delete request via API.
     */
    public void deleteRequest(String requestId) throws Exception {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be empty");
        }

        try {
            ApiClient.delete("/requests/" + requestId.trim());
            notifyListeners(listener -> {
                Request req = new Request();
                req.setId(requestId);
                listener.onRequestUpdated(req);
            });
        } catch (ApiClient.ApiException e) {
            throw new Exception("Failed to delete request: " + extractErrorMessage(e));
        }
    }

    /**
     * Create a reply to a request (TA only).
     */
    public ReplyDto createReply(String requestId, String message) throws Exception {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Reply message cannot be empty");
        }

        JsonObject replyRequest = new JsonObject();
        replyRequest.addProperty("message", message.trim());

        try {
            String response = ApiClient.post("/replies/request/" + requestId.trim(), replyRequest);
            return parseReply(response);
        } catch (ApiClient.ApiException e) {
            String errorMsg = extractErrorMessage(e);

            if (errorMsg.contains("You can only reply to requests assigned to you")) {
                throw new Exception("You can only reply to requests assigned to you.");
            }

            throw new Exception("Failed to create reply: " + errorMsg);
        }
    }

    /**
     * Get replies for a request.
     */
    public List<ReplyDto> getReplies(String requestId) throws Exception {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be empty");
        }

        try {
            String response = ApiClient.get("/replies/request/" + requestId.trim());
            JsonArray jsonArray = safeParseArray(response);

            List<ReplyDto> replies = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                try {
                    replies.add(parseReply(jsonArray.get(i).toString()));
                } catch (Exception e) {
                    System.err.println("Error parsing reply at index " + i + ": " + e.getMessage());
                }
            }
            return replies;
        } catch (ApiClient.ApiException e) {
            throw new Exception("Failed to fetch replies: " + extractErrorMessage(e));
        }
    }

    /**
     * Get statistics from backend.
     */
    public Map<String, Object> getStatistics() throws Exception {
        try {
            String response = ApiClient.get("/admin/stats");
            JsonObject stats = safeParseObject(response);

            Map<String, Object> result = new java.util.HashMap<>();
            result.put("pending", safeGetLong(stats, "pendingCount", 0L));
            result.put("inProgress", safeGetLong(stats, "inProgressCount", 0L));
            result.put("resolved", safeGetLong(stats, "resolvedCount", 0L));
            result.put("total", safeGetLong(stats, "totalRequests", 0L));

            return result;
        } catch (Exception e) {
            // Return empty stats if error
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("pending", 0L);
            result.put("inProgress", 0L);
            result.put("resolved", 0L);
            result.put("total", 0L);
            return result;
        }
    }

    /**
     * Setup WebSocket listeners for real-time updates.
     */
    public void setupRealtimeUpdates() {
        WebSocketManager wsManager = WebSocketManager.getInstance();
        if (wsManager == null) {
            System.err.println("WebSocket manager not available");
            return;
        }

        wsManager.addEventListener("request:created", message -> {
            try {
                JsonObject jsonMessage = safeParseObject(message);
                if (jsonMessage.has("payload") && !jsonMessage.get("payload").isJsonNull()) {
                    JsonObject payload = jsonMessage.getAsJsonObject("payload");
                    Request request = parseRequest(payload.toString());
                    Platform.runLater(() -> notifyListeners(listener -> listener.onRequestCreated(request)));
                }
            } catch (Exception e) {
                System.err.println("Error parsing request:created event: " + e.getMessage());
            }
        });

        wsManager.addEventListener("request:assigned", message -> {
            try {
                JsonObject jsonMessage = safeParseObject(message);
                if (jsonMessage.has("payload") && !jsonMessage.get("payload").isJsonNull()) {
                    JsonObject payload = jsonMessage.getAsJsonObject("payload");
                    Request request = parseRequest(payload.toString());
                    Platform.runLater(() -> notifyListeners(listener -> listener.onRequestAssigned(request)));
                }
            } catch (Exception e) {
                System.err.println("Error parsing request:assigned event: " + e.getMessage());
            }
        });

        wsManager.addEventListener("request:resolved", message -> {
            try {
                JsonObject jsonMessage = safeParseObject(message);
                if (jsonMessage.has("payload") && !jsonMessage.get("payload").isJsonNull()) {
                    JsonObject payload = jsonMessage.getAsJsonObject("payload");
                    Request request = parseRequest(payload.toString());
                    Platform.runLater(() -> notifyListeners(listener -> listener.onRequestResolved(request)));
                }
            } catch (Exception e) {
                System.err.println("Error parsing request:resolved event: " + e.getMessage());
            }
        });

        wsManager.addEventListener("request:updated", message -> {
            try {
                JsonObject jsonMessage = safeParseObject(message);
                if (jsonMessage.has("payload") && !jsonMessage.get("payload").isJsonNull()) {
                    JsonObject payload = jsonMessage.getAsJsonObject("payload");
                    Request request = parseRequest(payload.toString());
                    Platform.runLater(() -> notifyListeners(listener -> listener.onRequestUpdated(request)));
                }
            } catch (Exception e) {
                System.err.println("Error parsing request:updated event: " + e.getMessage());
            }
        });
    }

    /**
     * Parse Request from JSON string with comprehensive error handling.
     * Public method for use by controllers.
     */
    public Request parseRequest(String json) throws Exception {
        try {
            JsonObject obj = safeParseObject(json);

            Request request = new Request();

            // Required fields
            request.setId(safeGetString(obj, "id", ""));
            request.setTitle(safeGetString(obj, "title", ""));
            request.setDescription(safeGetString(obj, "description", ""));
            request.setStudentId(safeGetString(obj, "studentId", ""));
            request.setStudentUsername(safeGetString(obj, "studentUsername", "Unknown"));

            // Parse status
            String statusStr = safeGetString(obj, "status", "PENDING");
            try {
                request.setStatus(RequestStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                request.setStatus(RequestStatus.PENDING);
            }

            // Optional fields
            request.setPriority(safeGetLong(obj, "priority", 0L));
            request.setAssignedTo(safeGetString(obj, "assignedTo", null));
            request.setAssignedToUsername(safeGetString(obj, "assignedToUsername", null));

            // Parse timestamps
            request.setCreatedAt(safeParseDateTime(obj, "createdAt"));
            request.setResolvedAt(safeParseDateTime(obj, "resolvedAt"));

            return request;
        } catch (JsonSyntaxException e) {
            throw new Exception("Invalid JSON format: " + e.getMessage());
        }
    }

    /**
     * Parse Reply from JSON string with error handling.
     */
    private ReplyDto parseReply(String json) throws Exception {
        try {
            JsonObject obj = safeParseObject(json);

            ReplyDto reply = new ReplyDto();
            reply.setId(safeGetString(obj, "id", ""));
            reply.setRequestId(safeGetString(obj, "requestId", ""));
            reply.setTaId(safeGetString(obj, "taId", ""));
            reply.setTaUsername(safeGetString(obj, "taUsername", "Unknown TA"));
            reply.setMessage(safeGetString(obj, "message", ""));
            reply.setCreatedAt(safeParseDateTime(obj, "createdAt"));

            return reply;
        } catch (JsonSyntaxException e) {
            throw new Exception("Invalid reply JSON format: " + e.getMessage());
        }
    }

    // ========== SAFE PARSING UTILITY METHODS ==========

    private JsonObject safeParseObject(String json) throws JsonSyntaxException {
        if (json == null || json.trim().isEmpty()) {
            return new JsonObject();
        }
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private JsonArray safeParseArray(String json) throws JsonSyntaxException {
        if (json == null || json.trim().isEmpty()) {
            return new JsonArray();
        }
        return JsonParser.parseString(json).getAsJsonArray();
    }

    private String safeGetString(JsonObject obj, String key, String defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return defaultValue;
    }

    private long safeGetLong(JsonObject obj, String key, long defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            try {
                return obj.get(key).getAsLong();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private LocalDateTime safeParseDateTime(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            try {
                String dateStr = obj.get(key).getAsString();
                return LocalDateTime.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                System.err.println("Error parsing date field '" + key + "': " + e.getMessage());
            }
        }
        return null;
    }

    private String extractErrorMessage(ApiClient.ApiException e) {
        String message = e.getMessage();
        if (message == null) {
            return "Unknown error occurred";
        }

        // Try to extract meaningful error from response body
        if (message.contains("{")) {
            try {
                int start = message.indexOf("{");
                String jsonPart = message.substring(start);
                JsonObject errorObj = JsonParser.parseString(jsonPart).getAsJsonObject();

                if (errorObj.has("message")) {
                    return errorObj.get("message").getAsString();
                } else if (errorObj.has("error")) {
                    return errorObj.get("error").getAsString();
                }
            } catch (Exception ignored) {
                // Fall through to return original message
            }
        }

        return message;
    }

    private void handleAuthError(ApiClient.ApiException e) throws Exception {
        if (e.getMessage().contains("403") || e.getMessage().contains("401") ||
                e.getMessage().contains("Unauthorized")) {
            throw new Exception("Authentication failed. Please log out and log back in.");
        }
    }

    /**
     * Notify all listeners of request changes.
     */
    private void notifyListeners(java.util.function.Consumer<RequestChangeListener> action) {
        for (RequestChangeListener listener : listeners) {
            try {
                action.accept(listener);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }
}