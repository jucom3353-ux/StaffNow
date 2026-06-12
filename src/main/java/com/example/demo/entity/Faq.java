package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "faq")
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private FaqCategory category; // WAGE, DISMISSAL, CONTRACT, WORK, ETC

    @Enumerated(EnumType.STRING)
    private FaqTarget target; // ALL, WORKER, COMPANY

    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;

    private int orderIndex = 0;  // 노출 순서
    private boolean isActive = true;

    public Long getId() { return id; }
    public FaqCategory getCategory() { return category; }
    public FaqTarget getTarget() { return target; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public int getOrderIndex() { return orderIndex; }
    public boolean isActive() { return isActive; }

    public void setCategory(FaqCategory category) { this.category = category; }
    public void setTarget(FaqTarget target) { this.target = target; }
    public void setQuestion(String question) { this.question = question; }
    public void setAnswer(String answer) { this.answer = answer; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public void setActive(boolean active) { isActive = active; }
}