package com.example.demo.repository;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findBySenderAndReceiverOrderByTimestampAsc(User sender, User receiver);
    
    List<ChatMessage> findByChatRoom_IdOrderByTimestampAsc(Long chatRoomId);
    
    @Query("SELECT m FROM ChatMessage m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.timestamp ASC")
    List<ChatMessage> findConversationBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.timestamp >= :since ORDER BY m.timestamp ASC")
    List<ChatMessage> findMessagesSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.sender = :sender AND m.receiver = :receiver AND m.timestamp >= :since")
    long countUnreadMessages(@Param("sender") User sender, @Param("receiver") User receiver, @Param("since") LocalDateTime since);
}