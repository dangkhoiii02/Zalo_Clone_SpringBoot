package com.zalo.clone.repository;

import com.zalo.clone.domain.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    List<Conversation> findByParticipantsContainingOrderByUpdatedAtDesc(String userId);

    @Query("{ 'type': 'DIRECT', 'participants': { $all: ?0, $size: ?1 } }")
    Optional<Conversation> findDirectConversation(List<String> participants, int size);
}
