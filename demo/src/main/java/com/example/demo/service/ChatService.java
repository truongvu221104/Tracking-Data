package com.example.demo.service;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.dto.ChatMessagePayload;
import com.example.demo.model.ChatMessage;
import com.example.demo.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageDto saveMessage(ChatMessagePayload payload) {
        if (payload.getConversationId() == null) {
            throw new IllegalArgumentException("conversationId is required");
        }
        if (payload.getSenderId() == null || payload.getSenderRole() == null) {
            throw new IllegalArgumentException("sender info is required");
        }

        ChatMessage msg = ChatMessage.builder()
                .conversationId(payload.getConversationId())
                .senderId(payload.getSenderId())
                .senderRole(payload.getSenderRole())
                .content(payload.getContent())
                .createdAt(Instant.now())
                .build();

        ChatMessage saved = chatMessageRepository.save(msg);
        return toDto(saved);
    }

    public List<ChatMessageDto> getMessages(Long conversationId) {
        return chatMessageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ChatMessageDto toDto(ChatMessage m) {
        return ChatMessageDto.builder()
                .id(m.getId())
                .conversationId(m.getConversationId())
                .senderId(m.getSenderId())
                .senderRole(m.getSenderRole())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
