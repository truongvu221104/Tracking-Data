package com.example.demo.controller;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.dto.ChatMessagePayload;
import com.example.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // REST: lấy lịch sử
    @GetMapping("/api/chat/conversations/{conversationId}/messages")
    public List<ChatMessageDto> getMessages(@PathVariable Long conversationId) {
        return chatService.getMessages(conversationId);
    }

    @MessageMapping("/chat/{conversationId}")
    public void sendMessage(
            @DestinationVariable Long conversationId,
            @Payload ChatMessagePayload payload
    ) {
        try {
            payload.setConversationId(conversationId);
            ChatMessageDto saved = chatService.saveMessage(payload);

            Long roomId = saved.getConversationId();

            messagingTemplate.convertAndSend("/topic/chat/" + roomId, saved);
            if ("USER".equalsIgnoreCase(saved.getSenderRole())) {
                messagingTemplate.convertAndSend("/topic/chat/admin", saved);
            }
        } catch (Exception ex) {
            log.error("Error handling chat message for conversation {}", conversationId, ex);
        }
    }

}
