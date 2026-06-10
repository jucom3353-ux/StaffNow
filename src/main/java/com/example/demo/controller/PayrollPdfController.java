package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.PayrollPdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
  
import org.springframework.web.bind.annotation.*;

@Tag(name = "급여명세서 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payrolls")
public class PayrollPdfController {

    private final PayrollPdfService payrollPdfService;

    @Operation(summary = "급여명세서 PDF 발급")
    @GetMapping("/{payrollId}/pdf")
    public ResponseEntity<ApiResponse<?>> generatePayrollPdf(
            @PathVariable Long payrollId) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollPdfService.generatePayrollPdf(payrollId,  AuthorizationUtil.getLoginUser())));
    }

     
}