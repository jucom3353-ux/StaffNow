package com.example.demo.dto;

public class UserResponseDto {

    private String name;
    private double rating;

    public UserResponseDto(String name, double rating) {
        this.name = name;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }
}