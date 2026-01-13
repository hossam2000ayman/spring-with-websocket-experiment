package com.example.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chat_rooms")
@Schema(description = "Represents a chat room for group conversations")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the chat room", example = "1")
    private Long id;
    
    @Schema(description = "Name of the chat room", example = "General Discussion", required = true)
    private String name;
    
    @Schema(description = "Description of the chat room", example = "A place for general conversations")
    private String description;
    
    @Schema(description = "Timestamp when the chat room was created", example = "2023-12-01T10:30:00")
    private LocalDateTime createdAt;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "chat_room_participants",
        joinColumns = @JoinColumn(name = "chat_room_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Schema(description = "Users participating in the chat room")
    private Set<User> participants = new HashSet<>();
    
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Schema(description = "Messages sent in the chat room")
    private Set<ChatMessage> messages = new HashSet<>();
    
    public ChatRoom() {
        this.createdAt = LocalDateTime.now();
    }
    
    public ChatRoom(String name, String description) {
        this.name = name;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Set<User> getParticipants() {
        return participants;
    }
    
    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }
    
    public void addParticipant(User user) {
        participants.add(user);
    }
    
    public void removeParticipant(User user) {
        participants.remove(user);
    }
    
    public Set<ChatMessage> getMessages() {
        return messages;
    }
    
    public void setMessages(Set<ChatMessage> messages) {
        this.messages = messages;
    }
}