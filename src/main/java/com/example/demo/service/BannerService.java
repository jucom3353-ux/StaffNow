package com.example.demo.service;

import com.example.demo.dto.BannerRequestDto;
import com.example.demo.dto.BannerResponseDto;
import com.example.demo.entity.Banner;
import com.example.demo.entity.BannerPosition;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    // 활성 배너 조회 (전체)
    @Transactional(readOnly = true)
    public List<BannerResponseDto> getActiveBanners(BannerPosition position) {
        LocalDateTime now = LocalDateTime.now();
        if (position != null) {
            return bannerRepository.findActiveBannersByPosition(position, now)
                    .stream().map(BannerResponseDto::new).collect(Collectors.toList());
        }
        return bannerRepository.findActiveBanners(now)
                .stream().map(BannerResponseDto::new).collect(Collectors.toList());
    }

    // 전체 배너 조회 (관리자)
    @Transactional(readOnly = true)
    public List<BannerResponseDto> getAllBanners(User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        return bannerRepository.findAll().stream()
                .map(BannerResponseDto::new).collect(Collectors.toList());
    }

    // 배너 등록 (관리자)
    @Transactional
    public BannerResponseDto createBanner(BannerRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Banner banner = new Banner();
        banner.setTitle(requestDto.getTitle());
        banner.setImageUrl(requestDto.getImageUrl());
        banner.setLinkUrl(requestDto.getLinkUrl());
        banner.setPosition(requestDto.getPosition());
        banner.setOrderIndex(requestDto.getOrderIndex());
        banner.setActive(requestDto.isActive());
        banner.setStartAt(requestDto.getStartAt());
        banner.setEndAt(requestDto.getEndAt());

        return new BannerResponseDto(bannerRepository.save(banner));
    }

    // 배너 수정 (관리자)
    @Transactional
    public BannerResponseDto updateBanner(
            Long id, BannerRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BANNER_NOT_FOUND));

        banner.setTitle(requestDto.getTitle());
        banner.setImageUrl(requestDto.getImageUrl());
        banner.setLinkUrl(requestDto.getLinkUrl());
        banner.setPosition(requestDto.getPosition());
        banner.setOrderIndex(requestDto.getOrderIndex());
        banner.setActive(requestDto.isActive());
        banner.setStartAt(requestDto.getStartAt());
        banner.setEndAt(requestDto.getEndAt());

        return new BannerResponseDto(bannerRepository.save(banner));
    }

    // 배너 삭제 (관리자)
    @Transactional
    public void deleteBanner(Long id, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        bannerRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BANNER_NOT_FOUND));

        bannerRepository.deleteById(id);
    }

    // 배너 클릭 수 증가
    @Transactional
    public void clickBanner(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BANNER_NOT_FOUND));
        banner.setClickCount(banner.getClickCount() + 1);
        bannerRepository.save(banner);
    }
}