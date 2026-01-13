package com.example.demo.repository;

import com.example.demo.model.ChatRoom;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    List<ChatRoom> findByParticipantsContaining(User user);
    
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.participants p WHERE p = :user")
    List<ChatRoom> findChatRoomsByParticipant(@Param("user") User user);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.name LIKE %:name%")
    List<ChatRoom> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.participants p1 JOIN cr.participants p2 WHERE p1 = :user1 AND p2 = :user2 AND p1 != p2")
    List<ChatRoom> findDirectChatRoomsBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
    
    boolean existsByName(String name);
}