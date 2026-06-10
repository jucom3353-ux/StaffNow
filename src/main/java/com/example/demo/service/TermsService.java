package com.example.demo.service;

import com.example.demo.dto.TermsRequestDto;
import com.example.demo.dto.TermsResponseDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.Terms;
import com.example.demo.entity.TermsType;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TermsService {

    private final TermsRepository termsRepository;

    // 최신 약관 조회
    @Transactional(readOnly = true)
    public TermsResponseDto getLatestTerms(TermsType type) {
        return new TermsResponseDto(
                termsRepository.findTopByTypeAndIsActiveTrueOrderByCreatedAtDesc(type)
                        .orElseThrow(() -> new CustomException(ErrorCode.TERMS_NOT_FOUND)));
    }

    // 약관 히스토리 조회
    @Transactional(readOnly = true)
    public List<TermsResponseDto> getTermsHistory(TermsType type) {
        return termsRepository.findByTypeOrderByCreatedAtDesc(type)
                .stream()
                .map(TermsResponseDto::new)
                .collect(Collectors.toList());
    }

    // 약관 등록 (관리자)
    @Transactional
    public TermsResponseDto createTerms(TermsRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        // 기존 같은 타입 약관 비활성화
        termsRepository.findTopByTypeAndIsActiveTrueOrderByCreatedAtDesc(requestDto.getType())
                .ifPresent(existing -> {
                    existing.setActive(false);
                    termsRepository.save(existing);
                });

        Terms terms = new Terms();
        terms.setType(requestDto.getType());
        terms.setTitle(requestDto.getTitle());
        terms.setContent(requestDto.getContent());
        terms.setVersion(requestDto.getVersion());
        terms.setActive(true);

        return new TermsResponseDto(termsRepository.save(terms));
    }

    // 약관 수정 (관리자)
    @Transactional
    public TermsResponseDto updateTerms(
            Long id, TermsRequestDto requestDto, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Terms terms = termsRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.TERMS_NOT_FOUND));

        terms.setTitle(requestDto.getTitle());
        terms.setContent(requestDto.getContent());
        terms.setVersion(requestDto.getVersion());
        terms.setActive(requestDto.isActive());

        return new TermsResponseDto(termsRepository.save(terms));
    }
}