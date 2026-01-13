package com.example.demo.controller;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.model.ChatMessage;
import com.example.demo.model.User;
import com.example.demo.service.ChatService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageDto sendMessage(@Payload ChatMessageDto chatMessageDto) {
        try {
            User sender = userService.findById(chatMessageDto.getSenderId());
            User receiver = userService.findById(chatMessageDto.getReceiverId());
            
            ChatMessage chatMessage = chatService.sendMessage(
                chatMessageDto.getContent(),
                sender,
                receiver,
                null
            );
            
            ChatMessageDto responseDto = convertToDto(chatMessage);
            
            messagingTemplate.convertAndSendToUser(
                String.valueOf(receiver.getId()),
                "/queue/messages",
                responseDto
            );
            
            return responseDto;
        } catch (Exception e) {
            chatMessageDto.setType("ERROR");
            chatMessageDto.setContent("Failed to send message: " + e.getMessage());
            return chatMessageDto;
        }
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessageDto addUser(@Payload ChatMessageDto chatMessageDto) {
        try {
            User user = userService.findById(chatMessageDto.getSenderId());
            chatMessageDto.setType("JOIN");
            chatMessageDto.setSenderName(user.getName());
            
            messagingTemplate.convertAndSend("/topic/public", chatMessageDto);
            
            return chatMessageDto;
        } catch (Exception e) {
            chatMessageDto.setType("ERROR");
            chatMessageDto.setContent("Failed to join chat: " + e.getMessage());
            return chatMessageDto;
        }
    }

    @MessageMapping("/chat.sendToRoom")
    public void sendToRoom(@Payload ChatMessageDto chatMessageDto) {
        try {
            User sender = userService.findById(chatMessageDto.getSenderId());
            
            ChatMessage chatMessage = chatService.sendMessage(
                chatMessageDto.getContent(),
                sender,
                null,
                chatMessageDto.getChatRoomId()
            );
            
            ChatMessageDto responseDto = convertToDto(chatMessage);
            
            messagingTemplate.convertAndSend(
                "/topic/room/" + chatMessageDto.getChatRoomId(),
                responseDto
            );
        } catch (Exception e) {
            ChatMessageDto errorDto = new ChatMessageDto();
            errorDto.setType("ERROR");
            errorDto.setContent("Failed to send message: " + e.getMessage());
            messagingTemplate.convertAndSend(
                "/topic/room/" + chatMessageDto.getChatRoomId(),
                errorDto
            );
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
        
        dto.setType("CHAT");
        return dto;
    }
}