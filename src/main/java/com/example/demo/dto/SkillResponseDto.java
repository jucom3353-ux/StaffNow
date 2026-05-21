package com.example.demo.dto;

import com.example.demo.entity.Skill;
import lombok.Getter;

@Getter
public class SkillResponseDto {

    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;

    public SkillResponseDto(Skill skill) {
        this.id = skill.getId();
        this.name = skill.getName();
        if (skill.getCategory() != null) {
            this.categoryId = skill.getCategory().getId();
            this.categoryName = skill.getCategory().getName();
        }
    }
}