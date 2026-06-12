package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.BusinessLicenseStatus;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

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
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        String ext = originalFilename
                .substring(originalFilename.lastIndexOf(".")).toLowerCase();

        if (!ext.equals(".jpg") && !ext.equals(".jpeg")
                && !ext.equals(".png") && !ext.equals(".pdf")) {
            throw new CustomException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        String dirPath = System.getProperty("user.dir") + "/" + uploadDir + "/license";
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();

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
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        String url = fileBaseUrl + "/uploads/license/" + savedFilename;
        loginUser.setBusinessLicenseUrl(url);
        loginUser.setBusinessLicenseStatus(BusinessLicenseStatus.PENDING);
        userRepository.save(loginUser);

        return url;
    }
}