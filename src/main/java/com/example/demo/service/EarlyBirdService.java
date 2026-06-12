package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.EarlyBirdRequestDto;
import com.example.demo.dto.EarlyBirdResponseDto;
import com.example.demo.entity.EarlyBird;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.EarlyBirdRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EarlyBirdService {

    private final EarlyBirdRepository earlyBirdRepository;

    // 사전 등록
    @Transactional
    public EarlyBirdResponseDto register(EarlyBirdRequestDto requestDto) {
        if (earlyBirdRepository.existsByEmail(requestDto.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        EarlyBird earlyBird = new EarlyBird();
        earlyBird.setEmail(requestDto.getEmail());
        earlyBird.setMarketingAgreed(requestDto.isMarketingAgreed());

        return new EarlyBirdResponseDto(earlyBirdRepository.save(earlyBird));
    }

    // 전체 목록 조회 (ADMIN)
    @Transactional(readOnly = true)
    public List<EarlyBirdResponseDto> getAll(User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        return earlyBirdRepository.findAll()
                .stream().map(EarlyBirdResponseDto::new).collect(Collectors.toList());
    }

    // 마케팅 동의자만 조회 (ADMIN)
    @Transactional(readOnly = true)
    public List<EarlyBirdResponseDto> getMarketingAgreed(User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        return earlyBirdRepository.findByMarketingAgreedTrue()
                .stream().map(EarlyBirdResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getCount() {
    return earlyBirdRepository.count();
}
}