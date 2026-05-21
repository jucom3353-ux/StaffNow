package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class MessageRequestDto {

    @NotNull(message = "수신자를 선택하세요")
    private Long receiverId;

    @NotBlank(message = "메시지 내용을 입력하세요")
    @Size(max = 1000, message = "메시지는 1000자 이하로 작성해주세요")
    private String content;
}