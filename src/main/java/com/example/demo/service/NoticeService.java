package com.example.demo.service;

import com.example.demo.dto.NoticeRequestDto;
import com.example.demo.dto.NoticeResponseDto;
import com.example.demo.entity.Notice;
import com.example.demo.entity.NoticeCategory;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    // 전체 공지사항 조회
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

    // 공지사항 단건 조회
    @Transactional
    public NoticeResponseDto getNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
        notice.setViewCount(notice.getViewCount() + 1);
        noticeRepository.save(notice);
        return new NoticeResponseDto(notice);
    }

    // 공지사항 등록 (관리자)
    @Transactional
    public NoticeResponseDto createNotice(NoticeRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Notice notice = new Notice();
        notice.setTitle(requestDto.getTitle());
        notice.setContent(requestDto.getContent());
        notice.setCategory(requestDto.getCategory());
        notice.setPinned(requestDto.isPinned());
        notice.setActive(requestDto.isActive());

        return new NoticeResponseDto(noticeRepository.save(notice));
    }

    // 공지사항 수정 (관리자)
    @Transactional
    public NoticeResponseDto updateNotice(Long id, NoticeRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));

        notice.setTitle(requestDto.getTitle());
        notice.setContent(requestDto.getContent());
        notice.setCategory(requestDto.getCategory());
        notice.setPinned(requestDto.isPinned());
        notice.setActive(requestDto.isActive());

        return new NoticeResponseDto(noticeRepository.save(notice));
    }

    // 공지사항 삭제 (관리자)
    @Transactional
    public void deleteNotice(Long id, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));

        noticeRepository.deleteById(id);
    }
}