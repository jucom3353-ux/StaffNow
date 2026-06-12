package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.EventRequestDto;
import com.example.demo.entity.EventStatus;
import com.example.demo.service.EventService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "이벤트 API", description = "플랫폼 이벤트 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    @Operation(
        summary = "이벤트 목록 조회",
        description = "비회원도 조회 가능합니다. status로 필터링 가능합니다. status: ONGOING(진행중), ENDED(종료), UPCOMING(예정)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(
            @Parameter(description = "이벤트 상태 (ONGOING/ENDED/UPCOMING)")
            @RequestParam(required = false) EventStatus status) {
        if (status != null) {
            return ResponseEntity.ok(ApiResponse.ok(eventService.getByStatus(status)));
        }
        return ResponseEntity.ok(ApiResponse.ok(eventService.getAll()));
    }

    @Operation(
        summary = "이벤트 단건 조회",
        description = "비회원도 조회 가능합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getEvent(
            @Parameter(description = "이벤트 ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEvent(id)));
    }

    @Operation(
        summary = "이벤트 등록 (관리자)",
        description = "관리자 전용. 이벤트를 등록합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createEvent(
            @RequestBody EventRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                eventService.createEvent(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "이벤트 수정 (관리자)",
        description = "관리자 전용. 이벤트 내용을 수정합니다."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateEvent(
            @Parameter(description = "이벤트 ID", example = "1")
            @PathVariable Long id,
            @RequestBody EventRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                eventService.updateEvent(id, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "당첨자 발표 (관리자)",
        description = "관리자 전용. 이벤트 당첨자를 발표합니다. body: {\"winnerContent\": \"당첨자 내용\"}"
    )
    @PatchMapping("/{id}/winner")
    public ResponseEntity<ApiResponse<?>> announceWinner(
            @Parameter(description = "이벤트 ID", example = "1")
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok(
                eventService.announceWinner(id, body.get("winnerContent"),  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "이벤트 삭제 (관리자)",
        description = "관리자 전용. 이벤트를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteEvent(
            @Parameter(description = "이벤트 ID", example = "1")
            @PathVariable Long id) {
        eventService.deleteEvent(id,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("이벤트 삭제 완료"));
    }

     
}