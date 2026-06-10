package com.example.demo.service;

import com.example.demo.dto.FaqRequestDto;
import com.example.demo.dto.FaqResponseDto;
import com.example.demo.entity.Faq;
import com.example.demo.entity.FaqCategory;
import com.example.demo.entity.FaqTarget;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    // 전체 FAQ 조회
    @Transactional(readOnly = true)
    public List<FaqResponseDto> getFaqs(FaqCategory category, FaqTarget target, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return faqRepository.findByQuestionContainingAndIsActiveTrue(keyword)
                    .stream().map(FaqResponseDto::new).collect(Collectors.toList());
        }
        if (category != null) {
            return faqRepository.findByCategoryAndIsActiveTrueOrderByOrderIndexAsc(category)
                    .stream().map(FaqResponseDto::new).collect(Collectors.toList());
        }
        if (target != null) {
            return faqRepository.findByTargetAndIsActiveTrueOrderByOrderIndexAsc(target)
                    .stream().map(FaqResponseDto::new).collect(Collectors.toList());
        }
        return faqRepository.findByIsActiveTrueOrderByOrderIndexAsc()
                .stream().map(FaqResponseDto::new).collect(Collectors.toList());
    }

    // FAQ 단건 조회
    @Transactional(readOnly = true)
    public FaqResponseDto getFaq(Long id) {
        return new FaqResponseDto(faqRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.FAQ_NOT_FOUND)));
    }

    // FAQ 등록 (관리자)
    @Transactional
    public FaqResponseDto createFaq(FaqRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Faq faq = new Faq();
        faq.setCategory(requestDto.getCategory());
        faq.setTarget(requestDto.getTarget());
        faq.setQuestion(requestDto.getQuestion());
        faq.setAnswer(requestDto.getAnswer());
        faq.setOrderIndex(requestDto.getOrderIndex());
        faq.setActive(requestDto.isActive());

        return new FaqResponseDto(faqRepository.save(faq));
    }

    // FAQ 수정 (관리자)
    @Transactional
    public FaqResponseDto updateFaq(Long id, FaqRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.FAQ_NOT_FOUND));

        faq.setCategory(requestDto.getCategory());
        faq.setTarget(requestDto.getTarget());
        faq.setQuestion(requestDto.getQuestion());
        faq.setAnswer(requestDto.getAnswer());
        faq.setOrderIndex(requestDto.getOrderIndex());
        faq.setActive(requestDto.isActive());

        return new FaqResponseDto(faqRepository.save(faq));
    }

    // FAQ 삭제 (관리자)
    @Transactional
    public void deleteFaq(Long id, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        faqRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.FAQ_NOT_FOUND));

        faqRepository.deleteById(id);
    }
}