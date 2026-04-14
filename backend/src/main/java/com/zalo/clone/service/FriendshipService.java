package com.zalo.clone.service;

import com.zalo.clone.domain.Friendship;
import com.zalo.clone.domain.Friendship.FriendshipStatus;
import com.zalo.clone.domain.User;
import com.zalo.clone.dto.FriendInfo;
import com.zalo.clone.repository.FriendshipRepository;
import com.zalo.clone.repository.PresenceRepository;
import com.zalo.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * FriendshipService — equivalent to Go's usecase.FriendshipUsecase.
 * Handles friend requests, acceptance, removal, and listing.
 */
@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final PresenceRepository presenceRepository;

    public Friendship sendRequest(UUID userId, UUID friendId) {
        if (userId.equals(friendId)) {
            throw new RuntimeException("cannot send friend request to yourself");
        }

        // Check if friend exists
        userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        // Check existing friendship
        if (friendshipRepository.findFriendship(userId, friendId).isPresent()) {
            throw new RuntimeException("friendship already exists");
        }

        Friendship friendship = Friendship.builder()
                .userId(userId)
                .friendId(friendId)
                .status(FriendshipStatus.PENDING)
                .build();

        return friendshipRepository.save(friendship);
    }

    public void acceptRequest(UUID userId, UUID friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("friendship not found"));

        // Only the recipient can accept
        if (!friendship.getFriendId().equals(userId)) {
            throw new RuntimeException("only the request recipient can accept");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new RuntimeException("friendship is not in pending state");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
    }

    public void removeFriend(UUID userId, UUID friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("friendship not found"));

        // Only participants can remove
        if (!friendship.getUserId().equals(userId) && !friendship.getFriendId().equals(userId)) {
            throw new RuntimeException("not authorized to remove this friendship");
        }

        friendshipRepository.delete(friendship);
    }

    public List<FriendInfo> getFriends(UUID userId) {
        List<Friendship> friendships = friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED);

        List<FriendInfo> friends = new ArrayList<>();
        List<String> userIds = new ArrayList<>();

        for (Friendship f : friendships) {
            UUID friendUserId = f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId();
            User friendUser = userRepository.findById(friendUserId).orElse(null);
            if (friendUser == null) continue;

            userIds.add(friendUserId.toString());

            friends.add(FriendInfo.builder()
                    .friendshipId(f.getId())
                    .userId(friendUserId)
                    .username(friendUser.getUsername())
                    .email(friendUser.getEmail())
                    .avatarUrl(friendUser.getAvatarUrl())
                    .bio(friendUser.getBio())
                    .status(f.getStatus())
                    .createdAt(f.getCreatedAt())
                    .build());
        }

        // Enrich with online status
        Map<String, Boolean> onlineMap = presenceRepository.getOnlineUsers(userIds);
        for (FriendInfo fi : friends) {
            fi.setOnline(Boolean.TRUE.equals(onlineMap.get(fi.getUserId().toString())));
        }

        return friends;
    }

    public List<FriendInfo> getPendingRequests(UUID userId) {
        List<Friendship> requests = friendshipRepository.findPendingRequests(userId);

        List<FriendInfo> result = new ArrayList<>();
        for (Friendship f : requests) {
            User sender = userRepository.findById(f.getUserId()).orElse(null);
            if (sender == null) continue;

            result.add(FriendInfo.builder()
                    .friendshipId(f.getId())
                    .userId(sender.getId())
                    .username(sender.getUsername())
                    .email(sender.getEmail())
                    .avatarUrl(sender.getAvatarUrl())
                    .bio(sender.getBio())
                    .status(f.getStatus())
                    .createdAt(f.getCreatedAt())
                    .build());
        }

        return result;
    }
}
