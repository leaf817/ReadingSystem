package org.example.modules.user.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {

    private final Map<String, String> tokenToUsername = new ConcurrentHashMap<>();

    public void bind(String token, String username) {
        if (token == null || token.isBlank() || username == null || username.isBlank()) {
            return;
        }
        tokenToUsername.put(token, username);
    }

    public void unbind(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        tokenToUsername.remove(token);
    }

    public Optional<String> findUsername(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokenToUsername.get(token));
    }
}
