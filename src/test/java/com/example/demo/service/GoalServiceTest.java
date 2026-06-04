package com.example.demo.service;

import com.example.demo.dto.GoalRequestDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @InjectMocks
    private GoalService goalService;

    @Mock private GoalRepository goalRepository;
    @Mock private MileageService mileageService;
    @Mock private NotificationService notificationService;

    private User worker;
    private Goal goal;
    private GoalRequestDto requestDto;

    @BeforeEach
    void setUp() {
        worker = new User();
        worker.setId(1L);
        worker.setRole(Role.INDIVIDUAL);
        worker.setName("홍길동");
        worker.setMileage(0);

        goal = new Goal();
        goal.setUser(worker);
        goal.setTargetAmount(1000000);
        goal.setCurrentAmount(0);

        requestDto = new GoalRequestDto();
        requestDto.setTargetAmount(1000000);
    }

    // ===== setGoal() 테스트 =====

    @Test
    @DisplayName("목표 설정 성공")
    void setGoal_success() {
        given(goalRepository.findByUserAndAchievedFalse(worker)).willReturn(Optional.empty());
        given(goalRepository.save(any())).willReturn(goal);

        var result = goalService.setGoal(requestDto, worker);

        assertThat(result.getTargetAmount()).isEqualTo(1000000);
        verify(goalRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("구직자 아닌 경우 목표 설정 불가")
    void setGoal_fail_notWorker() {
        User company = new User();
        company.setRole(Role.COMPANY);

        assertThatThrownBy(() -> goalService.setGoal(requestDto, company))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("진행 중인 목표 있으면 중복 설정 불가")
    void setGoal_fail_alreadyExists() {
        given(goalRepository.findByUserAndAchievedFalse(worker))
                .willReturn(Optional.of(goal));

        assertThatThrownBy(() -> goalService.setGoal(requestDto, worker))
                .isInstanceOf(CustomException.class);
    }

    // ===== deleteGoal() 테스트 =====

    @Test
    @DisplayName("목표 삭제 성공")
    void deleteGoal_success() {
        given(goalRepository.findByUserAndAchievedFalse(worker))
                .willReturn(Optional.of(goal));

        goalService.deleteGoal(worker);

        verify(goalRepository, times(1)).delete(goal);
    }

    @Test
    @DisplayName("진행 중인 목표 없으면 삭제 불가")
    void deleteGoal_fail_notFound() {
        given(goalRepository.findByUserAndAchievedFalse(worker))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.deleteGoal(worker))
                .isInstanceOf(CustomException.class);
    }

    // ===== addToGoal() 테스트 =====

    @Test
    @DisplayName("급여 지급 시 목표 금액 누적")
    void addToGoal_accumulate() {
        goal.setCurrentAmount(500000);
        given(goalRepository.findByUserAndAchievedFalse(worker))
                .willReturn(Optional.of(goal));

        goalService.addToGoal(worker, 300000);

        assertThat(goal.getCurrentAmount()).isEqualTo(800000);
        assertThat(goal.isAchieved()).isFalse();
        verify(goalRepository, times(1)).save(goal);
    }

    @Test
    @DisplayName("목표 금액 달성 시 achieved 처리 + 마일리지 지급")
    void addToGoal_achieved() {
        goal.setCurrentAmount(900000);
        given(goalRepository.findByUserAndAchievedFalse(worker))
                .willReturn(Optional.of(goal));

        goalService.addToGoal(worker, 200000);

        assertThat(goal.isAchieved()).isTrue();
        assertThat(goal.getCurrentAmount()).isEqualTo(1000000); // 목표 금액으로 고정
        assertThat(goal.getAchievedAt()).isNotNull();
        verify(mileageService, times(1)).addMileage(
                eq(worker), eq(MileageType.STREAK_BONUS), eq(5000), any(), any());
        verify(notificationService, times(1)).send(
                eq(worker), eq(NotificationType.GOAL_ACHIEVED), any(), any());
    }

    @Test
    @DisplayName("목표 없으면 누적 안함")
    void addToGoal_noGoal() {
        given(goalRepository.findByUserAndAchievedFalse(worker))
                .willReturn(Optional.empty());

        goalService.addToGoal(worker, 100000);

        verify(goalRepository, never()).save(any());
        verify(mileageService, never()).addMileage(any(), any(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("목표 달성 시 currentAmount가 targetAmount 초과하지 않음")
    void addToGoal_notExceedTarget() {
        goal.setCurrentAmount(950000);
        given(goalRepository.findByUserAndAchievedFalse(worker))
                .willReturn(Optional.of(goal));

        goalService.addToGoal(worker, 500000); // 950000 + 500000 = 1450000 > 1000000

        assertThat(goal.getCurrentAmount()).isEqualTo(1000000); // 목표 금액으로 고정
    }

    // ===== getMyGoalHistory() 테스트 =====

    @Test
    @DisplayName("목표 내역 조회 성공")
    void getMyGoalHistory_success() {
        Goal achievedGoal = new Goal();
        achievedGoal.setUser(worker);
        achievedGoal.setTargetAmount(500000);
        achievedGoal.setCurrentAmount(500000);

        given(goalRepository.findByUserOrderByCreatedAtDesc(worker))
                .willReturn(List.of(goal, achievedGoal));

        var result = goalService.getMyGoalHistory(worker);

        assertThat(result).hasSize(2);
    }
}