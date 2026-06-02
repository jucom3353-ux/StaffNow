package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    // 직종 뱃지 업데이트 (근무 완료 시 호출)
    @Transactional
    public void updateSpecialtyBadge(User user) {
        List<Object[]> result = applicationRepository.findTopCategoryByUser(user);
        if (result != null && !result.isEmpty()) {
            String topCategory = (String) result.get(0)[0];
            user.setSpecialtyBadge(topCategory);
            userRepository.save(user);
            log.info("직종 뱃지 업데이트: userId={}, badge={}", 
                    user.getId(), topCategory);
        }
    }
}