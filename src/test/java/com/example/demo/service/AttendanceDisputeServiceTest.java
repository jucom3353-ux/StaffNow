package com.example.demo.service;

import com.example.demo.dto.AttendanceDisputeRequestDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.AttendanceDisputeRepository;
import com.example.demo.repository.WorkAttendanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceDisputeServiceTest {

    @InjectMocks
    private AttendanceDisputeService attendanceDisputeService;

    @Mock private AttendanceDisputeRepository attendanceDisputeRepository;
    @Mock private WorkAttendanceRepository workAttendanceRepository;
    @Mock private NotificationService notificationService;

    // 분쟁 신청 - 출퇴근 기록 없음
    @Test
    void createDispute_attendanceNotFound_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);
        AttendanceDisputeRequestDto dto = new AttendanceDisputeRequestDto();
        dto.setAttendanceId(999L);

        given(workAttendanceRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceDisputeService.createDispute(dto, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("출퇴근 기록을 찾을 수 없습니다");
    }

    // 분쟁 신청 - 본인 아님
    @Test
    void createDispute_notMyAttendance_throwsException() {
        User owner = makeUser(1L, Role.INDIVIDUAL);
        User other = makeUser(2L, Role.INDIVIDUAL);

        User postOwner = makeUser(99L, Role.COMPANY);
        JobPost post = new JobPost();
        post.setUser(postOwner);

        Application app = new Application();
        app.setUser(owner);
        app.setJobPost(post);

        WorkAttendance attendance = new WorkAttendance();
        attendance.setApplication(app);

        AttendanceDisputeRequestDto dto = new AttendanceDisputeRequestDto();
        dto.setAttendanceId(1L);

        given(workAttendanceRepository.findById(1L)).willReturn(Optional.of(attendance));

        assertThatThrownBy(() -> attendanceDisputeService.createDispute(dto, other))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("본인 지원");
    }

    // 기업 분쟁 조회 - INDIVIDUAL이면 예외
    @Test
    void getCompanyDisputes_individualUser_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        assertThatThrownBy(() -> attendanceDisputeService.getCompanyDisputes(user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기업 회원만");
    }

    // 분쟁 승인 - ADMIN 아님
    @Test
    void approveDispute_notAdmin_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        assertThatThrownBy(() -> attendanceDisputeService.approveDispute(1L, "메모", user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("관리자만");
    }

    // 분쟁 반려 - ADMIN 아님
    @Test
    void rejectDispute_notAdmin_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        assertThatThrownBy(() -> attendanceDisputeService.rejectDispute(1L, "메모", user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("관리자만");
    }

    // 전체 분쟁 조회 - ADMIN 아님
    @Test
    void getAllDisputes_notAdmin_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        assertThatThrownBy(() -> attendanceDisputeService.getAllDisputes(null, user))
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