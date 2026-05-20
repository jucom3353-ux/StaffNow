package com.example.demo.dto;

import com.example.demo.entity.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class JobPostCreateRequestDto {

    @NotBlank(message = "제목을 입력하세요")
    private String title;

    private String content;

    @NotBlank(message = "근무 지역을 입력하세요")
    private String workLocation;

    private String startTime;
    private String endTime;
    private String breakTime;

    @NotNull(message = "급여 유형을 선택하세요")
    private WageType wageType;

    @NotNull(message = "급여 금액을 입력하세요")
    @Min(value = 0, message = "급여는 0원 이상이어야 합니다")
    private Integer wageAmount;

    private Boolean includeHolidayPay;
    private String workType;
    private String description;
    private Gender requiredGender;

    @Min(value = 18, message = "최소 나이는 18세 이상이어야 합니다")
    private Integer requiredAgeMin;

    @Max(value = 70, message = "최대 나이는 70세 이하여야 합니다")
    private Integer requiredAgeMax;

    private String requiredPersonality;
    private String requiredCondition;
    private String preferredExperience;
    private String preferredLanguage;
    private String preferredEtc;

    @NotNull(message = "모집 인원을 입력하세요")
    @Min(value = 1, message = "모집 인원은 1명 이상이어야 합니다")
    private Integer recruitCount;

    private PostStatus postStatus;

    // 변경: JobCategory → Long
    private Long categoryId;

    private String deadline;
    private LocalDate workStartDate;
    private LocalDate workEndDate;
    private Boolean mealProvided = false;
    private String uniformInfo;
    private String managerName;
    private String managerPhone;
    private String managerEmail;
    private String managerFax;
}