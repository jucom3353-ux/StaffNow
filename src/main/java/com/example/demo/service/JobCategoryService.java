package com.example.demo.service;

import com.example.demo.entity.JobCategory;
import com.example.demo.repository.JobCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobCategoryService {

    private final JobCategoryRepository jobCategoryRepository;

    // 대분류 전체 조회
    @Transactional(readOnly = true)
    public List<JobCategory> getParentCategories() {
        return jobCategoryRepository.findByParentIsNull();
    }

    // 중분류 조회 (대분류 id 기준)
    @Transactional(readOnly = true)
    public List<JobCategory> getChildCategories(Long parentId) {
        return jobCategoryRepository.findByParentId(parentId);
    }
}