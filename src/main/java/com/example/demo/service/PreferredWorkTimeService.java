package com.example.demo.service;

import com.example.demo.dto.PreferredWorkTimeRequestDto;
import com.example.demo.dto.PreferredWorkTimeResponseDto;
import com.example.demo.entity.PreferredWorkTime;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.PreferredWorkTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreferredWorkTimeService {

    private final PreferredWorkTimeRepository preferredWorkTimeRepository;

    private static final List<String> VALID_TYPES =
            List.of("주말", "평일", "새벽", "오전", "오후", "저녁");

    // 선호 근무 시간 저장 (기존 삭제 후 재저장)
    @Transactional
    public List<PreferredWorkTimeResponseDto> savePreferredWorkTime(
            PreferredWorkTimeRequestDto requestDto, User loginUser) {

        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new RuntimeException("개인 회원만 선호 근무 시간을 설정할 수 있습니다.");
        }

        if (requestDto.getTimeTypes() == null || requestDto.getTimeTypes().isEmpty()) {
            throw new RuntimeException("선호 시간대를 1개 이상 선택해주세요.");
        }

        for (String type : requestDto.getTimeTypes()) {
            if (!VALID_TYPES.contains(type)) {
                throw new RuntimeException("유효하지 않은 시간대입니다: " + type);
            }
        }

        // 기존 설정 삭제
        preferredWorkTimeRepository.deleteByUser(loginUser);

        // 새로 저장
        List<PreferredWorkTime> saved = requestDto.getTimeTypes().stream()
                .map(type -> {
                    PreferredWorkTime p = new PreferredWorkTime();
                    p.setUser(loginUser);
                    p.setTimeType(type);
                    return preferredWorkTimeRepository.save(p);
                })
                .collect(Collectors.toList());

        return saved.stream()
                .map(PreferredWorkTimeResponseDto::new)
                .collect(Collectors.toList());
    }

    // 선호 근무 시간 조회
    @Transactional(readOnly = true)
    public List<PreferredWorkTimeResponseDto> getPreferredWorkTime(User loginUser) {
        return preferredWorkTimeRepository.findByUser(loginUser)
                .stream()
                .map(PreferredWorkTimeResponseDto::new)
                .collect(Collectors.toList());
    }

    // 선호 근무 시간 전체 삭제
    @Transactional
    public void deletePreferredWorkTime(User loginUser) {
        preferredWorkTimeRepository.deleteByUser(loginUser);
    }
}