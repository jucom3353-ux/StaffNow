package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.User;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateSpecialtyBadge(User user) {
        List<Object[]> result = applicationRepository.findTopCategoryByUser(user);
        if (result != null && !result.isEmpty()) {
            String topCategory = (String) result.get(0)[0];
            user.setSpecialtyBadge(topCategory);
            log.info("직종 뱃지 업데이트: userId={}, badge={}",
                    user.getId(), topCategory);
        }
        userRepository.save(user);
    }
}