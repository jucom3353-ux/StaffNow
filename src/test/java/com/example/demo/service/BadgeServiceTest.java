package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @InjectMocks
    private BadgeService badgeService;

    @Mock private ApplicationRepository applicationRepository;
    @Mock private UserRepository userRepository;

    // 뱃지 업데이트 - 카테고리 있으면 뱃지 설정
    @Test
    void updateSpecialtyBadge_withCategory_setBadge() {
        User user = makeUser(1L);
        List<Object[]> result = new java.util.ArrayList<>();
        result.add(new Object[]{"프로모터"});

        given(applicationRepository.findTopCategoryByUser(user)).willReturn(result);

        badgeService.updateSpecialtyBadge(user);

        assertThat(user.getSpecialtyBadge()).isEqualTo("프로모터");
        verify(userRepository).save(user);
    }

    // 뱃지 업데이트 - 카테고리 없으면 뱃지 미설정
    @Test
    void updateSpecialtyBadge_noCategory_noBadge() {
        User user = makeUser(1L);

        given(applicationRepository.findTopCategoryByUser(user))
                .willReturn(Collections.emptyList());

        badgeService.updateSpecialtyBadge(user);

        assertThat(user.getSpecialtyBadge()).isNull();
        verify(userRepository).save(user);
    }

    // 뱃지 업데이트 - null 반환해도 저장은 됨
    @Test
    void updateSpecialtyBadge_nullResult_saveUser() {
        User user = makeUser(1L);

        given(applicationRepository.findTopCategoryByUser(user)).willReturn(null);

        badgeService.updateSpecialtyBadge(user);

        verify(userRepository).save(user);
    }

    private User makeUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setRole(Role.INDIVIDUAL);
        return user;
    }
}