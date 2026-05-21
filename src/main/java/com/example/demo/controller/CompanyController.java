package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompanyController {

    @PreAuthorize("hasRole('COMPANY')")
    @GetMapping("/company/test")
    public ApiResponse<String> companyTest() {
        return ApiResponse.ok("기업 계정 접근 성공");
    }
}