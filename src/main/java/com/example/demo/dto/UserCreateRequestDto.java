package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.demo.entity.Role;

@Getter
@Setter
@NoArgsConstructor
public class UserCreateRequestDto {

    @NotBlank(message = "이메일을 입력하세요")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @NotBlank(message = "비밀번호를 입력하세요")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "이름을 입력하세요")
    private String name;

    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 전화번호 형식이 아닙니다")
    private String phone;

    private String companyName;

    @NotNull(message = "회원 유형을 선택하세요")
    private Role role;

    private String mbti;
}