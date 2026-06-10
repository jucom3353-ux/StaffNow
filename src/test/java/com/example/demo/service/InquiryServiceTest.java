package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.InquiryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @InjectMocks
    private InquiryService inquiryService;

    @Mock private InquiryRepository inquiryRepository;
    @Mock private NotificationService notificationService;

    // 문의 단건 조회 - 없음
    @Test
    void getMyInquiry_notFound_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);
        given(inquiryRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.getMyInquiry(999L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("문의를 찾을 수 없습니다");
    }

    // 문의 단건 조회 - 본인 아님
    @Test
    void getMyInquiry_notOwner_throwsException() {
        User owner = makeUser(1L, Role.INDIVIDUAL);
        User other = makeUser(2L, Role.INDIVIDUAL);

        Inquiry inquiry = new Inquiry();
        inquiry.setUser(owner);

        given(inquiryRepository.findById(1L)).willReturn(Optional.of(inquiry));

        assertThatThrownBy(() -> inquiryService.getMyInquiry(1L, other))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("접근 권한이 없습니다");
    }

    // 답변 등록 - ADMIN 아님
    @Test
    void replyInquiry_notAdmin_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        assertThatThrownBy(() -> inquiryService.replyInquiry(1L, "답변", user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("관리자만");
    }

    // 문의 종료 - ADMIN 아님
    @Test
    void closeInquiry_notAdmin_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        assertThatThrownBy(() -> inquiryService.closeInquiry(1L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("관리자만");
    }

    // 전체 문의 조회 - ADMIN 아님
    @Test
    void getAllInquiries_notAdmin_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        assertThatThrownBy(() -> inquiryService.getAllInquiries(null, null, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("관리자만");
    }

    private User makeUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}