package com.zalo.clone.repository;

import com.zalo.clone.domain.Friendship;
import com.zalo.clone.domain.Friendship.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.userId = :userId AND f.friendId = :friendId) OR " +
           "(f.userId = :friendId AND f.friendId = :userId)")
    Optional<Friendship> findFriendship(@Param("userId") UUID userId,
                                         @Param("friendId") UUID friendId);

    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.userId = :userId OR f.friendId = :userId) AND f.status = :status " +
           "ORDER BY f.createdAt DESC")
    List<Friendship> findByUserIdAndStatus(@Param("userId") UUID userId,
                                            @Param("status") FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE f.friendId = :userId AND f.status = 'PENDING' " +
           "ORDER BY f.createdAt DESC")
    List<Friendship> findPendingRequests(@Param("userId") UUID userId);
}
