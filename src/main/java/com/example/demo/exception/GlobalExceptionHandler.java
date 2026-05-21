package com.example.demo.exception;

import com.example.demo.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid 검증 실패 → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
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

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 필수 파라미터 누락 → 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400,
                        "필수 파라미터 누락: " + ex.getParameterName(),
                        request.getRequestURI()));
    }

    // 파라미터 타입 불일치 → 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400,
                        "잘못된 파라미터 타입: " + ex.getName(),
                        request.getRequestURI()));
    }

    // RuntimeException 전체 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        String message = ex.getMessage();
        String path = request.getRequestURI();

        if (message == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(500, "서버 내부 오류", path));
        }

        // 403 Forbidden
        if (message.contains("기업 회원만") ||
            message.contains("구직자만") ||
            message.contains("관리자만") ||
            message.contains("본인 공고만") ||
            message.contains("본인 지원만") ||
            message.contains("본인 공고의") ||
            message.contains("본인 계약서만") ||
            message.contains("본인 분쟁만") ||
            message.contains("권한 없음") ||
            message.contains("개인 회원만")
        ) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, message, path));
        }

        // 404 Not Found
        if (message.contains("없음") || message.contains("존재하지 않")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, message, path));
        }

        // 409 Conflict
        if (message.contains("이미 지원한") ||
            message.contains("이미 완료된") ||
            message.contains("이미 노쇼") ||
            message.contains("이미 서명") ||
            message.contains("이미 사용 중인 이메일") ||
            message.contains("이미 등록된") ||
            message.contains("이미 출근") ||
            message.contains("이미 퇴근") ||
            message.contains("이미 해당 주차") ||
            message.contains("이미 신고한") ||
            message.contains("이미 분쟁이 신청된")
        ) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(409, message, path));
        }

        // 400 Bad Request
        if (message.contains("노쇼 누적") ||
            message.contains("완료된 지원은 취소") ||
            message.contains("지원 상태인 경우에만") ||
            message.contains("취소된 계약서") ||
            message.contains("서명 완료된 계약서는 취소") ||
            message.contains("파일이 없습니다") ||
            message.contains("파일만 업로드 가능") ||
            message.contains("마감된 공고") ||
            message.contains("임시저장된 공고") ||
            message.contains("모집이 마감") ||
            message.contains("경력자만 지원") ||
            message.contains("대기 상태의") ||
            message.contains("확정된 정산만") ||
            message.contains("반려된 정산만") ||
            message.contains("근로자가 거절한") ||
            message.contains("인원이 꽉 찼습니다") ||
            message.contains("설정되어 있지 않습니다") ||
            message.contains("늦습니다") ||
            message.contains("구독 플랜이 필요")
        ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, message, path));
        }

        // 500 Internal Server Error
        if (message.contains("파일 업로드 실패") ||
            message.contains("PDF 생성 실패")
        ) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(500, message, path));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, message, path));
    }

    // 그 외 모든 예외 → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500,
                        "서버 내부 오류가 발생했습니다.",
                        request.getRequestURI()));
    }
}