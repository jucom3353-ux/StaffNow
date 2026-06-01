package com.example.demo.dto;

import com.example.demo.entity.User;
import lombok.Getter;

@Getter
public class UserPrivateResponseDto extends UserResponseDto {

    private String bankName;
    private String accountNumber;
    private String accountHolder;

    public UserPrivateResponseDto(User user) {
        super(user);
        this.bankName = user.getBankName();
        this.accountNumber = user.getAccountNumber();
        this.accountHolder = user.getAccountHolder();
    }
}