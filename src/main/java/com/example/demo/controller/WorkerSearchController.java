package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Gender;
import com.example.demo.entity.User;
import com.example.demo.service.WorkerSearchService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "추천 인력 API", description = "근로자 검색 및 추천 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/workers")
public class WorkerSearchController {

    private final WorkerSearchService workerSearchService;

    @Operation(
        summary = "추천 인력 조회",
        description = "기업/매니저 전용. 조건에 맞는 근로자를 검색합니다. sort: temperature(별점순), noShow(노쇼 적은 순). 프로필 부스트 활성화 + 사진 많은 순으로 상단 노출됩니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<?>> searchWorkers(
            @Parameter(description = "이름 검색", example = "홍길동")
            @RequestParam(required = false) String name,
            @Parameter(description = "최소 별점 (0.0~5.0)", example = "3.0")
            @RequestParam(defaultValue = "0") double minRating,
            @Parameter(description = "최대 노쇼 횟수", example = "2")
            @RequestParam(defaultValue = "100") int maxNoShow,
            @Parameter(description = "활동 지역", example = "서울")
            @RequestParam(required = false) String activityRegion,
            @Parameter(description = "MBTI", example = "ENFP")
            @RequestParam(required = false) String mbti,
            @Parameter(description = "상시 근무 가능 여부")
            @RequestParam(required = false) Boolean availableAlways,
            @Parameter(description = "성별 (MALE/FEMALE)")
            @RequestParam(required = false) Gender gender,
            @Parameter(description = "최소 나이", example = "20")
            @RequestParam(required = false) Integer minAge,
            @Parameter(description = "최대 나이", example = "35")
            @RequestParam(required = false) Integer maxAge,
            @Parameter(description = "선호 근무 시간대")
            @RequestParam(required = false) String timeType,
            @Parameter(description = "포트폴리오 보유 여부")
            @RequestParam(required = false) Boolean hasPortfolio,
            @Parameter(description = "자격증 보유 여부")
            @RequestParam(required = false) Boolean hasCertificate,
            @Parameter(description = "정렬 기준 (temperature/noShow)", example = "temperature")
            @RequestParam(defaultValue = "temperature") String sort,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        User loginUser =  AuthorizationUtil.getLoginUser();
        return ResponseEntity.ok(ApiResponse.ok(
                workerSearchService.searchWorkers(
                        name, minRating, maxNoShow,
                        activityRegion, mbti, availableAlways,
                        gender, minAge, maxAge, timeType,
                        hasPortfolio, hasCertificate,
                        sort, page, size,
                        loginUser.getId()
                )));
    }

    @Operation(
        summary = "근로자 상세 조회",
        description = "특정 근로자의 공개 프로필 정보를 반환합니다."
    )
    @GetMapping("/{workerId}")
    public ResponseEntity<ApiResponse<?>> getWorker(
            @Parameter(description = "근로자 ID", example = "1")
            @PathVariable Long workerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                workerSearchService.getWorker(workerId)));
    }

     
}