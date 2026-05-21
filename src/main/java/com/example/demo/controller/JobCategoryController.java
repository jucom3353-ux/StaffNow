package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.JobCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "카테고리 API", description = "공고 카테고리 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class JobCategoryController {

    private final JobCategoryService jobCategoryService;

    @Operation(summary = "대분류 전체 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getParentCategories() {
        return ResponseEntity.ok(ApiResponse.ok(
                jobCategoryService.getParentCategories()));
    }

    @Operation(summary = "중분류 조회 (대분류 id 기준)")
    @GetMapping("/{parentId}/children")
    public ResponseEntity<ApiResponse<?>> getChildCategories(
            @PathVariable Long parentId) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobCategoryService.getChildCategories(parentId)));
    }
}