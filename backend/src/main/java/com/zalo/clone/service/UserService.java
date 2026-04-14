package com.zalo.clone.service;

import com.zalo.clone.domain.User;
import com.zalo.clone.dto.UpdateProfileRequest;
import com.zalo.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * UserService — equivalent to Go's usecase.UserUsecase.
 * Handles profile management and user search.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getProfile(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));
    }

    public User updateProfile(UUID userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        if (req.getUsername() != null) {
            // Check if username is taken by another user
            userRepository.findByUsername(req.getUsername())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(userId)) {
                            throw new RuntimeException("username already taken");
                        }
                    });
            user.setUsername(req.getUsername());
        }

        if (req.getAvatarUrl() != null) {
            user.setAvatarUrl(req.getAvatarUrl());
        }

        if (req.getBio() != null) {
            user.setBio(req.getBio());
        }

        return userRepository.save(user);
    }

    public List<User> searchUsers(String query) {
        if (query == null || query.length() < 2) {
            throw new RuntimeException("search query must be at least 2 characters");
        }
        return userRepository.searchUsers(query, PageRequest.of(0, 20));
    }
}
