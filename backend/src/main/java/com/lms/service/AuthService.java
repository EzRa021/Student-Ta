package com.lms.service;

import com.lms.dto.AuthResponse;
import com.lms.dto.LoginRequest;
import com.lms.dto.RegisterRequest;
import com.lms.dto.StudentCreateRequest;
import com.lms.entity.RefreshToken;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.repository.RefreshTokenRepository;
import com.lms.repository.UserRepository;
import com.lms.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for authentication and authorization operations.
 * Handles user registration, login, and token management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user. Only STUDENT role allowed for open registration.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // SECURITY: Force STUDENT role for public registration
        // Ignore any role provided in the request to prevent privilege escalation
        Role role = Role.STUDENT;

        // Log security attempt if someone tries to register with elevated role
        if (request.getRole() != null && request.getRole() != Role.STUDENT) {
            log.warn("Security: Attempt to register with role {} blocked. User: {}",
                    request.getRole(), request.getUsername());
        }

        // Create new user with STUDENT role only
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .studentId(request.getStudentId())
                .build();

        user.addRole(role);
        user = userRepository.save(user);

        log.info("Student registered successfully: {}", user.getUsername());

        // Generate tokens
        return generateAuthResponse(user);
    }

    /**
     * Create a student account.
     */
    @Transactional
    public User createStudentAccount(StudentCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .studentId(request.getStudentId())
                .build();

        user.addRole(Role.STUDENT);
        user = userRepository.save(user);

        log.info("Student account created by admin: {}", user.getUsername());
        return user;
    }

    /**
     * Register a TA account.
     */
    @Transactional
    public AuthResponse registerTA(RegisterRequest request) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user with TA role
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .studentId(request.getStudentId())
                .build();

        user.addRole(Role.TA);
        user = userRepository.save(user);

        log.info("TA account created by admin: {}", user.getUsername());

        // Generate tokens
        return generateAuthResponse(user);
    }

    /**
     * Authenticate user and generate JWT tokens.
     * Supports login with either username or Student/TA ID.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // Try to find by username first
        User user = userRepository.findByUsernameWithRoles(request.getUsername())
                .orElseGet(() -> {
                    // If not found by username, try by Student/TA ID
                    return userRepository.findByStudentIdWithRoles(request.getUsername())
                            .orElseThrow(() -> new IllegalArgumentException("User not found"));
                });

        log.info("User logged in successfully: {}", user.getUsername());

        return generateAuthResponse(user);
    }

    /**
     * Refresh access token using refresh token.
     */
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new IllegalArgumentException("Refresh token expired");
        }

        User user = userRepository.findByUsernameWithRoles(
                userRepository.findById(storedToken.getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("User not found"))
                        .getUsername())
                .orElseThrow();

        return generateAuthResponse(user);
    }

    /**
     * Generate authentication response with access and refresh tokens.
     */
    private AuthResponse generateAuthResponse(User user) {
        org.springframework.security.core.userdetails.UserDetails userDetails = org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(user.getRoles().stream()
                        .map(role -> "ROLE_" + role.getRole().name())
                        .toArray(String[]::new))
                .build();

        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // Save refresh token
        RefreshToken token = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(token);

        List<String> roles = user.getRoles().stream()
                .map(ur -> ur.getRole().name())
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .userId(user.getId())
                .roles(roles)
                .build();
    }

    /**
     * Logout user by invalidating refresh token.
     */
    @Transactional
    public void logout(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("User logged out: {}", userId);
    }
}