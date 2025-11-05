package com.example.demo.handler;

import com.example.demo.log.RequestIdFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponse base(HttpStatus status, String code, String message,
                               Map<String,String> details, HttpServletRequest req) {
        return ErrorResponse.builder()
                .error(code)
                .message(message)
                .details(details)
                .path(req.getRequestURI())
                .requestId(req.getHeader(RequestIdFilter.HDR))
                .timestamp(Instant.now())
                .status(status.value())
                .build();
    }

    // 400 - Validation body (@Valid DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var details = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (a,b)->a
                ));
        log.debug("Validation failed: {}", details);
        return ResponseEntity.badRequest().body(base(HttpStatus.BAD_REQUEST, "validation_failed",
                "Dữ liệu không hợp lệ", details, req));
    }

    // 400 - Sai tham số query/path
    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, IllegalArgumentException.class })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest req) {
        log.info("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(base(HttpStatus.BAD_REQUEST, "bad_request",
                ex.getMessage(), null, req));
    }

    // 401 - Sai xác thực (login)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        log.info("Unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(base(HttpStatus.UNAUTHORIZED, "unauthorized",
                ex.getMessage(), null, req));
    }

    // 403 - Không đủ quyền
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("Forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(base(HttpStatus.FORBIDDEN, "forbidden",
                "Bạn không có quyền thực hiện hành động này", null, req));
    }

    // 405 - Sai method
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest req) {
        log.info("Method not allowed: {}", ex.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(base(HttpStatus.METHOD_NOT_ALLOWED, "method_not_allowed",
                        "Phương thức không được hỗ trợ", null, req));
    }

    // 404 - Không tìm thấy (service ném RuntimeException "not found")
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex, HttpServletRequest req) {
        // Bạn có thể phân loại message để trả 404 hay 400; ở đây demo 404 cho not found
        if (ex.getMessage()!=null && ex.getMessage().toLowerCase().contains("not found")) {
            log.info("Not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(base(HttpStatus.NOT_FOUND, "not_found",
                    ex.getMessage(), null, req));
        }
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(base(HttpStatus.INTERNAL_SERVER_ERROR,
                "server_error", "Lỗi hệ thống", null, req));
    }

    // Fallback - mọi thứ khác
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(base(HttpStatus.INTERNAL_SERVER_ERROR,
                "server_error", "Lỗi hệ thống", null, req));
    }
}
