package com.example.demo.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import com.example.demo.dto.SearchSuggestionsResponse;
import com.example.demo.service.SearchKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "검색어 관리", description = "인기/최근 검색어 저장 및 조회 API")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchKeywordController {

    private final SearchKeywordService searchKeywordService;

    @Operation(summary = "검색어 저장", description = "유저가 검색한 키워드를 저장합니다.")
    @PostMapping("/keywords")
    public ResponseEntity<Void> saveKeyword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SearchKeywordRequest request) {
        searchKeywordService.save(userDetails.getUsername(), request.getKeyword());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "검색어 조회", description = "최근 검색어 5개 + 최근 7일 인기 검색어 10개를 반환합니다.")
    @GetMapping("/suggestions")
    public ResponseEntity<SearchSuggestionsResponse> getSuggestions(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                searchKeywordService.getSuggestions(userDetails.getUsername())
        );
    }

    @Getter
    @NoArgsConstructor
    static class SearchKeywordRequest {
        private String keyword;
    }
}