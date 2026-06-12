package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.InquiryRequestDto;
import com.example.demo.dto.InquiryResponseDto;
import com.example.demo.entity.Inquiry;
import com.example.demo.entity.InquiryStatus;
import com.example.demo.entity.InquiryType;
import com.example.demo.entity.NotificationType;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.InquiryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final NotificationService notificationService;

    // 문의/제안/신고 등록
    @Transactional
    public InquiryResponseDto createInquiry(
            InquiryRequestDto requestDto, User loginUser) {

        Inquiry inquiry = new Inquiry();
        inquiry.setUser(loginUser);
        inquiry.setType(requestDto.getType());
        inquiry.setTitle(requestDto.getTitle());
        inquiry.setContent(requestDto.getContent());

        return new InquiryResponseDto(inquiryRepository.save(inquiry));
    }

    // 내 문의 목록 조회
    @Transactional(readOnly = true)
    public List<InquiryResponseDto> getMyInquiries(User loginUser) {
        return inquiryRepository.findByUserOrderByCreatedAtDesc(loginUser)
                .stream()
                .map(InquiryResponseDto::new)
                .collect(Collectors.toList());
    }

    // 내 문의 단건 조회
    @Transactional(readOnly = true)
    public InquiryResponseDto getMyInquiry(Long inquiryId, User loginUser) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INQUIRY_NOT_FOUND));

        if (!inquiry.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        return new InquiryResponseDto(inquiry);
    }

    // 답변 등록 (관리자)
    @Transactional
    public InquiryResponseDto replyInquiry(
            Long inquiryId, String reply, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INQUIRY_NOT_FOUND));

        inquiry.setAdminReply(reply);
        inquiry.setAdmin(loginUser);
        inquiry.setStatus(InquiryStatus.REPLIED);
        inquiry.setRepliedAt(LocalDateTime.now());

        Inquiry saved = inquiryRepository.save(inquiry);

        notificationService.send(
                inquiry.getUser(),
                NotificationType.INQUIRY_REPLIED,
                "'" + inquiry.getTitle() + "' 문의에 답변이 등록되었습니다.",
                saved.getId()
        );

        return new InquiryResponseDto(saved);
    }

    // 문의 종료 (관리자)
    @Transactional
    public InquiryResponseDto closeInquiry(Long inquiryId, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INQUIRY_NOT_FOUND));

        inquiry.setStatus(InquiryStatus.CLOSED);

        return new InquiryResponseDto(inquiryRepository.save(inquiry));
    }

    // 전체 문의 조회 (관리자)
    @Transactional(readOnly = true)
    public List<InquiryResponseDto> getAllInquiries(
            InquiryType type, InquiryStatus status, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        List<Inquiry> inquiries;

        if (type != null && status != null) {
            inquiries = inquiryRepository
                    .findByTypeAndStatusOrderByCreatedAtDesc(type, status);
        } else if (type != null) {
            inquiries = inquiryRepository.findByTypeOrderByCreatedAtDesc(type);
        } else if (status != null) {
            inquiries = inquiryRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            inquiries = inquiryRepository.findAll();
        }

        return inquiries.stream()
                .map(InquiryResponseDto::new)
                .collect(Collectors.toList());
    }
}