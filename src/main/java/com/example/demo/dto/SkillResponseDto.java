package com.example.demo.dto;

import com.example.demo.entity.Skill;
import lombok.Getter;

@Getter
public class SkillResponseDto {

    private Long id;
    private String name;
    private String category;

    public SkillResponseDto(Skill skill) {
        this.id = skill.getId();
        this.name = skill.getName();
        this.category = skill.getCategory();
    }
}