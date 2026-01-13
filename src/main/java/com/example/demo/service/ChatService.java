package com.example.demo.service;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.ChatRoom;
import com.example.demo.model.User;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    public ChatMessage sendMessage(String content, User sender, User receiver, Long chatRoomId) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(content);
        chatMessage.setSender(sender);
        chatMessage.setReceiver(receiver);
        
        if (chatRoomId != null) {
            Optional<ChatRoom> chatRoom = chatRoomRepository.findById(chatRoomId);
            chatRoom.ifPresent(chatMessage::setChatRoom);
        }
        
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getConversationBetweenUsers(Long user1Id, Long user2Id) {
        User user1 = new User();
        user1.setId(user1Id);
        User user2 = new User();
        user2.setId(user2Id);
        
        return chatMessageRepository.findConversationBetweenUsers(user1, user2);
    }

    public List<ChatMessage> getChatRoomMessages(Long chatRoomId) {
        return chatMessageRepository.findByChatRoom_IdOrderByTimestampAsc(chatRoomId);
    }

    public ChatRoom createChatRoom(String name, String description, List<User> participants) {
        ChatRoom chatRoom = new ChatRoom(name, description);
        
        if (participants != null) {
            participants.forEach(chatRoom::addParticipant);
        }
        
        return chatRoomRepository.save(chatRoom);
    }

    public ChatRoom createDirectChatRoom(User user1, User user2) {
        String roomName = "Direct: " + user1.getName() + " & " + user2.getName();
        ChatRoom chatRoom = new ChatRoom(roomName, "Direct chat between " + user1.getName() + " and " + user2.getName());
        
        chatRoom.addParticipant(user1);
        chatRoom.addParticipant(user2);
        
        return chatRoomRepository.save(chatRoom);
    }

    public List<ChatRoom> getUserChatRooms(Long userId) {
        User user = new User();
        user.setId(userId);
        
        return chatRoomRepository.findByParticipantsContaining(user);
    }

    public void addUserToChatRoom(Long chatRoomId, User user) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findById(chatRoomId);
        if (chatRoom.isPresent()) {
            chatRoom.get().addParticipant(user);
            chatRoomRepository.save(chatRoom.get());
        }
    }

    public void removeUserFromChatRoom(Long chatRoomId, User user) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findById(chatRoomId);
        if (chatRoom.isPresent()) {
            chatRoom.get().removeParticipant(user);
            chatRoomRepository.save(chatRoom.get());
        }
    }

    public long getUnreadMessageCount(Long senderId, Long receiverId, LocalDateTime since) {
        User sender = new User();
        sender.setId(senderId);
        User receiver = new User();
        receiver.setId(receiverId);
        
        return chatMessageRepository.countUnreadMessages(sender, receiver, since);
    }

    public List<ChatMessage> getRecentMessages(LocalDateTime since) {
        return chatMessageRepository.findMessagesSince(since);
    }

    public Optional<ChatRoom> findDirectChatRoom(Long user1Id, Long user2Id) {
        User user1 = new User();
        user1.setId(user1Id);
        User user2 = new User();
        user2.setId(user2Id);
        
        List<ChatRoom> rooms = chatRoomRepository.findDirectChatRoomsBetweenUsers(user1, user2);
        return rooms.isEmpty() ? Optional.empty() : Optional.of(rooms.get(0));
    }
}