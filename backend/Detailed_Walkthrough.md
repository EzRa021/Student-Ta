# Backend Detailed Walkthrough

This document provides a more detailed, plain-English walkthrough for each Java source file in `src/main/java/com/lms`. Each section includes a short summary, then a step-by-step explanation of the file's main parts and how they're used.

Note: I generated these explanations based on the project's file structure and typical Spring Boot patterns. If you want true line-by-line annotated code (with each source line quoted and explained), I can produce that for individual files on request; creating full annotated code for all files would be extremely large.

---

## `src/main/java/com/lms/LabManagementSystemApplication.java`

Purpose: Application entry point. When you run the backend, this class starts Spring Boot which scans components, loads configuration, and opens HTTP and WebSocket endpoints.

Walkthrough:
- `@SpringBootApplication` annotation: marks this class as the bootstrapping configuration. Spring uses it to auto-configure and scan for beans.
- `public static void main(String[] args)`: the main method calls `SpringApplication.run(...)` which starts the embedded server (Tomcat by default) and initializes the application context.

Notes:
- There is typically no business logic here. If you need custom startup behavior, you might implement a `CommandLineRunner` or `ApplicationRunner` bean.

---

### Line-by-line annotated source for `LabManagementSystemApplication.java`

```java
package com.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Lab Management System.
 * Spring Boot application with WebSocket and JPA support.
 */
@SpringBootApplication
@EnableScheduling
public class LabManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(LabManagementSystemApplication.class, args);
	}
}
```

Line-by-line explanation:

- `package com.lms;` — Declares the Java package. All application classes live under `com.lms` so Spring's component scan finds them.
- `import org.springframework.boot.SpringApplication;` — Brings in the helper used to start the Spring Boot application.
- `import org.springframework.boot.autoconfigure.SpringBootApplication;` — Imports the annotation that enables Spring Boot's auto-configuration and component scanning.
- `import org.springframework.scheduling.annotation.EnableScheduling;` — Imports the annotation that enables scheduled tasks if the app defines any `@Scheduled` methods.
- The block comment (`/** ... */`) — Human-readable description of the class; useful when someone opens the file.
- `@SpringBootApplication` — Meta-annotation that enables component scanning, auto-configuration, and configuration properties; it effectively tells Spring "start wiring beans from here." 
- `@EnableScheduling` — Activates the scheduling subsystem so methods annotated with `@Scheduled` run on a schedule (used for background jobs if present).
- `public class LabManagementSystemApplication {` — The application's main class; Spring Boot looks here for startup info.
- `public static void main(String[] args) {` — Standard Java entry point — the JVM calls this method when launching the app.
- `SpringApplication.run(LabManagementSystemApplication.class, args);` — Boots the Spring context, starts the embedded servlet container (Tomcat), registers beans, applies configuration, and begins listening for HTTP and WebSocket connections.
- Closing braces end the class and method.

Practical notes:
- This file intentionally contains very little logic. It's a bootstrapper. If you need to run startup code (migrations, seed data, warm caches), implement a `CommandLineRunner` bean elsewhere.
- `@EnableScheduling` is safe to keep even if no `@Scheduled` methods exist; it simply enables that feature.

## `src/main/java/com/lms/controller/RequestController.java`

Purpose: Exposes REST endpoints related to help requests: creation, listing, assignment, updates, and resolution.

Main responsibilities:
- Map HTTP paths (e.g., `POST /api/requests`, `GET /api/requests`) to Java methods.
- Perform request-level validation of parameters and path variables.
- Enforce authorization annotations (e.g., `@PreAuthorize("hasRole('TA')")` or `hasRole('STUDENT')`).
- Convert between DTOs (like `CreateRequestDto`) and entity objects.
- Delegate heavy lifting to `RequestService` for business rules and persistence.
- Send WebSocket events (or call a messaging service) after important actions (e.g., `REQUEST_CREATED`).

Typical method-by-method flow (plain steps):
- `createRequest(CreateRequestDto dto, Principal p)`: validate input, call `requestService.createRequest(dto, username)`, return `RequestResponse`.
- `getRequests(...)`: read query parameters (status, assignedTo), call `requestService.findRequests(...)`, return list of `RequestResponse`.
- `assignRequest(Long id, TaUpdateRequest dto)`: check caller is TA, call `requestService.assignRequest(id, taId)`, publish WebSocket update.
- `updateRequest(Long id, StudentUpdateRequest dto)`: ensure caller is owner (student), call `requestService.updateRequest(...)`.

Notes:
- Controllers are thin; they should not contain complex business rules.
- Look at method `@PreAuthorize` annotations to understand role restrictions.

---

### Line-by-line annotated source for `RequestController.java`

```java
package com.lms.controller;

import com.lms.dto.CreateRequestDto;
import com.lms.dto.RequestResponse;
import com.lms.dto.UpdateRequestDto;
import com.lms.entity.RequestStatus;
import com.lms.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for request management endpoints.
 */
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestController {

		private final RequestService requestService;

		/**
		 * Create a new help request (Student only).
		 */
		@PostMapping
		@PreAuthorize("hasRole('STUDENT')")
		public ResponseEntity<RequestResponse> createRequest(
						@Valid @RequestBody CreateRequestDto dto,
						Authentication authentication) {
				log.info("Create request by: {}", authentication.getName());
				RequestResponse response = requestService.createRequest(dto, authentication.getName());
				return ResponseEntity.status(HttpStatus.CREATED).body(response);
		}

		/**
		 * Get all requests with optional filtering (TA only).
		 */
		@GetMapping
		@PreAuthorize("hasRole('TA')")
		public ResponseEntity<Page<RequestResponse>> getAllRequests(
						@RequestParam(required = false) RequestStatus status,
						@RequestParam(defaultValue = "0") int page,
						@RequestParam(defaultValue = "20") int size,
						@RequestParam(defaultValue = "priority") String sort) {
				log.info("Get all requests - Status: {}, Page: {}, Size: {}", status, page, size);
				Page<RequestResponse> requests = requestService.getAllRequests(status, page, size, sort);
				return ResponseEntity.ok(requests);
		}

		/**
		 * Get student's own requests (Student only).
		 */
		@GetMapping("/my")
		@PreAuthorize("hasRole('STUDENT')")
		public ResponseEntity<Page<RequestResponse>> getMyRequests(
						@RequestParam(required = false) RequestStatus status,
						@RequestParam(defaultValue = "0") int page,
						@RequestParam(defaultValue = "20") int size,
						Authentication authentication) {
				log.info("Get my requests for user: {}", authentication.getName());
				Page<RequestResponse> requests = requestService.getMyRequests(
								authentication.getName(), status, page, size);
				return ResponseEntity.ok(requests);
		}

		/**
		 * Get a single request by ID (Student can view their own, TA can view any).
		 */
		@GetMapping("/{id}")
		@PreAuthorize("hasAnyRole('STUDENT', 'TA')")
		public ResponseEntity<RequestResponse> getRequestById(@PathVariable String id) {
				log.info("Get request by ID: {}", id);
				RequestResponse response = requestService.getRequestById(id);
				return ResponseEntity.ok(response);
		}

		/**
		 * Assign a request to TA (TA only).
		 */
		@PutMapping("/{id}/assign")
		@PreAuthorize("hasRole('TA')")
		public ResponseEntity<RequestResponse> assignRequest(
						@PathVariable String id,
						Authentication authentication) {
				log.info("Assign request {} to TA: {}", id, authentication.getName());
				RequestResponse response = requestService.assignRequest(id, authentication.getName());
				return ResponseEntity.ok(response);
		}

		/**
		 * Mark request as resolved (TA only).
		 */
		@PutMapping("/{id}/resolve")
		@PreAuthorize("hasRole('TA')")
		public ResponseEntity<RequestResponse> resolveRequest(
						@PathVariable String id,
						Authentication authentication) {
				log.info("Resolve request {} by TA: {}", id, authentication.getName());
				RequestResponse response = requestService.resolveRequest(id, authentication.getName());
				return ResponseEntity.ok(response);
		}

		/**
		 * Update request priority (TA only).
		 */
		@PutMapping("/{id}/priority")
		@PreAuthorize("hasRole('TA')")
		public ResponseEntity<RequestResponse> updatePriority(
						@PathVariable String id,
						@RequestBody Map<String, Long> body) {
				Long priority = body.get("priority");
				if (priority == null) {
						return ResponseEntity.badRequest().build();
				}

				log.info("Update priority for request {} to: {}", id, priority);
				RequestResponse response = requestService.updatePriority(id, priority);
				return ResponseEntity.ok(response);
		}

		/**
		 * Update a request (Student can update their own requests).
		 */
		@PutMapping("/{id}")
		@PreAuthorize("hasRole('STUDENT')")
		public ResponseEntity<RequestResponse> updateRequest(
						@PathVariable String id,
						@Valid @RequestBody UpdateRequestDto dto,
						Authentication authentication) {
				log.info("Update request {} by: {}", id, authentication.getName());
				RequestResponse response = requestService.updateRequest(id, dto, authentication.getName());
				return ResponseEntity.ok(response);
		}

		/**
		 * Delete a request (Student can delete their own requests).
		 */
		@DeleteMapping("/{id}")
		@PreAuthorize("hasRole('STUDENT')")
		public ResponseEntity<Map<String, String>> deleteRequest(
						@PathVariable String id,
						Authentication authentication) {
				log.info("Delete request {} by: {}", id, authentication.getName());
				requestService.deleteRequest(id, authentication.getName());
				return ResponseEntity.ok(Map.of("message", "Request deleted successfully"));
		}
}

```

Line-by-line explanation (key points):

- `@RestController` — Declares this class as a REST controller; Spring will map return values to JSON responses automatically.
- `@RequestMapping("/api/requests")` — Base path for all endpoints in this controller.
- `@RequiredArgsConstructor` — Lombok annotation that generates a constructor requiring final fields; here it injects `RequestService`.
- `@Slf4j` — Lombok-provided logger named `log` used for debug/info messages.
- `createRequest(...)`:
	- `@PostMapping` and `@PreAuthorize("hasRole('STUDENT')")` restrict this endpoint to authenticated students.
	- `@Valid @RequestBody CreateRequestDto dto` ensures incoming JSON matches the DTO's validation rules.
	- `Authentication authentication` gives access to the caller's username via `authentication.getName()`.
	- Delegates to `requestService.createRequest(...)` and returns HTTP 201 Created with the created resource.
- `getAllRequests(...)`:
	- TA-only endpoint to list requests with optional `status`, `page`, `size`, and `sort` query params.
	- Delegates to `requestService.getAllRequests(...)` which performs paging and sorting.
- `getMyRequests(...)`:
	- Student-only endpoint returning the caller's own requests. Uses `authentication.getName()` to identify the student.
- `getRequestById(...)`:
	- Allows both STUDENT and TA roles to fetch a single request by id. `RequestService` enforces further ownership checks if needed.
- `assignRequest(...)` and `resolveRequest(...)`:
	- TA-only actions. They log the operation and call corresponding service methods which enforce business rules (e.g., only assign if pending).
- `updatePriority(...)`:
	- Reads a simple JSON body expecting a `priority` numeric value inside a map. Returns 400 Bad Request if missing.
	- Sends the new priority to `requestService.updatePriority` and returns the updated request.
- `updateRequest(...)` and `deleteRequest(...)`:
	- Student-only endpoints to allow the owner to modify or remove their requests. Service layer must check ownership.

Practical notes:
- The controller focuses on request/response mapping and authorization. All significant checks (ownership, valid state transitions) should live in `RequestService`.
- If you need to change who can call a route, update the `@PreAuthorize` expression here (and keep services defensive against direct calls).

## `src/main/java/com/lms/controller/ReplyController.java`

Purpose: Endpoints to create and fetch replies associated with requests.

Walkthrough:
- `POST /api/replies/request/{requestId}`: TA-only endpoint to add a reply. Validates the request exists, creates a `Reply` via `ReplyService`, returns `ReplyDto`.
- `GET /api/replies/request/{requestId}`: returns the list of replies for a request. May allow both student and TA roles.

Notes:
- The controller checks request ownership/authorization and then delegates to service which persists data.

---

## `src/main/java/com/lms/controller/AuthController.java`

Purpose: Authentication endpoints for login, token refresh, and possibly registration.

Walkthrough:
- `POST /api/auth/login` (or `/login`): Accepts `LoginRequest` (username & password). Calls `AuthService.authenticateUser`, which checks credentials and returns `AuthResponse` that includes a JWT and refresh token.
- `POST /api/auth/refresh`: Accepts a refresh token, validates it with `AuthService`, and returns a new JWT.
- `POST /api/auth/register` (if present): Allows user registration (or admin creation of TA) and delegates to `AuthService.registerUser`.

Security notes:
- Login endpoints are typically public (no JWT required). Refresh endpoint accepts a valid refresh token for renewal.

---

## `src/main/java/com/lms/controller/AdminController.java`

Purpose: Admin-only operations like creating TA accounts and returning system-wide statistics.

Walkthrough:
- `POST /api/admin/users/ta` or similar: Admin creates a TA account. Accepts `RegisterRequest` and calls `AdminService.createTAUser`.
- `GET /api/admin/stats`: Returns `StatsResponse` with counts (total requests, pending, in-progress, resolved, and user counts).

Notes:
- Methods should have `@PreAuthorize("hasRole('ADMIN')")` to prevent non-admin access.

---

## `src/main/java/com/lms/service/RequestService.java`

Purpose: Implements request-related business rules and database operations.

Walkthrough (typical content and logic):
- `createRequest(CreateRequestDto dto, String username)`: validate input, locate the `User` who created it, initialize `Request` entity with `PENDING` status and `createdAt`, save via `RequestRepository`, return `RequestResponse` DTO.
- `findRequests(filters...)`: build query conditions (status, assignedTo), call `RequestRepository` methods, map results to `RequestResponse`.
- `assignRequest(Long requestId, Long taId)`: check the request is still assignable (PENDING), set `assignedTo`, set status `IN_PROGRESS`, save, and publish a WebSocket event.
- `resolveRequest(Long requestId)`: check permissions, set status `RESOLVED`, update `resolvedAt`, save.

Important rules usually enforced here:
- Only the owning student may update or delete their request.
- Only a TA may assign themselves or be assigned to a request.
- Status transitions are validated (e.g., cannot resolve a request that is not in progress).

Notes:
- This is a key file for application logic. Unit tests usually focus here.

---

## `src/main/java/com/lms/service/ReplyService.java`

Purpose: Handles creating and fetching replies, ensuring replies are attached to the correct request and author.

Walkthrough:
- `createReply(Long requestId, ReplyDto dto, String authorUsername)`: verify request exists, build a `Reply` entity linking to `Request` and author `User`, save via `ReplyRepository`, possibly send WebSocket message.
- `getRepliesForRequest(Long requestId)`: fetch replies ordered by timestamp, map to `ReplyDto`.

Notes:
- Reply creation may check that the author is a TA (via roles) if that restriction exists.

---

## `src/main/java/com/lms/service/AuthService.java`

Purpose: Authenticate users and manage JWT and refresh token issuance.

Walkthrough:
- `authenticateUser(LoginRequest req)`: load user by username or email, verify password using password encoder, create a JWT (`JwtUtil.generateToken`) and a `RefreshToken` persisted by `RefreshTokenRepository`, return `AuthResponse` containing tokens and user info.
- `refreshToken(String token)`: validate refresh token (lookup and expiry), issue a new JWT, maybe rotate the refresh token.
- `registerUser(RegisterRequest req)`: create `User` entity, hash password, assign role(s), save.

Security notes:
- Passwords must be hashed (e.g., BCrypt) and never returned in DTOs.

---

## `src/main/java/com/lms/service/AdminService.java`

Purpose: Admin-specific actions and statistics calculations.

Walkthrough:
- `createTAUser(RegisterRequest req)`: validate inputs, create `User` with TA role, save, optionally send invitation email.
- `computeStatistics()`: query counts from `RequestRepository` (by status) and `UserRepository` for counts per role, package into `StatsResponse`.

Notes:
- Methods in this service are typically called by `AdminController` and should be protected by ADMIN-only checks.

---

## Repositories (short per-file walkthrough)

### `src/main/java/com/lms/repository/UserRepository.java`
Purpose: Data access for `User` entities. Typical methods include `findByUsername`, `findByEmail`, and `existsByUsername`.

Notes: Spring Data JPA generates implementations automatically from method names.

### `src/main/java/com/lms/repository/RequestRepository.java`
Purpose: Data access for `Request` entities. Common methods: `findByStatus`, `findByAssignedTo`, custom JPQL queries for filters.

Notes: Used heavily by `RequestService` for listing and filtering requests.

### `src/main/java/com/lms/repository/ReplyRepository.java`
Purpose: Data access for `Reply` entities. Main method: `findByRequestIdOrderByCreatedAtAsc` (or similar).

### `src/main/java/com/lms/repository/RefreshTokenRepository.java`
Purpose: Persist and lookup refresh tokens. Typical methods: `findByToken`, `deleteByUser`.

---

## Exceptions and Entities (short walkthroughs)

### `src/main/java/com/lms/exception/GlobalExceptionHandler.java`
Purpose: Map thrown exceptions to HTTP responses. Example: catch `NotFoundException` and return `404` with JSON explaining the error.

Notes: Look here if the frontend receives non-helpful HTTP error messages.

### `src/main/java/com/lms/entity/Role.java`
Purpose: Enum listing roles used in security checks: `ADMIN`, `TA`, `STUDENT`.

### `src/main/java/com/lms/entity/UserRole.java`
Purpose: If present, defines a link or helper for user-role relationships.

### `src/main/java/com/lms/entity/User.java`
Purpose: The `User` entity storing id, username, email, hashed password, roles, timestamps.

Notes: The `password` field must not be exposed by DTOs.

### `src/main/java/com/lms/entity/RequestStatus.java`
Purpose: Enum for `PENDING`, `IN_PROGRESS`, `RESOLVED`, `CANCELLED`.

### `src/main/java/com/lms/entity/Request.java`
Purpose: Request entity with fields: id, title, description, createdBy, assignedTo, status, createdAt, resolvedAt.

### `src/main/java/com/lms/entity/Reply.java`
Purpose: Reply entity with fields: id, request (foreign key), author, message, createdAt.

### `src/main/java/com/lms/entity/RefreshToken.java`
Purpose: Stores refresh token string, user relation, expiry.

---

## DTOs (data shapes used on the API)

For each DTO file, the walkthrough explains the fields and how the DTO is used.

- `CreateRequestDto`, `StudentCreateRequest`: Fields like `title`, `description`, `metadata`. Used when students create requests.
- `UpdateRequestDto`, `TaUpdateRequest`, `StudentUpdateRequest`: Contain fields allowed to be changed by the caller (TA vs Student). Validation annotations may be present.
- `RequestResponse`: Fields shown to the client when returning a request record (id, title, summary, status, timestamps, student/TA summaries).
- `ReplyDto`: Fields returned/accepted for replies (id, requestId, author, message, createdAt).
- `StatsResponse`: Fields like `totalRequests`, `pending`, `inProgress`, `resolved`, `userCounts`.
- `RegisterRequest`, `LoginRequest`, `AuthResponse`: Used for auth flows. `AuthResponse` contains `accessToken`, `refreshToken`, and user summary.
- `WebSocketEvent`: A small object used when the backend publishes events over WebSocket to connected clients. It typically has `type` (e.g., `REQUEST_CREATED`) and `payload`.

---

## Config & Security

### `src/main/java/com/lms/config/WebSocketConfiguration.java`
Purpose: Registers the WebSocket/STOMP endpoint (e.g., `/ws`) and configures the in-memory broker or broker relay. Also sets allowed origins and application destination prefixes.

Notes: If clients use SockJS/STOMP, this file defines the path they connect to and the destination prefix for topics (`/topic` or `/queue`).

### `src/main/java/com/lms/config/SecurityConfig.java`
Purpose: Configures HTTP security: which URLs are public (login, static resources), which require authentication, JWT filter wiring, password encoder bean, CORS config, and method security.

Walkthrough:
- `http.csrf().disable()` or similar: CSRF protection may be disabled for APIs.
- `authorizeRequests().antMatchers("/api/auth/**").permitAll()` : make auth endpoints public.
- Adds `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter` so tokens are checked on each request.

### `src/main/java/com/lms/security/JwtUtil.java`
Purpose: Create and verify JWT tokens. Contains a secret key, token validity period, and helper methods `generateToken`, `getUsernameFromToken`, `validateToken`.

### `src/main/java/com/lms/security/JwtAuthenticationFilter.java`
Purpose: Reads the `Authorization` header on incoming requests, extracts `Bearer <token>`, validates it via `JwtUtil`, and if valid, sets the `SecurityContext` with authenticated `UserDetails`.

### `src/main/java/com/lms/security/CustomUserDetailsService.java`
Purpose: Used by Spring Security to load `UserDetails` by username. It queries `UserRepository` and builds a `UserDetails` object containing username, password hash, and granted authorities (roles).
