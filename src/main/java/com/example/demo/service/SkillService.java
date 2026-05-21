package com.example.demo.service;

import com.example.demo.dto.SkillRequestDto;
import com.example.demo.dto.SkillResponseDto;
import com.example.demo.entity.*;
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

    // 스킬 추가
    @Transactional
    public SkillResponseDto addSkill(SkillRequestDto requestDto, User loginUser) {

        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new RuntimeException("개인 회원만 스킬을 등록할 수 있습니다.");
        }

        if (requestDto.getName() == null || requestDto.getName().isBlank()) {
            throw new RuntimeException("스킬명을 입력해주세요.");
        }

        if (skillRepository.existsByUserAndName(loginUser, requestDto.getName())) {
            throw new RuntimeException("이미 등록된 스킬입니다.");
        }

        JobCategory category = null;
        if (requestDto.getCategoryId() != null) {
            category = jobCategoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("카테고리 없음"));
        }

        Skill skill = new Skill();
        skill.setUser(loginUser);
        skill.setName(requestDto.getName());
        skill.setCategory(category);

        return new SkillResponseDto(skillRepository.save(skill));
    }

    // 내 스킬 목록 조회
    @Transactional(readOnly = true)
    public List<SkillResponseDto> getMySkills(User loginUser) {
        return skillRepository.findByUser(loginUser)
                .stream()
                .map(SkillResponseDto::new)
                .collect(Collectors.toList());
    }

    // 카테고리별 스킬 조회
    @Transactional(readOnly = true)
    public List<SkillResponseDto> getMySkillsByCategory(
            User loginUser, Long categoryId) {

        JobCategory category = jobCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("카테고리 없음"));

        return skillRepository.findByUserAndCategory(loginUser, category)
                .stream()
                .map(SkillResponseDto::new)
                .collect(Collectors.toList());
    }

    // 특정 유저 스킬 조회 (기업용)
    @Transactional(readOnly = true)
    public List<SkillResponseDto> getUserSkills(Long userId, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        return skillRepository.findByUser(targetUser)
                .stream()
                .map(SkillResponseDto::new)
                .collect(Collectors.toList());
    }

    // 스킬 삭제
    @Transactional
    public void deleteSkill(Long skillId, User loginUser) {
        Skill skill = skillRepository.findByIdAndUser(skillId, loginUser)
                .orElseThrow(() -> new RuntimeException("스킬 없음 또는 권한 없음"));

        skillRepository.delete(skill);
    }
}