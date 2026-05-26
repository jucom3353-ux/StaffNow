package com.example.demo.dto;

import com.example.demo.entity.PaymentType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDto {
    private PaymentType type;
    private String itemName;
    private int amount;
    private String payMethod;
    private String pgTransactionId;
    private Long referenceId;
}