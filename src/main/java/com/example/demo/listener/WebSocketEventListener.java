package com.example.demo.listener;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private UserService userService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection");
        // We can track connection here if needed, but JOIN message handles the UI
        // update
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        Principal principal = (Principal) headerAccessor.getUser();
        if (principal != null) {
            String username = principal.getName(); // In our case, this is email

            try {
                User user = userService.findByEmail(username);
                if (user != null) {
                    logger.info("User Disconnected : " + username);

                    // Update online status in DB
                    userService.setOnlineStatus(user.getId(), false);

                    ChatMessageDto chatMessage = new ChatMessageDto();
                    chatMessage.setType("LEAVE");
                    chatMessage.setSenderId(user.getId());
                    chatMessage.setSenderName(user.getName());

                    messagingTemplate.convertAndSend("/topic/public", chatMessage);
                }
            } catch (Exception e) {
                logger.error("Error handling disconnect for user: " + username, e);
            }
        }
    }
}
