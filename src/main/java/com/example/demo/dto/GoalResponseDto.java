package com.example.demo.dto;

import com.example.demo.entity.Goal;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GoalResponseDto {

    private Long id;
    private int targetAmount;
    private int currentAmount;
    private int remainingAmount;
    private double progressPercent;
    private boolean achieved;
    private LocalDateTime achievedAt;
    private LocalDateTime createdAt;

    public GoalResponseDto(Goal goal) {
        this.id = goal.getId();
        this.targetAmount = goal.getTargetAmount();
        this.currentAmount = goal.getCurrentAmount();
        this.remainingAmount = Math.max(goal.getTargetAmount() - goal.getCurrentAmount(), 0);
        this.progressPercent = goal.getTargetAmount() > 0
                ? Math.min((double) goal.getCurrentAmount() / goal.getTargetAmount() * 100, 100)
                : 0;
        this.achieved = goal.isAchieved();
        this.achievedAt = goal.getAchievedAt();
        this.createdAt = goal.getCreatedAt();
    }
}