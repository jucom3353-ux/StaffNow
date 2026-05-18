package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessLicenseService {

    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String fileBaseUrl;

    @Transactional
    public String uploadLicense(MultipartFile file, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 업로드 가능합니다.");
        }

        if (file.isEmpty()) {
            throw new RuntimeException("파일이 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new RuntimeException("파일명이 올바르지 않습니다.");
        }

        String ext = originalFilename
                .substring(originalFilename.lastIndexOf(".")).toLowerCase();

        if (!ext.equals(".jpg") && !ext.equals(".jpeg") &&
            !ext.equals(".png") && !ext.equals(".pdf")) {
            throw new RuntimeException("jpg, jpeg, png, pdf 파일만 업로드 가능합니다.");
        }

        // 저장 디렉토리 생성
        String dirPath = System.getProperty("user.dir") + "/" + uploadDir + "/license";
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        // 기존 파일 삭제
        if (loginUser.getBusinessLicenseUrl() != null) {
            String oldFileName = loginUser.getBusinessLicenseUrl()
                    .substring(loginUser.getBusinessLicenseUrl().lastIndexOf("/") + 1);
            File oldFile = new File(dirPath + "/" + oldFileName);
            if (oldFile.exists()) oldFile.delete();
        }

        String savedFilename = "license_" + loginUser.getId() + "_"
                + UUID.randomUUID() + ext;
        File savedFile = new File(dirPath + "/" + savedFilename);

        try {
            file.transferTo(savedFile);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage());
        }

        String url = fileBaseUrl + "/uploads/license/" + savedFilename;
        loginUser.setBusinessLicenseUrl(url);
        loginUser.setBusinessLicenseStatus("PENDING");
        userRepository.save(loginUser);

        return url;
    }
}