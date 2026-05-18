package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid 검증 실패 → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // RuntimeException 전체 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(
            RuntimeException ex
    ) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        String message = ex.getMessage();
        if (message == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        // 403 Forbidden
        if (message.contains("기업 회원만") ||
            message.contains("구직자만") ||
            message.contains("본인 공고만") ||
            message.contains("본인 지원만") ||
            message.contains("본인 공고의") ||
            message.contains("본인 계약서만") ||
            message.contains("권한 없음")
        ) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // 404 Not Found
        if (message.contains("없음")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        // 409 Conflict
        if (message.contains("이미 지원한") ||
            message.contains("이미 완료된") ||
            message.contains("이미 노쇼") ||
            message.contains("이미 서명") ||
            message.contains("이미 사용 중인 이메일")
        ) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        // 400 Bad Request
        if (message.contains("노쇼 누적") ||
            message.contains("완료된 지원은 취소") ||
            message.contains("지원 상태인 경우에만") ||
            message.contains("취소된 계약서") ||
            message.contains("서명 완료된 계약서는 취소") ||
            message.contains("파일이 없습니다") ||
            message.contains("파일만 업로드 가능")
        ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // 500 Internal Server Error
        if (message.contains("파일 업로드 실패")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}