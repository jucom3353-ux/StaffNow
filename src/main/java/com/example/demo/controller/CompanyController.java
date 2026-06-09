package com.example.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompanyController {

    // COMPANY만 접근 가능
    @PreAuthorize("hasRole('COMPANY')")
    @GetMapping("/company/test")
    public String companyTest() {

        return "기업 계정 접근 성공";
    }
}