package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.EventRequestDto;
import com.example.demo.entity.EventStatus;
import com.example.demo.entity.User;
import com.example.demo.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "이벤트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    @Operation(summary = "이벤트 전체 목록 조회 (비회원 가능)")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(required = false) EventStatus status) {
        if (status != null) {
            return ResponseEntity.ok(ApiResponse.ok(eventService.getByStatus(status)));
        }
        return ResponseEntity.ok(ApiResponse.ok(eventService.getAll()));
    }

    @Operation(summary = "이벤트 단건 조회 (비회원 가능)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEvent(id)));
    }

    @Operation(summary = "이벤트 등록 (ADMIN)")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createEvent(
            @RequestBody EventRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                eventService.createEvent(requestDto, getLoginUser())));
    }

    @Operation(summary = "이벤트 수정 (ADMIN)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateEvent(
            @PathVariable Long id,
            @RequestBody EventRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                eventService.updateEvent(id, requestDto, getLoginUser())));
    }

    @Operation(summary = "당첨자 발표 (ADMIN)")
    @PatchMapping("/{id}/winner")
    public ResponseEntity<ApiResponse<?>> announceWinner(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok(
                eventService.announceWinner(id, body.get("winnerContent"), getLoginUser())));
    }

    @Operation(summary = "이벤트 삭제 (ADMIN)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("이벤트 삭제 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}