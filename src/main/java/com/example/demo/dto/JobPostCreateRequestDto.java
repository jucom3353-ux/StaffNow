package com.example.demo.dto;

import com.example.demo.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "공고 생성/수정 요청")
public class JobPostCreateRequestDto {

    @Schema(description = "공고 제목", example = "롤스로이스 시승행사 프로모터 모집")
    @NotBlank(message = "제목을 입력하세요")
    private String title;

    @Schema(description = "공고 내용", example = "행사 안내 및 고객 응대 업무")
    private String content;

    @Schema(description = "근무 지역 (주소 입력 시 자동으로 좌표 변환됨)", example = "서울시 강남구 테헤란로 123")
    @NotBlank(message = "근무 지역을 입력하세요")
    private String workLocation;

    @Schema(description = "위도 (자동 변환, 직접 입력 불필요)", example = "37.5665")
    private Double latitude;

    @Schema(description = "경도 (자동 변환, 직접 입력 불필요)", example = "126.9780")
    private Double longitude;

    @Schema(description = "근무 시작 시간", example = "09:00")
    private String startTime;

    @Schema(description = "근무 종료 시간", example = "18:00")
    private String endTime;

    @Schema(description = "휴게 시간", example = "60분")
    private String breakTime;

    @Schema(description = "급여 유형 (HOURLY: 시급, DAILY: 일급, MONTHLY: 월급)", example = "DAILY")
    @NotNull(message = "급여 유형을 선택하세요")
    private WageType wageType;

    @Schema(description = "급여 금액 (원)", example = "170000")
    @NotNull(message = "급여 금액을 입력하세요")
    @Min(value = 0, message = "급여는 0원 이상이어야 합니다")
    private Integer wageAmount;

    @Schema(description = "주휴수당 포함 여부", example = "false")
    private Boolean includeHolidayPay;

    @Schema(description = "근무 형태", example = "단기")
    private String workType;

    @Schema(description = "상세 업무 설명")
    private String description;

    @Schema(description = "모집 성별 (MALE/FEMALE/null: 무관)", example = "FEMALE")
    private Gender requiredGender;

    @Schema(description = "최소 나이 (18 이상)", example = "20")
    @Min(value = 18, message = "최소 나이는 18세 이상이어야 합니다")
    private Integer requiredAgeMin;

    @Schema(description = "최대 나이 (70 이하)", example = "35")
    @Max(value = 70, message = "최대 나이는 70세 이하여야 합니다")
    private Integer requiredAgeMax;

    @Schema(description = "요구 성격/태도", example = "친절하고 적극적인 분")
    private String requiredPersonality;

    @Schema(description = "지원 조건", example = "이전 행사 경험자 우대")
    private String requiredCondition;

    @Schema(description = "우대 경력", example = "프로모터 1년 이상")
    private String preferredExperience;

    @Schema(description = "우대 언어", example = "영어 가능자 우대")
    private String preferredLanguage;

    @Schema(description = "기타 우대사항")
    private String preferredEtc;

    @Schema(description = "모집 인원 (1명 이상)", example = "3")
    @NotNull(message = "모집 인원을 입력하세요")
    @Min(value = 1, message = "모집 인원은 1명 이상이어야 합니다")
    private Integer recruitCount;

    @Schema(description = "공고 상태 (DRAFT: 임시저장, OPEN: 공개, CLOSED: 마감)", example = "DRAFT")
    private PostStatus postStatus;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "모집 마감일 (yyyy-MM-dd, 오늘 이후)", example = "2026-06-30")
    private String deadline;

    @Schema(description = "근무 시작일 (yyyy-MM-dd)", example = "2026-06-01")
    private LocalDate workStartDate;

    @Schema(description = "근무 종료일 (yyyy-MM-dd)", example = "2026-06-30")
    private LocalDate workEndDate;

    @Schema(description = "식사 제공 여부", example = "false")
    private Boolean mealProvided = false;

    @Schema(description = "유니폼 정보", example = "검정 정장 (개인 지참)")
    private String uniformInfo;

    @Schema(description = "담당자 이름", example = "김담당")
    private String managerName;

    @Schema(description = "담당자 전화번호", example = "01098765432")
    private String managerPhone;

    @Schema(description = "담당자 이메일", example = "manager@company.com")
    private String managerEmail;

    @Schema(description = "담당자 팩스")
    private String managerFax;

    @Schema(description = "공고 대표 이미지 URL", example = "https://storage.example.com/jobpost.jpg")
    private String imageUrl;

    @Schema(description = "상단 노출 여부 (유료)", example = "false")
    private Boolean topExposure = false;

    @Schema(description = "급구 배지 표시 여부", example = "false")
    private Boolean urgentBadge = false;

    @Schema(description = "온라인 지원 허용 여부", example = "true")
    private Boolean allowOnline = true;

    @Schema(description = "전화 지원 허용 여부", example = "false")
    private Boolean allowPhone = false;

    @Schema(description = "문자 지원 허용 여부", example = "false")
    private Boolean allowSms = false;
}