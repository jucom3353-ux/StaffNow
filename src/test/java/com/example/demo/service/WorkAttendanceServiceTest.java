package com.example.demo.service;

import com.example.demo.dto.CheckInRequestDto;
import com.example.demo.dto.CheckOutRequestDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class WorkAttendanceServiceTest {

    @InjectMocks
    private WorkAttendanceService workAttendanceService;

    @Mock private WorkAttendanceRepository workAttendanceRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private JobPostRepository jobPostRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private PayrollRepository payrollRepository;
    @Mock private GradeService gradeService;

    // 출근 - 지원 없음
    @Test
    void checkIn_applicationNotFound_throwsException() {
        given(applicationRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> workAttendanceService.checkIn(
                999L, new CheckInRequestDto(), makeUser(1L, Role.INDIVIDUAL)))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("지원 내역을 찾을 수 없습니다");
    }

    // 출근 - 본인 지원 아님
    @Test
    void checkIn_notMyApplication_throwsException() {
        User owner = makeUser(1L, Role.INDIVIDUAL);
        User other = makeUser(2L, Role.INDIVIDUAL);

        Application application = makeApplication(owner, ApplicationStatus.APPROVED);
        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));

        assertThatThrownBy(() -> workAttendanceService.checkIn(
                1L, new CheckInRequestDto(), other))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("본인 지원");
    }

    // 출근 - 승인 안 된 지원
    @Test
    void checkIn_notApproved_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);
        Application application = makeApplication(user, ApplicationStatus.APPLIED);
        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));

        assertThatThrownBy(() -> workAttendanceService.checkIn(
                1L, new CheckInRequestDto(), user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("잘못된 상태 전환");
    }

    // 출근 - 이미 출근
    @Test
    void checkIn_alreadyCheckedIn_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);
        Application application = makeApplication(user, ApplicationStatus.APPROVED);
        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));
        given(workAttendanceRepository.findByApplication(application))
                .willReturn(Optional.of(new WorkAttendance()));

        assertThatThrownBy(() -> workAttendanceService.checkIn(
                1L, new CheckInRequestDto(), user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 출근");
    }

    // 퇴근 - 출퇴근 기록 없음
    @Test
    void checkOut_attendanceNotFound_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);
        Application application = makeApplication(user, ApplicationStatus.APPROVED);
        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));
        given(workAttendanceRepository.findByApplication(application))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> workAttendanceService.checkOut(
                1L, new CheckOutRequestDto(), user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("출퇴근 기록을 찾을 수 없습니다");
    }

    // 퇴근 - 이미 퇴근
    @Test
    void checkOut_alreadyCheckedOut_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);
        Application application = makeApplication(user, ApplicationStatus.APPROVED);

        WorkAttendance attendance = new WorkAttendance();
        attendance.setCheckOutTime(java.time.LocalDateTime.now());

        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));
        given(workAttendanceRepository.findByApplication(application))
                .willReturn(Optional.of(attendance));

        assertThatThrownBy(() -> workAttendanceService.checkOut(
                1L, new CheckOutRequestDto(), user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 퇴근");
    }

    // 결근 처리 - INDIVIDUAL이면 예외
    @Test
    void markAbsent_individualUser_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        assertThatThrownBy(() -> workAttendanceService.markAbsent(1L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기업");
    }

    // 공고별 출퇴근 조회 - 본인 공고 아님
    @Test
    void getAttendancesByJobPost_notOwner_throwsException() {
        User owner = makeUser(1L, Role.COMPANY);
        User other = makeUser(2L, Role.COMPANY);

        JobPost post = new JobPost();
        post.setUser(owner);

        given(jobPostRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> workAttendanceService.getAttendancesByJobPost(1L, other))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("본인 공고");
    }

    // 헬퍼 메서드
    private User makeUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }

    private Application makeApplication(User user, ApplicationStatus status) {
        User postOwner = makeUser(99L, Role.COMPANY);
        JobPost post = new JobPost();
        post.setUser(postOwner);

        Application app = new Application();
        app.setUser(user);
        app.setStatus(status);
        app.setJobPost(post);
        return app;
    }
}