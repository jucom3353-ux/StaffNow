package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.SalaryCalculatorRequestDto;
import com.example.demo.service.SalaryCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "급여 계산기 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/salary")
public class SalaryCalculatorController {

    private final SalaryCalculatorService salaryCalculatorService;

    @Operation(summary = "급여 계산")
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<?>> calculate(
            @RequestBody SalaryCalculatorRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                salaryCalculatorService.calculate(requestDto)));
    }

    @Operation(summary = "최저시급 조회")
    @GetMapping("/minimum-wage")
    public ResponseEntity<ApiResponse<?>> getMinimumWage() {
        return ResponseEntity.ok(ApiResponse.ok(
                salaryCalculatorService.getMinimumWage()));
    }
}