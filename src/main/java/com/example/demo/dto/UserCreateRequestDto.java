package com.example.demo.dto;

import com.example.demo.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "회원가입 요청")
public class UserCreateRequestDto {

    @Schema(description = "이메일", example = "user@example.com")
    @NotBlank(message = "이메일을 입력하세요")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @Schema(description = "비밀번호 (8자 이상)", example = "password123!")
    @NotBlank(message = "비밀번호를 입력하세요")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String password;

    @Schema(description = "이름", example = "홍길동")
    @NotBlank(message = "이름을 입력하세요")
    private String name;

    @Schema(description = "전화번호 (01로 시작하는 10~11자리)", example = "01012345678")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 전화번호 형식이 아닙니다")
    private String phone;

    @Schema(description = "회사명 (COMPANY 가입 시 필수)", example = "롯데마트")
    private String companyName;

    @Schema(description = "회원 유형 (INDIVIDUAL: 구직자, COMPANY: 기업)", example = "INDIVIDUAL")
    @NotNull(message = "회원 유형을 선택하세요")
    private Role role;

    @Schema(description = "MBTI (선택)", example = "ENFP")
    private String mbti;

    @Schema(description = "추천인 코드 (선택)", example = "ABC123")
    private String referralCode;

    @Schema(description = "담당자 초대 코드 (MANAGER 가입 시 필수)", example = "MGR456")
    private String inviteCode;
}