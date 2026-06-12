package com.example.demo.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.example.demo.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException → ErrorCode 기반 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(
            CustomException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(new ErrorResponse(
                        ex.getStatus(),
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    // @Valid 검증 실패 → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex, HttpServletRequest request) {

    Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage,
                    (existing, replacement) -> existing
            ));

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("message", "입력값 검증 실패");
        response.put("errors", fieldErrors);
        response.put("path", request.getRequestURI());
        response.put("timestamp", java.time.LocalDateTime.now().toString());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                 .body(new ErrorResponse(400, "입력값 검증 실패",
                    request.getRequestURI(), fieldErrors));
    }

    // 필수 파라미터 누락 → 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400,
                        "필수 파라미터 누락: " + ex.getParameterName(),
                        request.getRequestURI()));
    }

    // 파라미터 타입 불일치 → 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400,
                        "잘못된 파라미터 타입: " + ex.getName(),
                        request.getRequestURI()));
    }

    // 그 외 RuntimeException → 500
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500,
                        ex.getMessage() != null ? ex.getMessage() : "서버 내부 오류",
                        request.getRequestURI()));
    }

    // 그 외 모든 예외 → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500,
                        "서버 내부 오류가 발생했습니다.",
                        request.getRequestURI()));
    }
   
    // 파일 크기 초과 → 400
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
                MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400,
                        "파일 크기가 너무 큽니다.",
                        request.getRequestURI()));
}
}