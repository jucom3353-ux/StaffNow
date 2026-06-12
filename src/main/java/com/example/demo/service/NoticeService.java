package com.example.demo.service;

import com.example.demo.dto.NoticeRequestDto;
import com.example.demo.dto.NoticeResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
import com.example.demo.util.AuthorizationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<NoticeResponseDto> getNotices(NoticeCategory category, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return noticeRepository.findByTitleContainingAndIsActiveTrue(keyword)
                    .stream().map(NoticeResponseDto::new).collect(Collectors.toList());
        }
        if (category != null) {
            return noticeRepository.findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(category)
                    .stream().map(NoticeResponseDto::new).collect(Collectors.toList());
        }
        return noticeRepository.findByIsActiveTrueOrderByIsPinnedDescCreatedAtDesc()
                .stream().map(NoticeResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NoticeResponseDto> getAdminNotices() {
        return noticeRepository
                .findByNoticeTypeAndIsActiveTrueOrderByIsPinnedDescCreatedAtDesc(NoticeType.ADMIN_NOTICE)
                .stream().map(NoticeResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NoticeResponseDto> getNoticesByJobPost(Long jobPostId) {
        return noticeRepository
                .findByJobPostIdAndIsActiveTrueOrderByCreatedAtDesc(jobPostId)
                .stream().map(NoticeResponseDto::new).collect(Collectors.toList());
    }

    @Transactional
    public NoticeResponseDto getNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
        notice.setViewCount(notice.getViewCount() + 1);
        noticeRepository.save(notice);
        return new NoticeResponseDto(notice);
    }

    @Transactional
    public NoticeResponseDto createNotice(NoticeRequestDto requestDto, User loginUser) {
        Role role = loginUser.getRole();

        if (role != Role.ADMIN && role != Role.COMPANY && role != Role.MANAGER) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        if (requestDto.getNoticeType() == NoticeType.ADMIN_NOTICE && role != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        if (requestDto.getNoticeType() == NoticeType.COMPANY_NOTICE) {
            if (role != Role.COMPANY && role != Role.MANAGER) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
            if (requestDto.getJobPostId() == null) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
        }

        Notice notice = new Notice();
        notice.setTitle(requestDto.getTitle());
        notice.setContent(requestDto.getContent());
        notice.setCategory(requestDto.getCategory());
        notice.setNoticeType(requestDto.getNoticeType());
        notice.setTargetType(requestDto.getTargetType() != null
                ? requestDto.getTargetType() : NoticeTarget.ALL);
        notice.setAuthor(loginUser);
        notice.setPinned(requestDto.isPinned());
        notice.setActive(requestDto.isActive());

        if (requestDto.getJobPostId() != null) {
            JobPost jobPost = jobPostRepository.findById(requestDto.getJobPostId())
                    .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));
            notice.setJobPost(jobPost);
        }

        Notice saved = noticeRepository.save(notice);
        sendNoticeNotification(saved);
        return new NoticeResponseDto(saved);
    }

    @Transactional
    public NoticeResponseDto updateNotice(Long id, NoticeRequestDto requestDto, User loginUser) {
        Role role = loginUser.getRole();
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));

        if (notice.getNoticeType() == NoticeType.ADMIN_NOTICE && role != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        if (notice.getNoticeType() == NoticeType.COMPANY_NOTICE) {
            boolean isAuthor = notice.getAuthor() != null &&
                    notice.getAuthor().getId().equals(loginUser.getId());
            if (!isAuthor && role != Role.ADMIN) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
        }

        notice.setTitle(requestDto.getTitle());
        notice.setContent(requestDto.getContent());
        notice.setCategory(requestDto.getCategory());
        notice.setTargetType(requestDto.getTargetType() != null
                ? requestDto.getTargetType() : notice.getTargetType());
        notice.setPinned(requestDto.isPinned());
        notice.setActive(requestDto.isActive());

        return new NoticeResponseDto(noticeRepository.save(notice));
    }

    @Transactional
    public void deleteNotice(Long id, User loginUser) {
        Role role = loginUser.getRole();
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));

        if (notice.getNoticeType() == NoticeType.ADMIN_NOTICE && role != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        if (notice.getNoticeType() == NoticeType.COMPANY_NOTICE) {
            boolean isAuthor = notice.getAuthor() != null &&
                    notice.getAuthor().getId().equals(loginUser.getId());
            if (!isAuthor && role != Role.ADMIN) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
        }

        noticeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<NoticeResponseDto> getCompanyNotices(User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);
        User companyUser = AuthorizationUtil.getCompanyUser(loginUser);
        return noticeRepository.findCompanyNotices(companyUser.getId())
                .stream().map(NoticeResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NoticeResponseDto> getWorkerNotices(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL)
            throw new CustomException(ErrorCode.WORKER_ONLY);

        List<Application> apps = applicationRepository.findByUser(loginUser);

        List<Long> companyIds = apps.stream()
                .filter(a -> a.getJobPost() != null && a.getJobPost().getUser() != null)
                .map(a -> a.getJobPost().getUser().getId())
                .distinct().collect(Collectors.toList());

        List<Long> jobPostIds = apps.stream()
                .filter(a -> a.getJobPost() != null)
                .map(a -> a.getJobPost().getId())
                .distinct().collect(Collectors.toList());

        if (companyIds.isEmpty() && jobPostIds.isEmpty()) {
            return noticeRepository
                    .findByNoticeTypeAndIsActiveTrueOrderByIsPinnedDescCreatedAtDesc(NoticeType.ADMIN_NOTICE)
                    .stream().map(NoticeResponseDto::new).collect(Collectors.toList());
        }

        if (companyIds.isEmpty()) companyIds = List.of(-1L);
        if (jobPostIds.isEmpty()) jobPostIds = List.of(-1L);

        return noticeRepository.findWorkerNotices(companyIds, jobPostIds, !jobPostIds.contains(-1L))
                .stream().map(NoticeResponseDto::new).collect(Collectors.toList());
    }

    @Transactional
    public void rejectNotice(Long id, String reason, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN)
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
        notice.setActive(false);
        noticeRepository.save(notice);
    }

    private void sendNoticeNotification(Notice notice) {
        String message = "[공지] " + notice.getTitle();

        if (notice.getNoticeType() == NoticeType.ADMIN_NOTICE) {
            return;
        }

        if (notice.getNoticeType() == NoticeType.COMPANY_NOTICE
                && notice.getJobPost() != null) {

            NoticeTarget target = notice.getTargetType();
            Long jobPostId = notice.getJobPost().getId();

            List<Application> applications;

            if (target == NoticeTarget.WORKER) {
                applications = applicationRepository
                        .findByJobPostIdAndStatus(jobPostId, ApplicationStatus.APPROVED);
            } else {
                applications = applicationRepository.findByJobPostId(jobPostId);
            }

            for (Application application : applications) {
                notificationService.send(
                        application.getUser(),
                        NotificationType.NOTICE_RECEIVED,
                        message,
                        notice.getId()
                );
            }
        }
    }
}