package com.example.demo.service;

import com.example.demo.entity.JobCategory;
import com.example.demo.entity.PreferredCategory;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.JobCategoryRepository;
import com.example.demo.repository.PreferredCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreferredCategoryService {

    private final PreferredCategoryRepository preferredCategoryRepository;
    private final JobCategoryRepository jobCategoryRepository;

    private static final int MAX_PREFERRED_CATEGORIES = 5;

    @Transactional
    public void addPreferredCategory(Long categoryId, User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        JobCategory category = jobCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        if (preferredCategoryRepository.existsByUserAndCategory(loginUser, category)) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS);
        }

        List<PreferredCategory> existing = preferredCategoryRepository.findByUser(loginUser);
        if (existing.size() >= MAX_PREFERRED_CATEGORIES) {
            throw new CustomException(ErrorCode.PREFERRED_CATEGORY_LIMIT_EXCEEDED);
        }

        PreferredCategory preferredCategory = new PreferredCategory();
        preferredCategory.setUser(loginUser);
        preferredCategory.setCategory(category);
        preferredCategoryRepository.save(preferredCategory);
    }

    @Transactional
    public void removePreferredCategory(Long categoryId, User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        JobCategory category = jobCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        if (!preferredCategoryRepository.existsByUserAndCategory(loginUser, category)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        preferredCategoryRepository.deleteByUserAndCategory(loginUser, category);
    }

    @Transactional(readOnly = true)
    public List<Long> getMyPreferredCategoryIds(User loginUser) {
        return preferredCategoryRepository.findByUser(loginUser)
                .stream()
                .map(pc -> pc.getCategory().getId())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getMyPreferredCategoryNames(User loginUser) {
        return preferredCategoryRepository.findByUser(loginUser)
                .stream()
                .map(pc -> pc.getCategory().getName())
                .collect(Collectors.toList());
    }
}