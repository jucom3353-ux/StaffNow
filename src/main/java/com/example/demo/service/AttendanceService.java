package com.example.demo.service;

import com.example.demo.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class AttendanceService {

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/attendance";

    public String uploadPhoto(MultipartFile file, User loginUser) {

        if (file.isEmpty()) {
            throw new RuntimeException("파일이 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png")) {
            throw new RuntimeException("jpg, jpeg, png 파일만 업로드 가능합니다.");
        }

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String savedFilename = UUID.randomUUID() + ext;
        File savedFile = new File(uploadDir + "/" + savedFilename);

        try {
            file.transferTo(savedFile);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패");
        }

        return "http://localhost:8080/uploads/attendance/" + savedFilename;
    }
}