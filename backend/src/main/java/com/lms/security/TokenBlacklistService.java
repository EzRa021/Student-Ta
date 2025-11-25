package com.lms.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for managing blacklisted (revoked) JWT tokens.
 * Tokens are added to the blacklist on logout to prevent their reuse.
 * 
 * Note: In production, consider using Redis for distributed caching
 * across multiple server instances.
 */
@Service
@Slf4j
public class TokenBlacklistService {

    private final ConcurrentMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Add token to blacklist on logout.
     * Token will be stored with its expiration time for automatic cleanup.
     */
    public void blacklistToken(String token, Long expirationTime) {
        blacklistedTokens.put(token, expirationTime);
        log.debug("Token added to blacklist. Total blacklisted tokens: {}", blacklistedTokens.size());
    }

    /**
     * Check if token is blacklisted.
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    /**
     * Cleanup expired tokens from blacklist.
     * Runs every hour to prevent memory leaks from accumulated tokens.
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        int beforeSize = blacklistedTokens.size();

        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < currentTime);

        int afterSize = blacklistedTokens.size();
        if (beforeSize > afterSize) {
            log.info("Token blacklist cleanup: removed {} expired tokens. Remaining: {}",
                    beforeSize - afterSize, afterSize);
        }
    }

    /**
     * Get current size of blacklist (for monitoring).
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }
}
