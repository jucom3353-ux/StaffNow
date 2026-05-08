package com.example.demo.controller;

import com.example.demo.dto.WorkSessionCreateRequestDto;
import com.example.demo.dto.WorkSessionResponseDto;
import com.example.demo.service.WorkSessionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WorkSessionController {

    private final WorkSessionService workSessionService;

    public WorkSessionController(
            WorkSessionService workSessionService
    ) {
        this.workSessionService = workSessionService;
    }

    // 근무회차 생성
    @PostMapping("/worksessions")
    public String createWorkSession(
            @RequestBody WorkSessionCreateRequestDto requestDto
    ) {

        workSessionService.createWorkSession(requestDto);

        return "근무회차 생성 완료";
    }

    // 근무회차 전체조회
    @GetMapping("/worksessions")
    public List<WorkSessionResponseDto> getWorkSessions() {

        return workSessionService.getWorkSessions();
    }

    // 근무회차 단건조회
    @GetMapping("/worksessions/{id}")
    public WorkSessionResponseDto getWorkSession(
            @PathVariable Long id
    ) {

        return workSessionService.getWorkSession(id);
    }

    // 근무회차 수정
    @PutMapping("/worksessions/{id}")
    public String updateWorkSession(
            @PathVariable Long id,
            @RequestBody WorkSessionCreateRequestDto requestDto
    ) {

        workSessionService.updateWorkSession(id, requestDto);

        return "근무회차 수정 완료";
    }

    // 근무회차 삭제
    @DeleteMapping("/worksessions/{id}")
    public String deleteWorkSession(
            @PathVariable Long id
    ) {

        workSessionService.deleteWorkSession(id);

        return "근무회차 삭제 완료";
    }
}