package com.example.demo.dto;

import com.example.demo.entity.InquiryType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryRequestDto {
    private InquiryType type;
    private String title;
    private String content;
}