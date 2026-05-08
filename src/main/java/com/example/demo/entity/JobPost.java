package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter // getContent()를 자동으로 생성해줍니다.
@Setter // setContent()를 자동으로 생성해줍니다.
@NoArgsConstructor
public class JobPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;   // 제목 필드
    private String content; // 👈 이 필드가 있어야 setContent, getContent 에러가 사라집니다.
}