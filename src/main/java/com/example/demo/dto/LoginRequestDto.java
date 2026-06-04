package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "로그인 요청")
public class LoginRequestDto {

    @Schema(description = "이메일", example = "user@example.com")
    @NotBlank(message = "이메일을 입력하세요")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @Schema(description = "비밀번호 (8자 이상)", example = "password123!")
    @NotBlank(message = "비밀번호를 입력하세요")
    private String password;
}