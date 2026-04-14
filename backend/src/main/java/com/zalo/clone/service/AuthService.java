package com.zalo.clone.service;

import com.zalo.clone.domain.User;
import com.zalo.clone.dto.AuthResponse;
import com.zalo.clone.dto.LoginRequest;
import com.zalo.clone.dto.RegisterRequest;
import com.zalo.clone.repository.UserRepository;
import com.zalo.clone.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * AuthService — equivalent to Go's usecase.AuthUsecase.
 * Handles registration, login, and JWT generation.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthResponse register(RegisterRequest req) {
        // Check if email exists
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("user already exists");
        }

        // Check if username is taken
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("username already taken");
        }

        // Create user
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .avatarUrl("")
                .bio("")
                .build();

        user = userRepository.save(user);

        // Generate JWT
        String token = tokenProvider.generateToken(user.getId(), user.getUsername(), user.getEmail());

        return new AuthResponse(token, user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("invalid credentials");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getUsername(), user.getEmail());

        return new AuthResponse(token, user);
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));
    }
}
