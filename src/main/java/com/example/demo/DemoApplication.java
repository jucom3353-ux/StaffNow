package com.example.demo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableAsync  // ← 추가
public class DemoApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(e ->
                System.setProperty(e.getKey(), e.getValue())
        );

        SpringApplication.run(DemoApplication.class, args);
        System.out.println("=== Promoter 서버 시작 ===");
    }
}