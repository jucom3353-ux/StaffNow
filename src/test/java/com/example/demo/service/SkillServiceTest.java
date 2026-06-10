package com.example.demo.service;

import com.example.demo.dto.SkillRequestDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.JobCategoryRepository;
import com.example.demo.repository.SkillRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @InjectMocks
    private SkillService skillService;

    @Mock private SkillRepository skillRepository;
    @Mock private UserRepository userRepository;
    @Mock private JobCategoryRepository jobCategoryRepository;

    // 스킬 추가 - COMPANY이면 예외
    @Test
    void addSkill_companyUser_throwsException() {
        User user = makeUser(1L, Role.COMPANY);
        assertThatThrownBy(() -> skillService.addSkill(makeSkillDto("프로모터", null), user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("구직자만");
    }

    // 스킬 추가 - 이름 없으면 예외
    @Test
    void addSkill_emptyName_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        assertThatThrownBy(() -> skillService.addSkill(makeSkillDto("", null), user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("스킬명을 입력해주세요");
    }

    // 스킬 추가 - 이미 존재
    @Test
    void addSkill_alreadyExists_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        given(skillRepository.existsByUserAndName(user, "프로모터")).willReturn(true);

        assertThatThrownBy(() -> skillService.addSkill(makeSkillDto("프로모터", null), user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 등록된 스킬");
    }

    // 스킬 추가 - 카테고리 없음
    @Test
    void addSkill_categoryNotFound_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        given(skillRepository.existsByUserAndName(user, "프로모터")).willReturn(false);
        given(jobCategoryRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.addSkill(makeSkillDto("프로모터", 999L), user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("카테고리를 찾을 수 없습니다");
    }

    // 스킬 조회 - INDIVIDUAL이면 예외
    @Test
    void getUserSkills_individualUser_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        assertThatThrownBy(() -> skillService.getUserSkills(2L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기업 회원만");
    }

    // 스킬 삭제 - 없음
    @Test
    void deleteSkill_notFound_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);
        given(skillRepository.findByIdAndUser(999L, user)).willReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.deleteSkill(999L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("스킬을 찾을 수 없습니다");
    }

    private SkillRequestDto makeSkillDto(String name, Long categoryId) {
        try {
            SkillRequestDto dto = new SkillRequestDto();
            var nameField = SkillRequestDto.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(dto, name);
            if (categoryId != null) {
                var categoryField = SkillRequestDto.class.getDeclaredField("categoryId");
                categoryField.setAccessible(true);
                categoryField.set(dto, categoryId);
            }
            return dto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User makeUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}