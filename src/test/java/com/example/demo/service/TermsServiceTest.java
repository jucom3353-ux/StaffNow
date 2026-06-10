package com.example.demo.service;

import com.example.demo.dto.TermsRequestDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.TermsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TermsServiceTest {

    @InjectMocks
    private TermsService termsService;

    @Mock private TermsRepository termsRepository;

    // 최신 약관 조회 - 없음
    @Test
    void getLatestTerms_notFound_throwsException() {
        given(termsRepository.findTopByTypeAndIsActiveTrueOrderByCreatedAtDesc(TermsType.SERVICE))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> termsService.getLatestTerms(TermsType.SERVICE))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("약관을 찾을 수 없습니다");
    }

    // 약관 등록 - ADMIN 아님
    @Test
    void createTerms_notAdmin_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        assertThatThrownBy(() -> termsService.createTerms(new TermsRequestDto(), user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("관리자만");
    }

    // 약관 수정 - ADMIN 아님
    @Test
    void updateTerms_notAdmin_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        assertThatThrownBy(() -> termsService.updateTerms(1L, new TermsRequestDto(), user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("관리자만");
    }

    // 약관 수정 - 없음
    @Test
    void updateTerms_notFound_throwsException() {
        User user = makeUser(1L, Role.ADMIN);
        given(termsRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> termsService.updateTerms(999L, new TermsRequestDto(), user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("약관을 찾을 수 없습니다");
    }

    private User makeUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}