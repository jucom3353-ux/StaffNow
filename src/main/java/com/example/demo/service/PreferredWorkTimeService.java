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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreferredWorkTimeService {

    private final PreferredWorkTimeRepository preferredWorkTimeRepository;

    private static final List<String> VALID_DAY_TYPES =
            List.of("평일", "주말", "요일무관");

    private static final List<String> VALID_TIME_TYPES =
            List.of("새벽", "오전", "오후", "저녁", "시간무관");

    @Transactional
    public List<PreferredWorkTimeResponseDto> savePreferredWorkTime(
            PreferredWorkTimeRequestDto requestDto, User loginUser) {

        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        boolean hasDayTypes = requestDto.getDayTypes() != null && !requestDto.getDayTypes().isEmpty();
        boolean hasTimeTypes = requestDto.getTimeTypes() != null && !requestDto.getTimeTypes().isEmpty();

        if (!hasDayTypes && !hasTimeTypes) {
            throw new CustomException(ErrorCode.PREFERRED_TIME_REQUIRED);
        }

        if (hasDayTypes) {
            for (String type : requestDto.getDayTypes()) {
                if (!VALID_DAY_TYPES.contains(type)) {
                    throw new CustomException(ErrorCode.INVALID_TIME_TYPE,
                            "유효하지 않은 근무요일입니다: " + type);
                }
            }
        }

        if (hasTimeTypes) {
            for (String type : requestDto.getTimeTypes()) {
                if (!VALID_TIME_TYPES.contains(type)) {
                    throw new CustomException(ErrorCode.INVALID_TIME_TYPE,
                            "유효하지 않은 시간대입니다: " + type);
                }
            }
        }

        preferredWorkTimeRepository.deleteByUser(loginUser);

        List<PreferredWorkTime> toSave = new ArrayList<>();

        if (hasDayTypes) {
            for (String dayType : requestDto.getDayTypes()) {
                PreferredWorkTime p = new PreferredWorkTime();
                p.setUser(loginUser);
                p.setDayType(dayType);
                toSave.add(p);
            }
        }

        if (hasTimeTypes) {
            for (String timeType : requestDto.getTimeTypes()) {
                PreferredWorkTime p = new PreferredWorkTime();
                p.setUser(loginUser);
                p.setTimeType(timeType);
                toSave.add(p);
            }
        }

        List<PreferredWorkTime> saved = preferredWorkTimeRepository.saveAll(toSave);

        return saved.stream()
                .map(PreferredWorkTimeResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PreferredWorkTimeResponseDto> getPreferredWorkTime(User loginUser) {
        return preferredWorkTimeRepository.findByUser(loginUser).stream()
                .map(PreferredWorkTimeResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePreferredWorkTime(User loginUser) {
        preferredWorkTimeRepository.deleteByUser(loginUser);
    }
}