package com.example.microfinance_loan_system.config;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public void blacklistToken(String token) {
        if (token != null) {
            blacklistedTokens.add(token);
        }
    }

    public boolean isBlacklisted(String token) {
        return token != null && blacklistedTokens.contains(token);
    }
}
