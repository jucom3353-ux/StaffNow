package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.GoalRequestDto;
import com.example.demo.dto.GoalResponseDto;
import com.example.demo.entity.Goal;
import com.example.demo.entity.MileageType;
import com.example.demo.entity.NotificationType;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.GoalRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final MileageService mileageService;
    private final NotificationService notificationService;

    private static final int GOAL_BONUS_MILEAGE = 5000;

    // 목표 설정
    @Transactional
    public GoalResponseDto setGoal(GoalRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        // 진행 중인 목표 있으면 불가
        goalRepository.findByUserAndAchievedFalse(loginUser)
                .ifPresent(g -> {
                    throw new CustomException(ErrorCode.GOAL_ALREADY_EXISTS);
                });

        Goal goal = new Goal();
        goal.setUser(loginUser);
        goal.setTargetAmount(requestDto.getTargetAmount());
        goal.setCurrentAmount(0);

        return new GoalResponseDto(goalRepository.save(goal));
    }

    // 현재 목표 조회
    @Transactional(readOnly = true)
    public GoalResponseDto getMyGoal(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        return goalRepository.findByUserAndAchievedFalse(loginUser)
                .map(GoalResponseDto::new)
                .orElse(null);
    }

    // 목표 내역 조회 (달성 포함 전체)
    @Transactional(readOnly = true)
    public List<GoalResponseDto> getMyGoalHistory(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        return goalRepository.findByUserOrderByCreatedAtDesc(loginUser)
                .stream()
                .map(GoalResponseDto::new)
                .collect(Collectors.toList());
    }

    // 목표 삭제 (달성 전에만 가능)
    @Transactional
    public void deleteGoal(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        Goal goal = goalRepository.findByUserAndAchievedFalse(loginUser)
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_NOT_FOUND));

        goalRepository.delete(goal);
    }

    // 급여 지급 시 목표 누적 (PayrollService에서 호출)
    @Transactional
    public void addToGoal(User worker, int amount) {
        goalRepository.findByUserAndAchievedFalse(worker).ifPresent(goal -> {
            int newAmount = goal.getCurrentAmount() + amount;
            goal.setCurrentAmount(newAmount);

            // 목표 달성 시
            if (newAmount >= goal.getTargetAmount()) {
                goal.setCurrentAmount(goal.getTargetAmount());
                goal.setAchieved(true);
                goal.setAchievedAt(LocalDateTime.now());
                goalRepository.save(goal);

                // 보너스 마일리지 지급
                mileageService.addMileage(
                        worker,
                        MileageType.STREAK_BONUS,
                        GOAL_BONUS_MILEAGE,
                        "목표 금액 달성 보너스",
                        goal.getId()
                );

                // 알림 발송
                notificationService.send(
                        worker,
                        NotificationType.GOAL_ACHIEVED,
                        "목표 금액 " + goal.getTargetAmount() +
                        "원을 달성했습니다! 보너스 " + GOAL_BONUS_MILEAGE + " 마일리지가 지급되었습니다.",
                        goal.getId()
                );

                log.info("목표 달성: userId={}, targetAmount={}",
                        worker.getId(), goal.getTargetAmount());
            } else {
                goalRepository.save(goal);
            }
        });
    }
}