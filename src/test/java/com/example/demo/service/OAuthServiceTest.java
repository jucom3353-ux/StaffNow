package com.example.demo.service;

import com.example.demo.config.OAuthProperties;
import com.example.demo.entity.AuthProvider;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthServiceTest {

    @InjectMocks
    private OAuthService oAuthService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuthProperties oAuthProperties;

    // 기존 유저 - 이메일로 찾으면 반환
    @Test
    void findOrCreateUser_existingUser_returnsExisting() {
        User existing = new User();
        existing.setId(1L);
        existing.setEmail("test@kakao.com");
        existing.setRole(Role.INDIVIDUAL);

        given(userRepository.findByEmail("test@kakao.com"))
                .willReturn(Optional.of(existing));

        User result = invokeFind("test@kakao.com", "테스트", "12345");

        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository, never()).save(any());
    }

    // 신규 유저 - 없으면 저장
    @Test
    void findOrCreateUser_newUser_savesAndReturns() {
        given(userRepository.findByEmail("new@kakao.com"))
                .willReturn(Optional.empty());

        User newUser = new User();
        newUser.setId(2L);
        newUser.setEmail("new@kakao.com");
        newUser.setRole(Role.INDIVIDUAL);
        newUser.setProvider(AuthProvider.KAKAO);

        given(userRepository.save(any(User.class))).willReturn(newUser);

        User result = invokeFind("new@kakao.com", "신규유저", "67890");

        assertThat(result.getEmail()).isEqualTo("new@kakao.com");
        verify(userRepository).save(any(User.class));
    }

    // 신규 유저 - Role이 INDIVIDUAL로 설정됨
    @Test
    void findOrCreateUser_newUser_roleIsIndividual() {
        given(userRepository.findByEmail("new2@kakao.com"))
                .willReturn(Optional.empty());

        User newUser = new User();
        newUser.setRole(Role.INDIVIDUAL);
        given(userRepository.save(any(User.class))).willReturn(newUser);

        User result = invokeFind("new2@kakao.com", "신규유저2", "11111");

        assertThat(result.getRole()).isEqualTo(Role.INDIVIDUAL);
    }

    // 리플렉션으로 private 메서드 호출
    private User invokeFind(String email, String name, String providerId) {
        try {
            var method = OAuthService.class.getDeclaredMethod(
                    "findOrCreateUser", String.class, String.class, String.class);
            method.setAccessible(true);
            return (User) method.invoke(oAuthService, email, name, providerId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}