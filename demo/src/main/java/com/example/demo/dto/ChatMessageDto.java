package com.example.demo.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderRole;
    private String content;
    private Instant createdAt;
}
