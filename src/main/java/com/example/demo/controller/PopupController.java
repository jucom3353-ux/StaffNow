package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PopupRequestDto;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.PopupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
  
import org.springframework.web.bind.annotation.*;

@Tag(name = "팝업 API", description = "메인 화면 팝업 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/popups")
public class PopupController {

    private final PopupService popupService;

    @Operation(
        summary = "활성 팝업 조회",
        description = "현재 활성화된 팝업 목록을 반환합니다. 앱/웹 메인 화면 진입 시 호출합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getActivePopups() {
        return ResponseEntity.ok(ApiResponse.ok(popupService.getActivePopups()));
    }

    @Operation(
        summary = "전체 팝업 조회 (관리자)",
        description = "관리자 전용. 비활성 팝업 포함 전체 팝업 목록을 반환합니다."
    )
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAllPopups() {
        return ResponseEntity.ok(ApiResponse.ok(
                popupService.getAllPopups( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "팝업 등록 (관리자)",
        description = "관리자 전용. 새 팝업을 등록합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createPopup(
            @RequestBody PopupRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                popupService.createPopup(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "팝업 수정 (관리자)",
        description = "관리자 전용. 팝업 내용을 수정합니다."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updatePopup(
            @Parameter(description = "팝업 ID", example = "1")
            @PathVariable Long id,
            @RequestBody PopupRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                popupService.updatePopup(id, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "팝업 삭제 (관리자)",
        description = "관리자 전용. 팝업을 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deletePopup(
            @Parameter(description = "팝업 ID", example = "1")
            @PathVariable Long id) {
        popupService.deletePopup(id,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("팝업 삭제 완료"));
    }

     
}