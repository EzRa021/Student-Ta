package com.lms.security;

import com.lms.entity.User;
import com.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Custom UserDetailsService implementation for loading user-specific data.
 * Integrates with Spring Security for authentication.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        @Transactional(readOnly = true)
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                // Try to find by username first
                User user = userRepository.findByUsernameWithRoles(username)
                                .orElse(null);

                // If not found by username, try to find by studentId
                // This allows users to login with either their username or their Student/TA ID
                if (user == null) {
                        user = userRepository.findByStudentIdWithRoles(username)
                                        .orElseThrow(() -> new UsernameNotFoundException(
                                                        "User not found: " + username));
                }

                return org.springframework.security.core.userdetails.User.builder()
                                .username(user.getUsername())
                                .password(user.getPasswordHash())
                                .authorities(user.getRoles().stream()
                                                .map(role -> new SimpleGrantedAuthority(
                                                                "ROLE_" + role.getRole().name()))
                                                .collect(Collectors.toList()))
                                .build();
        }
}