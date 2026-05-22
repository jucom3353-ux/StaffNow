package com.example.demo.service;

import com.example.demo.dto.PreferredWorkTimeRequestDto;
import com.example.demo.dto.PreferredWorkTimeResponseDto;
import com.example.demo.entity.PreferredWorkTime;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
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

    @Transactional
    public List<PreferredWorkTimeResponseDto> savePreferredWorkTime(
            PreferredWorkTimeRequestDto requestDto, User loginUser) {

        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        if (requestDto.getTimeTypes() == null || requestDto.getTimeTypes().isEmpty()) {
            throw new CustomException(ErrorCode.PREFERRED_TIME_REQUIRED);
        }

        for (String type : requestDto.getTimeTypes()) {
            if (!VALID_TYPES.contains(type)) {
                throw new CustomException(ErrorCode.INVALID_TIME_TYPE,
                        "유효하지 않은 시간대입니다: " + type);
            }
        }

        preferredWorkTimeRepository.deleteByUser(loginUser);

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

    @Transactional(readOnly = true)
    public List<PreferredWorkTimeResponseDto> getPreferredWorkTime(User loginUser) {
        return preferredWorkTimeRepository.findByUser(loginUser).stream()
                .map(PreferredWorkTimeResponseDto::new).collect(Collectors.toList());
    }

    @Transactional
    public void deletePreferredWorkTime(User loginUser) {
        preferredWorkTimeRepository.deleteByUser(loginUser);
    }
}