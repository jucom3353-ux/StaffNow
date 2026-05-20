package com.example.demo.controller;

import com.example.demo.entity.JobCategory;
import com.example.demo.service.JobCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "카테고리 API", description = "공고 카테고리 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class JobCategoryController {

    private final JobCategoryService jobCategoryService;

    @Operation(summary = "대분류 전체 조회")
    @GetMapping
    public ResponseEntity<?> getParentCategories() {
        try {
            return ResponseEntity.ok(jobCategoryService.getParentCategories());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "중분류 조회 (대분류 id 기준)")
    @GetMapping("/{parentId}/children")
    public ResponseEntity<?> getChildCategories(@PathVariable Long parentId) {
        try {
            return ResponseEntity.ok(jobCategoryService.getChildCategories(parentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}