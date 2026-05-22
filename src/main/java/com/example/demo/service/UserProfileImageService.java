package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfileImage;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
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

    @Transactional
    public String addProfileImage(MultipartFile file, User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        int currentCount = userProfileImageRepository.countByUser(loginUser);
        if (currentCount >= 10) {
            throw new CustomException(ErrorCode.PROFILE_IMAGE_LIMIT);
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

        if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png")) {
            throw new CustomException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        String dirPath = System.getProperty("user.dir") + File.separator
                + uploadDir + File.separator + "profile";
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        String savedFilename = "profile_" + loginUser.getId() + "_"
                + UUID.randomUUID() + ext;
        File savedFile = new File(dir.getAbsolutePath() + File.separator + savedFilename);

        try {
            file.transferTo(savedFile.toPath());
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        String url = fileBaseUrl + "/uploads/profile/" + savedFilename;

        UserProfileImage profileImage = new UserProfileImage();
        profileImage.setUser(loginUser);
        profileImage.setImageUrl(url);
        profileImage.setOrderIndex(currentCount);
        userProfileImageRepository.save(profileImage);

        if (currentCount == 0) {
            loginUser.setProfileImageUrl(url);
        }

        loginUser.setProfileImageCount(currentCount + 1);
        userRepository.save(loginUser);

        return url;
    }

    @Transactional(readOnly = true)
    public List<String> getProfileImages(User loginUser) {
        return userProfileImageRepository.findByUserOrderByOrderIndexAsc(loginUser).stream()
                .map(UserProfileImage::getImageUrl).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getUserProfileImages(Long userId) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return userProfileImageRepository.findByUserOrderByOrderIndexAsc(target).stream()
                .map(UserProfileImage::getImageUrl).collect(Collectors.toList());
    }

    @Transactional
    public void deleteProfileImage(Long imageId, User loginUser) {
        UserProfileImage image = userProfileImageRepository.findByIdAndUser(imageId, loginUser)
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_IMAGE_NOT_FOUND));

        int deletedIndex = image.getOrderIndex();

        String filename = image.getImageUrl()
                .substring(image.getImageUrl().lastIndexOf("/") + 1);
        File file = new File(System.getProperty("user.dir") + File.separator
                + uploadDir + File.separator + "profile" + File.separator + filename);
        if (file.exists()) file.delete();

        userProfileImageRepository.delete(image);

        List<UserProfileImage> remaining =
                userProfileImageRepository.findByUserOrderByOrderIndexAsc(loginUser);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setOrderIndex(i);
        }
        userProfileImageRepository.saveAll(remaining);

        if (deletedIndex == 0) {
            loginUser.setProfileImageUrl(remaining.isEmpty()
                    ? null : remaining.get(0).getImageUrl());
        }

        loginUser.setProfileImageCount(remaining.size());
        userRepository.save(loginUser);
    }

    @Transactional(readOnly = true)
    public int getProfileImageCount(User user) {
        return userProfileImageRepository.countByUser(user);
    }
}