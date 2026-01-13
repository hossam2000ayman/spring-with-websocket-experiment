package com.example.demo.controller;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.model.ChatMessage;
import com.example.demo.model.ChatRoom;
import com.example.demo.model.User;
import com.example.demo.service.ChatService;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat API", description = "Endpoints for chat functionality")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.example.demo.repository.ChatRoomRepository chatRoomRepository;

    @PostMapping("/messages")
    @Operation(summary = "Send a message", description = "Send a message from one user to another")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @RequestBody ChatMessageDto messageDto) {
        try {
            User sender = userService.findById(messageDto.getSenderId());
            User receiver = null;
            if (messageDto.getReceiverId() != null) {
                receiver = userService.findById(messageDto.getReceiverId());
            }
            
            ChatMessage chatMessage = chatService.sendMessage(
                messageDto.getContent(),
                sender,
                receiver,
                messageDto.getChatRoomId()
            );
            
            ChatMessageDto responseDto = convertToDto(chatMessage);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/messages/conversation")
    @Operation(summary = "Get conversation between users", description = "Retrieve all messages between two users")
    public ResponseEntity<List<ChatMessageDto>> getConversation(
            @Parameter(description = "ID of the first user") @RequestParam Long user1Id,
            @Parameter(description = "ID of the second user") @RequestParam Long user2Id) {
        try {
            List<ChatMessage> messages = chatService.getConversationBetweenUsers(user1Id, user2Id);
            List<ChatMessageDto> messageDtos = messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            return ResponseEntity.ok(messageDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/messages/room/{roomId}")
    @Operation(summary = "Get chat room messages", description = "Retrieve all messages in a specific chat room")
    public ResponseEntity<List<ChatMessageDto>> getChatRoomMessages(
            @Parameter(description = "ID of the chat room") @PathVariable Long roomId) {
        try {
            List<ChatMessage> messages = chatService.getChatRoomMessages(roomId);
            List<ChatMessageDto> messageDtos = messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            return ResponseEntity.ok(messageDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/rooms")
    @Operation(summary = "Create a chat room", description = "Create a new chat room with participants")
    public ResponseEntity<ChatRoomDto> createChatRoom(
            @RequestBody CreateChatRoomRequest request) {
        try {
            List<User> participants = request.getParticipantIds().stream()
                .map(userService::findById)
                .collect(Collectors.toList());
            
            ChatRoom chatRoom = chatService.createChatRoom(
                request.getName(),
                request.getDescription(),
                participants
            );
            
            ChatRoomDto responseDto = convertChatRoomToDto(chatRoom);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/rooms/direct")
    @Operation(summary = "Create direct chat room", description = "Create a direct chat room between two users")
    public ResponseEntity<ChatRoomDto> createDirectChatRoom(
            @RequestBody DirectChatRoomRequest request) {
        try {
            User user1 = userService.findById(request.getUser1Id());
            User user2 = userService.findById(request.getUser2Id());
            
            Optional<ChatRoom> existingRoom = chatService.findDirectChatRoom(
                request.getUser1Id(), 
                request.getUser2Id()
            );
            
            if (existingRoom.isPresent()) {
                return ResponseEntity.ok(convertChatRoomToDto(existingRoom.get()));
            }
            
            ChatRoom chatRoom = chatService.createDirectChatRoom(user1, user2);
            ChatRoomDto responseDto = convertChatRoomToDto(chatRoom);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/rooms/{roomId}/users")
    @Operation(summary = "Add user to chat room", description = "Add a user to an existing chat room")
    public ResponseEntity<ChatRoomDto> addUserToRoom(
            @PathVariable Long roomId,
            @RequestParam Long userId) {
        try {
            User user = userService.findById(userId);
            chatService.addUserToChatRoom(roomId, user);
            
            ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow();
            ChatRoomDto responseDto = convertChatRoomToDto(chatRoom);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/rooms/{roomId}/users/{userId}")
    @Operation(summary = "Remove user from chat room", description = "Remove a user from a chat room")
    public ResponseEntity<ChatRoomDto> removeUserFromRoom(
            @PathVariable Long roomId,
            @PathVariable Long userId) {
        try {
            User user = userService.findById(userId);
            chatService.removeUserFromChatRoom(roomId, user);
            
            ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow();
            ChatRoomDto responseDto = convertChatRoomToDto(chatRoom);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/rooms/user/{userId}")
    @Operation(summary = "Get user chat rooms", description = "Retrieve all chat rooms for a specific user")
    public ResponseEntity<List<ChatRoomDto>> getUserChatRooms(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        try {
            List<ChatRoom> chatRooms = chatService.getUserChatRooms(userId);
            List<ChatRoomDto> chatRoomDtos = chatRooms.stream()
                .map(this::convertChatRoomToDto)
                .collect(Collectors.toList());
            return ResponseEntity.ok(chatRoomDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/messages/unread")
    @Operation(summary = "Get unread message count", description = "Count unread messages from a sender")
    public ResponseEntity<Long> getUnreadMessageCount(
            @Parameter(description = "ID of the sender") @RequestParam Long senderId,
            @Parameter(description = "ID of the receiver") @RequestParam Long receiverId,
            @Parameter(description = "Timestamp since when to count") @RequestParam LocalDateTime since) {
        try {
            long count = chatService.getUnreadMessageCount(senderId, receiverId, since);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private ChatMessageDto convertToDto(ChatMessage chatMessage) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(chatMessage.getId());
        dto.setContent(chatMessage.getContent());
        dto.setTimestamp(chatMessage.getTimestamp());
        dto.setSenderId(chatMessage.getSender().getId());
        dto.setSenderName(chatMessage.getSender().getName());
        
        if (chatMessage.getReceiver() != null) {
            dto.setReceiverId(chatMessage.getReceiver().getId());
            dto.setReceiverName(chatMessage.getReceiver().getName());
        }
        
        if (chatMessage.getChatRoom() != null) {
            dto.setChatRoomId(chatMessage.getChatRoom().getId());
        }
        
        return dto;
    }

    private ChatRoomDto convertChatRoomToDto(ChatRoom chatRoom) {
        ChatRoomDto dto = new ChatRoomDto();
        dto.setId(chatRoom.getId());
        dto.setName(chatRoom.getName());
        dto.setDescription(chatRoom.getDescription());
        dto.setCreatedAt(chatRoom.getCreatedAt());
        dto.setParticipantIds(chatRoom.getParticipants().stream()
            .map(User::getId)
            .collect(Collectors.toList()));
        dto.setParticipantNames(chatRoom.getParticipants().stream()
            .map(User::getName)
            .collect(Collectors.toList()));
        return dto;
    }

    public static class ChatRoomDto {
        private Long id;
        private String name;
        private String description;
        private LocalDateTime createdAt;
        private List<Long> participantIds;
        private List<String> participantNames;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public List<Long> getParticipantIds() { return participantIds; }
        public void setParticipantIds(List<Long> participantIds) { this.participantIds = participantIds; }
        public List<String> getParticipantNames() { return participantNames; }
        public void setParticipantNames(List<String> participantNames) { this.participantNames = participantNames; }
    }

    public static class CreateChatRoomRequest {
        private String name;
        private String description;
        private List<Long> participantIds;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<Long> getParticipantIds() { return participantIds; }
        public void setParticipantIds(List<Long> participantIds) { this.participantIds = participantIds; }
    }

    public static class DirectChatRoomRequest {
        private Long user1Id;
        private Long user2Id;

        public Long getUser1Id() { return user1Id; }
        public void setUser1Id(Long user1Id) { this.user1Id = user1Id; }
        public Long getUser2Id() { return user2Id; }
        public void setUser2Id(Long user2Id) { this.user2Id = user2Id; }
    }
}