package com.example.demo.service;

import com.example.demo.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class AttendanceService {

    @Value("${file.base-url}")
    private String fileBaseUrl;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String uploadPhoto(MultipartFile file, User loginUser) {

        if (file.isEmpty()) {
            throw new RuntimeException("파일이 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new RuntimeException("파일명이 올바르지 않습니다.");
        }

        String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png")) {
            throw new RuntimeException("jpg, jpeg, png 파일만 업로드 가능합니다.");
        }

        File dir = new File(System.getProperty("user.dir") + "/" + uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String savedFilename = UUID.randomUUID() + ext;
        File savedFile = new File(dir.getAbsolutePath() + "/" + savedFilename);

        try {
            file.transferTo(savedFile);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage());
        }

        return fileBaseUrl + "/uploads/attendance/" + savedFilename;
    }
}