package com.example.demo.dto;

import com.example.demo.entity.User;
import lombok.Getter;

@Getter
public class WorkerSearchResponseDto {

    private Long id;
    private String name;
    private String phone;
    private String mbti;
    private double temperature;
    private int noShowCount;

    public WorkerSearchResponseDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.mbti = user.getMbti();
        this.temperature = user.getTemperature();
        this.noShowCount = user.getNoShowCount();
    }
}