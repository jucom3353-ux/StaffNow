package com.example.demo.service;

import com.example.demo.dto.ProfileBoostResponseDto;
import com.example.demo.entity.ProfileBoost;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ProfileBoostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileBoostService {

    private final ProfileBoostRepository profileBoostRepository;

    // 부스트 시작 (7일)
    @Transactional
    public ProfileBoostResponseDto startBoost(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        // 이미 활성 부스트 있으면 불가
        profileBoostRepository.findActiveBoost(loginUser, LocalDateTime.now())
                .ifPresent(b -> {
                    throw new CustomException(ErrorCode.BOOST_ALREADY_ACTIVE);
                });

        LocalDateTime now = LocalDateTime.now();

        ProfileBoost boost = new ProfileBoost();
        boost.setUser(loginUser);
        boost.setStartAt(now);
        boost.setEndAt(now.plusDays(7));
        boost.setActive(true);

        return new ProfileBoostResponseDto(profileBoostRepository.save(boost));
    }

    // 부스트 취소
    @Transactional
    public void cancelBoost(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        ProfileBoost boost = profileBoostRepository
                .findActiveBoost(loginUser, LocalDateTime.now())
                .orElseThrow(() -> new CustomException(ErrorCode.BOOST_NOT_FOUND));

        boost.setActive(false);
        profileBoostRepository.save(boost);
    }

    // 내 부스트 현황 조회
    @Transactional(readOnly = true)
    public List<ProfileBoostResponseDto> getMyBoosts(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        return profileBoostRepository.findByUser(loginUser)
                .stream()
                .map(ProfileBoostResponseDto::new)
                .collect(Collectors.toList());
    }

    // 현재 부스트 활성 여부
    @Transactional(readOnly = true)
    public boolean isBoostActive(User loginUser) {
        return profileBoostRepository
                .findActiveBoost(loginUser, LocalDateTime.now())
                .isPresent();
    }
}