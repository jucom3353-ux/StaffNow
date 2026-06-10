package com.example.demo.service;

import com.example.demo.dto.PopupRequestDto;
import com.example.demo.dto.PopupResponseDto;
import com.example.demo.entity.Popup;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.PopupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PopupService {

    private final PopupRepository popupRepository;

    // 활성 팝업 조회
    @Transactional(readOnly = true)
    public List<PopupResponseDto> getActivePopups() {
        return popupRepository.findActivePopups(LocalDateTime.now())
                .stream().map(PopupResponseDto::new).collect(Collectors.toList());
    }

    // 전체 팝업 조회 (관리자)
    @Transactional(readOnly = true)
    public List<PopupResponseDto> getAllPopups(User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        return popupRepository.findAll().stream()
                .map(PopupResponseDto::new).collect(Collectors.toList());
    }

    // 팝업 등록 (관리자)
    @Transactional
    public PopupResponseDto createPopup(PopupRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Popup popup = new Popup();
        popup.setTitle(requestDto.getTitle());
        popup.setImageUrl(requestDto.getImageUrl());
        popup.setLinkUrl(requestDto.getLinkUrl());
        popup.setActive(requestDto.isActive());
        popup.setStartAt(requestDto.getStartAt());
        popup.setEndAt(requestDto.getEndAt());

        return new PopupResponseDto(popupRepository.save(popup));
    }

    // 팝업 수정 (관리자)
    @Transactional
    public PopupResponseDto updatePopup(
            Long id, PopupRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Popup popup = popupRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POPUP_NOT_FOUND));

        popup.setTitle(requestDto.getTitle());
        popup.setImageUrl(requestDto.getImageUrl());
        popup.setLinkUrl(requestDto.getLinkUrl());
        popup.setActive(requestDto.isActive());
        popup.setStartAt(requestDto.getStartAt());
        popup.setEndAt(requestDto.getEndAt());

        return new PopupResponseDto(popupRepository.save(popup));
    }

    // 팝업 삭제 (관리자)
    @Transactional
    public void deletePopup(Long id, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        popupRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POPUP_NOT_FOUND));

        popupRepository.deleteById(id);
    }
}