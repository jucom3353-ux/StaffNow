package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ContractCreateRequestDto;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.ContractPdfService;
import com.example.demo.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
  
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "계약서 API", description = "근로계약서 생성/조회/서명 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/contracts")
public class ContractController {

    private final ContractService contractService;
    private final ContractPdfService contractPdfService;

    @Operation(
        summary = "계약서 생성",
        description = "기업 전용. 근로계약서를 수동으로 생성합니다. 지원 승인 시 자동 생성되므로 일반적으로 직접 호출할 필요는 없습니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createContract(
            @RequestBody ContractCreateRequestDto requestDto) {
        contractService.createContract(requestDto,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("계약서 생성 완료"));
    }

    @Operation(
        summary = "내 계약서 목록 조회",
        description = "현재 로그인한 사용자의 계약서 목록을 반환합니다. 기업은 발급한 계약서, 구직자는 수신한 계약서가 조회됩니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getMyContracts() {
        return ResponseEntity.ok(ApiResponse.ok(
                contractService.getMyContracts( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "계약서 단건 조회",
        description = "계약서 상세 정보를 조회합니다. 본인(기업 또는 구직자)만 조회 가능합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 계약서 아님"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "계약서 없음")
    })
    @GetMapping("/{contractId}")
    public ResponseEntity<ApiResponse<?>> getContract(
            @Parameter(description = "계약서 ID", example = "1")
            @PathVariable Long contractId) {
        return ResponseEntity.ok(ApiResponse.ok(
                contractService.getContract(contractId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "계약서 서명",
        description = "기업 또는 구직자가 계약서에 서명합니다. signatureUrl은 서명 이미지 URL입니다 (선택). 양쪽 모두 서명 완료 시 계약이 체결됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "서명 완료"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 서명된 계약서 또는 만료된 계약서")
    })
    @PatchMapping("/{contractId}/sign")
    public ResponseEntity<ApiResponse<?>> signContract(
            @Parameter(description = "계약서 ID", example = "1")
            @PathVariable Long contractId,
            @Parameter(description = "서명 이미지 URL (선택)", example = "https://storage.example.com/signature.png")
            @RequestParam(required = false) String signatureUrl) {
        contractService.signContract(contractId, signatureUrl,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("서명 완료"));
    }

    @Operation(
        summary = "계약서 취소",
        description = "기업 전용. PENDING 상태의 계약서만 취소 가능합니다."
    )
    @PatchMapping("/{contractId}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelContract(
            @Parameter(description = "계약서 ID", example = "1")
            @PathVariable Long contractId) {
        contractService.cancelContract(contractId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("계약서 취소 완료"));
    }

    @Operation(
        summary = "계약서 PDF 생성",
        description = "계약서 PDF를 생성하고 다운로드 URL을 반환합니다. SIGNED 상태의 계약서만 PDF 생성 가능합니다. PDF는 1년간 다운로드 가능합니다."
    )
    @GetMapping("/{contractId}/pdf")
    public ResponseEntity<ApiResponse<?>> generatePdf(
            @Parameter(description = "계약서 ID", example = "1")
            @PathVariable Long contractId) {
        String url = contractPdfService.generateContractPdf(
                contractId,  AuthorizationUtil.getLoginUser().getId());
        return ResponseEntity.ok(ApiResponse.ok("PDF 생성 완료",
                Map.of("url", url)));
    }

     
}