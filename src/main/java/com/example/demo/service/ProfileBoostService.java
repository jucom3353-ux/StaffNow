package com.example.demo.service;

import com.example.demo.dto.ProfileBoostResponseDto;
import com.example.demo.entity.NotificationType;
import com.example.demo.entity.ProfileBoost;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.PreferredCategoryRepository;
import com.example.demo.repository.ProfileBoostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileBoostService {

    private final ProfileBoostRepository profileBoostRepository;
    private final PreferredCategoryRepository preferredCategoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ProfileBoostResponseDto startBoost(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

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

        ProfileBoost saved = profileBoostRepository.save(boost);

        // 부스트 팝업: 구직자의 선호 카테고리와 매칭되는 OPEN 공고 보유 기업에게 발송
        sendBoostPopupToCompanies(loginUser);

        return new ProfileBoostResponseDto(saved);
    }

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

    @Transactional(readOnly = true)
    public boolean isBoostActive(User loginUser) {
        return profileBoostRepository
                .findActiveBoost(loginUser, LocalDateTime.now())
                .isPresent();
    }

    // 부스트 팝업: 구직자 선호 카테고리 → 해당 카테고리 OPEN 공고 보유 기업에게 팝업
    private void sendBoostPopupToCompanies(User worker) {
        try {
            List<Long> categoryIds = preferredCategoryRepository
                    .findCategoryIdsByUser(worker);

            if (categoryIds.isEmpty()) return;

            List<User> companies = userRepository
                    .findCompaniesWithOpenPostsByCategories(categoryIds);

            companies.forEach(company ->
                    notificationService.sendPopup(
                            company,
                            NotificationType.BOOST_ACTIVATED,
                            "[부스트] " + worker.getName()
                                    + "님이 관심 카테고리에서 상단 노출을 시작했습니다.",
                            worker.getId()
                    )
            );
        } catch (Exception e) {
            log.warn("부스트 팝업 발송 실패: workerId={}, error={}",
                    worker.getId(), e.getMessage());
        }
    }
}