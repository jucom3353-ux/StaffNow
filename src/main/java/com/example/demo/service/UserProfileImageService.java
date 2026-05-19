package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfileImage;
import com.example.demo.repository.UserProfileImageRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileImageService {

    private final UserProfileImageRepository userProfileImageRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String fileBaseUrl;

    // 프로필 사진 추가 (최대 10장)
    @Transactional
    public String addProfileImage(MultipartFile file, User loginUser) {

        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new RuntimeException("개인 회원만 프로필 사진을 등록할 수 있습니다.");
        }

        int currentCount = userProfileImageRepository.countByUser(loginUser);
        if (currentCount >= 10) {
            throw new RuntimeException("프로필 사진은 최대 10장까지 등록 가능합니다.");
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

        if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png")) {
            throw new RuntimeException("jpg, jpeg, png 파일만 업로드 가능합니다.");
        }

        String dirPath = System.getProperty("user.dir") + "/" + uploadDir + "/profile";
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        String savedFilename = "profile_" + loginUser.getId() + "_"
                + UUID.randomUUID() + ext;
        File savedFile = new File(dirPath + "/" + savedFilename);

        try {
            file.transferTo(savedFile);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage());
        }

        String url = fileBaseUrl + "/uploads/profile/" + savedFilename;

        UserProfileImage profileImage = new UserProfileImage();
        profileImage.setUser(loginUser);
        profileImage.setImageUrl(url);
        profileImage.setOrderIndex(currentCount);
        userProfileImageRepository.save(profileImage);

        // 첫 번째 사진 → 대표 사진으로 설정
        if (currentCount == 0) {
            loginUser.setProfileImageUrl(url);
        }

        // 사진 개수 업데이트
        loginUser.setProfileImageCount(currentCount + 1);
        userRepository.save(loginUser);

        return url;
    }

    // 프로필 사진 목록 조회 (본인)
    @Transactional(readOnly = true)
    public List<String> getProfileImages(User loginUser) {
        return userProfileImageRepository
                .findByUserOrderByOrderIndexAsc(loginUser)
                .stream()
                .map(UserProfileImage::getImageUrl)
                .collect(Collectors.toList());
    }

    // 특정 유저 프로필 사진 조회 (기업용)
    @Transactional(readOnly = true)
    public List<String> getUserProfileImages(Long userId) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        return userProfileImageRepository
                .findByUserOrderByOrderIndexAsc(target)
                .stream()
                .map(UserProfileImage::getImageUrl)
                .collect(Collectors.toList());
    }

    // 프로필 사진 삭제
    @Transactional
    public void deleteProfileImage(Long imageId, User loginUser) {

        UserProfileImage image = userProfileImageRepository
                .findByIdAndUser(imageId, loginUser)
                .orElseThrow(() -> new RuntimeException("사진 없음 또는 권한 없음"));

        int deletedIndex = image.getOrderIndex();

        // 파일 삭제
        String filename = image.getImageUrl()
                .substring(image.getImageUrl().lastIndexOf("/") + 1);
        File file = new File(System.getProperty("user.dir") + "/" +
                uploadDir + "/profile/" + filename);
        if (file.exists()) file.delete();

        userProfileImageRepository.delete(image);

        // orderIndex 재정렬
        List<UserProfileImage> remaining =
                userProfileImageRepository.findByUserOrderByOrderIndexAsc(loginUser);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setOrderIndex(i);
        }
        userProfileImageRepository.saveAll(remaining);

        // 대표 사진 삭제된 경우 다음 사진으로 교체
        if (deletedIndex == 0) {
            if (!remaining.isEmpty()) {
                loginUser.setProfileImageUrl(remaining.get(0).getImageUrl());
            } else {
                loginUser.setProfileImageUrl(null);
            }
        }

        // 사진 개수 업데이트
        loginUser.setProfileImageCount(remaining.size());
        userRepository.save(loginUser);
    }

    // 사진 개수 조회
    @Transactional(readOnly = true)
    public int getProfileImageCount(User user) {
        return userProfileImageRepository.countByUser(user);
    }
}