// EventPhotoService.java
package com.example.demo.service;

import com.example.demo.dto.EventPhotoResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.EventPhotoRepository;
import com.example.demo.repository.JobCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventPhotoService {

    private final EventPhotoRepository eventPhotoRepository;
    private final ApplicationRepository applicationRepository;
    private final JobCategoryRepository jobCategoryRepository;

    private static final int MAX_EVENT_PHOTOS = 10;

    @Transactional
    public EventPhotoResponseDto addEventPhoto(
            String imageUrl, String description,
            Long applicationId, Long categoryId, User loginUser) {

        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        if (eventPhotoRepository.countByUser(loginUser) >= MAX_EVENT_PHOTOS) {
            throw new CustomException(ErrorCode.EVENT_PHOTO_LIMIT_EXCEEDED);
        }

        EventPhoto photo = new EventPhoto();
        photo.setUser(loginUser);
        photo.setImageUrl(imageUrl);
        photo.setDescription(description);

        // 근무 완료 건 연결 (선택)
        if (applicationId != null) {
            Application application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

            if (!application.getUser().getId().equals(loginUser.getId())) {
                throw new CustomException(ErrorCode.NOT_MY_APPLICATION);
            }

            if (application.getStatus() != ApplicationStatus.COMPLETED) {
                throw new CustomException(ErrorCode.WORK_NOT_COMPLETED);
            }

            photo.setApplication(application);

            // 카테고리 자동 설정 (공고 카테고리)
            if (application.getJobPost().getCategory() != null) {
                photo.setCategory(application.getJobPost().getCategory());
            }
        }

        // 카테고리 직접 지정 (applicationId 없을 때)
        if (categoryId != null && photo.getCategory() == null) {
            JobCategory category = jobCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
            photo.setCategory(category);
        }

        return new EventPhotoResponseDto(eventPhotoRepository.save(photo));
    }

    @Transactional
    public void deleteEventPhoto(Long photoId, User loginUser) {
        EventPhoto photo = eventPhotoRepository.findById(photoId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_PHOTO_NOT_FOUND));

        if (!photo.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        eventPhotoRepository.deleteById(photoId);
    }

    @Transactional(readOnly = true)
    public List<EventPhotoResponseDto> getMyEventPhotos(User loginUser) {
        return eventPhotoRepository.findByUserOrderByCreatedAtDesc(loginUser)
                .stream()
                .map(EventPhotoResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventPhotoResponseDto> getUserEventPhotos(Long userId,
                                                           User loginUser) {
        // 본인 또는 기업/매니저만 조회 가능
        if (!loginUser.getId().equals(userId)
                && loginUser.getRole() != Role.COMPANY
                && loginUser.getRole() != Role.MANAGER
                && loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        User target = new User();
        target.setId(userId);

        return eventPhotoRepository.findByUserOrderByCreatedAtDesc(target)
                .stream()
                .map(EventPhotoResponseDto::new)
                .collect(Collectors.toList());
    }
}