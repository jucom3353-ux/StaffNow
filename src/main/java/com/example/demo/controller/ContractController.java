package com.example.demo.controller;

import com.example.demo.dto.ContractCreateRequestDto;
import com.example.demo.dto.ContractResponseDto;
import com.example.demo.entity.User;
import com.example.demo.service.ContractService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "계약서 API", description = "근로계약서 생성/조회/서명 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/contracts")
public class ContractController {

    private final ContractService contractService;

    // 계약서 생성
    @Operation(summary = "계약서 생성 (기업만)")
    @PostMapping
    public String createContract(@RequestBody ContractCreateRequestDto requestDto) {
        contractService.createContract(requestDto, getLoginUser());
        return "계약서 생성 완료";
    }

    // 내 계약서 목록 조회
    @Operation(summary = "내 계약서 목록 조회")
    @GetMapping
    public List<ContractResponseDto> getMyContracts() {
        return contractService.getMyContracts(getLoginUser());
    }

    // 계약서 단건 조회
    @Operation(summary = "계약서 단건 조회")
    @GetMapping("/{contractId}")
    public ContractResponseDto getContract(@PathVariable Long contractId) {
        return contractService.getContract(contractId, getLoginUser());
    }

    // 서명
    @Operation(summary = "계약서 서명 (기업/구직자 각각)")
    @PatchMapping("/{contractId}/sign")
    public String signContract(@PathVariable Long contractId) {
        contractService.signContract(contractId, getLoginUser());
        return "서명 완료";
    }

    // 계약서 취소
    @Operation(summary = "계약서 취소 (기업만)")
    @PatchMapping("/{contractId}/cancel")
    public String cancelContract(@PathVariable Long contractId) {
        contractService.cancelContract(contractId, getLoginUser());
        return "계약서 취소 완료";
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}