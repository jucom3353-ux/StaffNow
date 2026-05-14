package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.AttendanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "출퇴근 API", description = "출퇴근 사진 업로드 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    // 출퇴근 사진 업로드
    @Operation(summary = "출퇴근 사진 업로드")
    @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadPhoto(
            @RequestParam("file") MultipartFile file
    ) {
        User loginUser = getLoginUser();
        String url = attendanceService.uploadPhoto(file, loginUser);
        return Map.of("url", url);
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}