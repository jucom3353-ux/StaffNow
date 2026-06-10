package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
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
public class ProfileImageService {

    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String fileBaseUrl;

    @Transactional
    public String uploadProfileImage(MultipartFile file, User loginUser) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        String ext = originalFilename
                .substring(originalFilename.lastIndexOf(".")).toLowerCase();

        if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png")) {
            throw new CustomException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        String dirPath = System.getProperty("user.dir") + "/" + uploadDir + "/profile";
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        if (loginUser.getProfileImageUrl() != null) {
            String oldFileName = loginUser.getProfileImageUrl()
                    .substring(loginUser.getProfileImageUrl().lastIndexOf("/") + 1);
            File oldFile = new File(dirPath + "/" + oldFileName);
            if (oldFile.exists()) oldFile.delete();
        }

        String savedFilename = UUID.randomUUID() + ext;
        File savedFile = new File(dirPath + "/" + savedFilename);

        try {
            file.transferTo(savedFile);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        String url = fileBaseUrl + "/uploads/profile/" + savedFilename;
        loginUser.setProfileImageUrl(url);
        userRepository.save(loginUser);

        return url;
    }

    @Transactional
    public void deleteProfileImage(User loginUser) {
        if (loginUser.getProfileImageUrl() == null) {
            throw new CustomException(ErrorCode.NO_PROFILE_IMAGE);
        }

        String dirPath = System.getProperty("user.dir") + "/" + uploadDir + "/profile";
        String oldFileName = loginUser.getProfileImageUrl()
                .substring(loginUser.getProfileImageUrl().lastIndexOf("/") + 1);
        File oldFile = new File(dirPath + "/" + oldFileName);
        if (oldFile.exists()) oldFile.delete();

        loginUser.setProfileImageUrl(null);
        userRepository.save(loginUser);
    }
}