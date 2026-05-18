package com.example.demo.controller;

import com.example.demo.dto.ContractCreateRequestDto;
import com.example.demo.entity.User;
import com.example.demo.service.ContractPdfService;
import com.example.demo.service.ContractService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "계약서 API", description = "근로계약서 생성/조회/서명 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/contracts")
public class ContractController {

    private final ContractService contractService;
    private final ContractPdfService contractPdfService;

    @Operation(summary = "계약서 생성 (기업만)")
    @PostMapping
    public ResponseEntity<?> createContract(
            @RequestBody ContractCreateRequestDto requestDto) {
        try {
            contractService.createContract(requestDto, getLoginUser());
            return ResponseEntity.ok("계약서 생성 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "내 계약서 목록 조회")
    @GetMapping
    public ResponseEntity<?> getMyContracts() {
        try {
            return ResponseEntity.ok(contractService.getMyContracts(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Operation(summary = "계약서 단건 조회")
    @GetMapping("/{contractId}")
    public ResponseEntity<?> getContract(@PathVariable Long contractId) {
        try {
            return ResponseEntity.ok(
                    contractService.getContract(contractId, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "계약서 서명 (기업/구직자 각각)")
    @PatchMapping("/{contractId}/sign")
    public ResponseEntity<?> signContract(@PathVariable Long contractId) {
        try {
            contractService.signContract(contractId, getLoginUser());
            return ResponseEntity.ok("서명 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "계약서 취소 (기업만)")
    @PatchMapping("/{contractId}/cancel")
    public ResponseEntity<?> cancelContract(@PathVariable Long contractId) {
        try {
            contractService.cancelContract(contractId, getLoginUser());
            return ResponseEntity.ok("계약서 취소 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "계약서 PDF 생성")
    @GetMapping("/{contractId}/pdf")
    public ResponseEntity<?> generatePdf(@PathVariable Long contractId) {
        try {
            String url = contractPdfService.generateContractPdf(
                    contractId, getLoginUser().getId());
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}