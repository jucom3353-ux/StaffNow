package com.example.demo.service;

import com.example.demo.config.OAuthProperties;
import com.example.demo.entity.AuthProvider;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final OAuthProperties oAuthProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    // ===== 카카오 =====
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

        return findOrCreateUser(email, name, AuthProvider.KAKAO, providerId);
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

    // ===== 구글 =====
    @Transactional
    public User loginWithGoogle(String code) {
        String accessToken = getGoogleAccessToken(code);
        Map<String, Object> userInfo = getGoogleUserInfo(accessToken);

        String providerId = (String) userInfo.get("sub");
        String email = (String) userInfo.getOrDefault("email",
                providerId + "@google.com");
        String name = (String) userInfo.getOrDefault("name", "구글유저");

        return findOrCreateUser(email, name, AuthProvider.GOOGLE, providerId);
    }

    private String getGoogleAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", oAuthProperties.getGoogle().getClientId());
        params.add("client_secret", oAuthProperties.getGoogle().getClientSecret());
        params.add("redirect_uri", oAuthProperties.getGoogle().getRedirectUri());
        params.add("code", code);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token",
                new HttpEntity<>(params, headers),
                Map.class
        );
        return (String) response.getBody().get("access_token");
    }

    private Map<String, Object> getGoogleUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        return response.getBody();
    }

    // ===== 네이버 =====
    @Transactional
    public User loginWithNaver(String code, String state) {
        String accessToken = getNaverAccessToken(code, state);
        Map<String, Object> userInfo = getNaverUserInfo(accessToken);
        Map<String, Object> response =
                (Map<String, Object>) userInfo.get("response");

        String providerId = (String) response.get("id");
        String email = (String) response.getOrDefault("email",
                providerId + "@naver.com");
        String name = (String) response.getOrDefault("name", "네이버유저");

        return findOrCreateUser(email, name, AuthProvider.NAVER, providerId);
    }

    private String getNaverAccessToken(String code, String state) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", oAuthProperties.getNaver().getClientId());
        params.add("client_secret", oAuthProperties.getNaver().getClientSecret());
        params.add("redirect_uri", oAuthProperties.getNaver().getRedirectUri());
        params.add("code", code);
        params.add("state", state);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://nid.naver.com/oauth2.0/token",
                new HttpEntity<>(params, headers),
                Map.class
        );
        return (String) response.getBody().get("access_token");
    }

    private Map<String, Object> getNaverUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        return response.getBody();
    }

    // ===== 공통: DB에 없으면 자동 회원가입 =====
    private User findOrCreateUser(
            String email, String name,
            AuthProvider provider, String providerId) {

        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setProvider(provider);
                    newUser.setProviderId(providerId);
                    newUser.setRole(Role.INDIVIDUAL); // 소셜 로그인 기본 역할
                    newUser.setNoShowCount(0);
                    newUser.setTemperature(36.5);
                    newUser.setPassword(""); // 소셜 로그인은 비밀번호 없음
                    return userRepository.save(newUser);
                });
    }
}