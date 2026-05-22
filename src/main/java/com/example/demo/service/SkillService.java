package com.example.demo.service;

import com.example.demo.dto.SkillRequestDto;
import com.example.demo.dto.SkillResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.JobCategoryRepository;
import com.example.demo.repository.SkillRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final JobCategoryRepository jobCategoryRepository;

    @Transactional
    public SkillResponseDto addSkill(SkillRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        if (requestDto.getName() == null || requestDto.getName().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_SKILL_NAME);
        }

        if (skillRepository.existsByUserAndName(loginUser, requestDto.getName())) {
            throw new CustomException(ErrorCode.ALREADY_SKILL);
        }

        JobCategory category = null;
        if (requestDto.getCategoryId() != null) {
            category = jobCategoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        Skill skill = new Skill();
        skill.setUser(loginUser);
        skill.setName(requestDto.getName());
        skill.setCategory(category);

        return new SkillResponseDto(skillRepository.save(skill));
    }

    @Transactional(readOnly = true)
    public List<SkillResponseDto> getMySkills(User loginUser) {
        return skillRepository.findByUser(loginUser).stream()
                .map(SkillResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SkillResponseDto> getMySkillsByCategory(User loginUser, Long categoryId) {
        JobCategory category = jobCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        return skillRepository.findByUserAndCategory(loginUser, category).stream()
                .map(SkillResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SkillResponseDto> getUserSkills(Long userId, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return skillRepository.findByUser(targetUser).stream()
                .map(SkillResponseDto::new).collect(Collectors.toList());
    }

    @Transactional
    public void deleteSkill(Long skillId, User loginUser) {
        Skill skill = skillRepository.findByIdAndUser(skillId, loginUser)
                .orElseThrow(() -> new CustomException(ErrorCode.SKILL_NOT_FOUND));
        skillRepository.delete(skill);
    }
}