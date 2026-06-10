package com.example.demo.service;

import com.example.demo.config.OAuthProperties;
import com.example.demo.entity.AuthProvider;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final OAuthProperties oAuthProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public User loginWithKakao(String code) {
        String accessToken = getKakaoAccessToken(code);
        Map<String, Object> userInfo = getKakaoUserInfo(accessToken);

        String providerId = String.valueOf(userInfo.get("id"));
        Map<String, Object> kakaoAccount =
                (Map<String, Object>) userInfo.get("kakao_account");
        Map<String, Object> profile =
                (Map<String, Object>) kakaoAccount.get("profile");

        String email = (String) kakaoAccount.getOrDefault("email",
                providerId + "@kakao.com");
        String name = (String) profile.getOrDefault("nickname", "카카오유저");

        return findOrCreateUser(email, name, providerId);
    }

    private String getKakaoAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", oAuthProperties.getKakao().getClientId());
        params.add("redirect_uri", oAuthProperties.getKakao().getRedirectUri());
        params.add("code", code);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token",
                new HttpEntity<>(params, headers),
                Map.class
        );
        return (String) response.getBody().get("access_token");
    }

    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        return response.getBody();
    }

    private User findOrCreateUser(String email, String name, String providerId) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setProvider(AuthProvider.KAKAO);
                    newUser.setProviderId(providerId);
                    newUser.setRole(Role.INDIVIDUAL);
                    newUser.setNoShowCount(0);
                    newUser.setTemperature(36.5);
                    newUser.setPassword("");
                    log.info("[카카오 신규 가입] email={}", email);
                    return userRepository.save(newUser);
                });
    }
}