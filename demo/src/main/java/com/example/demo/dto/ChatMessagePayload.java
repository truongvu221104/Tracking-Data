package com.example.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessagePayload {
    private Long conversationId; // userId của customer
    private Long senderId;       // AppUser.id
    private String senderRole;   // "ADMIN" hoặc "USER"
    private String content;
}
