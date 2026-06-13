package com.cuutruyen.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of(
            "error", e.getStatusCode().toString(),
            "message", e.getReason() != null ? e.getReason() : "Error"
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        logger.warn("Runtime exception: {}", e.getMessage());
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Bad Request",
            "message", e.getMessage() != null ? e.getMessage() : "Yêu cầu không hợp lệ"
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        logger.error("Unhandled exception", e);
        return ResponseEntity.status(500).body(Map.of(
            "error", "Internal Server Error",
            "message", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."
        ));
    }
}
