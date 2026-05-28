package com.example.demo.service;

import com.example.demo.dto.NoticeRequestDto;
import com.example.demo.dto.NoticeResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
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

    // 전체 공지사항 조회 (카테고리/키워드 필터)
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

    // ADMIN 공지만 조회
    @Transactional(readOnly = true)
    public List<NoticeResponseDto> getAdminNotices() {
        return noticeRepository
                .findByNoticeTypeAndIsActiveTrueOrderByIsPinnedDescCreatedAtDesc(NoticeType.ADMIN_NOTICE)
                .stream().map(NoticeResponseDto::new).collect(Collectors.toList());
    }

    // 공고별 기업 공지 조회
    @Transactional(readOnly = true)
    public List<NoticeResponseDto> getNoticesByJobPost(Long jobPostId) {
        return noticeRepository
                .findByJobPostIdAndIsActiveTrueOrderByCreatedAtDesc(jobPostId)
                .stream().map(NoticeResponseDto::new).collect(Collectors.toList());
    }

    // 공지사항 단건 조회 (조회수 증가)
    @Transactional
    public NoticeResponseDto getNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
        notice.setViewCount(notice.getViewCount() + 1);
        noticeRepository.save(notice);
        return new NoticeResponseDto(notice);
    }

    // 공지사항 등록
    @Transactional
    public NoticeResponseDto createNotice(NoticeRequestDto requestDto, User loginUser) {
        Role role = loginUser.getRole();

        // 권한 체크
        if (role != Role.ADMIN && role != Role.COMPANY && role != Role.MANAGER) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // ADMIN_NOTICE는 ADMIN만 작성 가능
        if (requestDto.getNoticeType() == NoticeType.ADMIN_NOTICE && role != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        // COMPANY_NOTICE는 COMPANY/MANAGER만, jobPostId 필수
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

        // 알림 발송
        sendNoticeNotification(saved);

        return new NoticeResponseDto(saved);
    }

    // 공지사항 수정
    @Transactional
    public NoticeResponseDto updateNotice(Long id, NoticeRequestDto requestDto, User loginUser) {
        Role role = loginUser.getRole();
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));

        // ADMIN_NOTICE 수정은 ADMIN만
        if (notice.getNoticeType() == NoticeType.ADMIN_NOTICE && role != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        // COMPANY_NOTICE 수정은 작성자 본인 또는 ADMIN만
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

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(Long id, User loginUser) {
        Role role = loginUser.getRole();
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));

        // ADMIN_NOTICE 삭제는 ADMIN만
        if (notice.getNoticeType() == NoticeType.ADMIN_NOTICE && role != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        // COMPANY_NOTICE 삭제는 작성자 본인 또는 ADMIN만
        if (notice.getNoticeType() == NoticeType.COMPANY_NOTICE) {
            boolean isAuthor = notice.getAuthor() != null &&
                    notice.getAuthor().getId().equals(loginUser.getId());
            if (!isAuthor && role != Role.ADMIN) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
        }

        noticeRepository.deleteById(id);
    }

    // 알림 발송 내부 메서드
    private void sendNoticeNotification(Notice notice) {
        String message = "[공지] " + notice.getTitle();

        // ADMIN_NOTICE → 모든 유저 대상 (앱에서 조회 방식으로 처리, 별도 발송 없음)
        if (notice.getNoticeType() == NoticeType.ADMIN_NOTICE) {
            return;
        }

        // COMPANY_NOTICE → 공고 지원자 기반 필터링
        if (notice.getNoticeType() == NoticeType.COMPANY_NOTICE
                && notice.getJobPost() != null) {

            NoticeTarget target = notice.getTargetType();
            Long jobPostId = notice.getJobPost().getId();

            List<Application> applications;

            if (target == NoticeTarget.WORKER) {
                // 근무 확정자만 (APPROVED 상태)
                applications = applicationRepository
                        .findByJobPostIdAndStatus(jobPostId, ApplicationStatus.APPROVED);
            } else {
                // INTERESTED or ALL → 전체 지원자
                applications = applicationRepository
                        .findByJobPostId(jobPostId);
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