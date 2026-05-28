package com.example.demo.dto;

import com.example.demo.entity.MileageWithdrawal;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MileageWithdrawalResponseDto {

    private Long id;
    private int requestAmount;
    private int taxDeduction;
    private int netAmount;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private String status;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MileageWithdrawalResponseDto(MileageWithdrawal w) {
        this.id = w.getId();
        this.requestAmount = w.getRequestAmount();
        this.taxDeduction = w.getTaxDeduction();
        this.netAmount = w.getNetAmount();
        this.bankName = w.getBankName();
        this.accountNumber = w.getAccountNumber();
        this.accountHolder = w.getAccountHolder();
        this.status = w.getStatus().name();
        this.rejectReason = w.getRejectReason();
        this.createdAt = w.getCreatedAt();
        this.updatedAt = w.getUpdatedAt();
    }
}