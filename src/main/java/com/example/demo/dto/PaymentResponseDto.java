package com.example.demo.dto;

import com.example.demo.entity.Payment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentResponseDto {

    private Long id;
    private String type;
    private String itemName;
    private int amount;
    private String payMethod;
    private String status;
    private String pgTransactionId;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;

    public PaymentResponseDto(Payment payment) {
        this.id = payment.getId();
        this.type = payment.getType().name();
        this.itemName = payment.getItemName();
        this.amount = payment.getAmount();
        this.payMethod = payment.getPayMethod();
        this.status = payment.getStatus().name();
        this.pgTransactionId = payment.getPgTransactionId();
        this.createdAt = payment.getCreatedAt();
        this.paidAt = payment.getPaidAt();
        this.cancelledAt = payment.getCancelledAt();
    }
}