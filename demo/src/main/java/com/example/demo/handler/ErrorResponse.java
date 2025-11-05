package com.example.demo.handler;

import lombok.*;
import java.time.Instant;
import java.util.Map;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class ErrorResponse {
    private String error;          // validation_failed | bad_request | unauthorized | forbidden | not_found | server_error
    private String message;        // mô tả ngắn
    private Map<String, String> details; // cho lỗi field
    private String path;           // URI
    private String requestId;      // từ MDC
    private Instant timestamp;     // thời điểm
    private Integer status;        // HTTP status code
}
