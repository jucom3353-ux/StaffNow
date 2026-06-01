package com.example.demo.service;

import com.example.demo.dto.PortfolioRequestDto;
import com.example.demo.dto.PortfolioResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.JobCategoryRepository;
import com.example.demo.repository.PortfolioImageRepository;
import com.example.demo.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioImageRepository portfolioImageRepository;
    private final JobCategoryRepository jobCategoryRepository;

    private static final int MAX_IMAGES = 10;

    // 포트폴리오 등록
    @Transactional
    public PortfolioResponseDto createPortfolio(
            PortfolioRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        // 이미지 개수 검증
        if (requestDto.getImageUrls() != null
                && requestDto.getImageUrls().size() > MAX_IMAGES) {
            throw new CustomException(ErrorCode.PORTFOLIO_IMAGE_LIMIT);
        }

        Portfolio portfolio = new Portfolio();
        portfolio.setUser(loginUser);
        portfolio.setTitle(requestDto.getTitle());
        portfolio.setDescription(requestDto.getDescription());

        if (requestDto.getCategoryId() != null) {
            JobCategory category = jobCategoryRepository
                    .findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
            portfolio.setCategory(category);
        }

        List<String> imageUrls = requestDto.getImageUrls();
        portfolio.setImageCount(imageUrls != null ? imageUrls.size() : 0);
        Portfolio saved = portfolioRepository.save(portfolio);

        // 이미지 저장
        List<PortfolioImage> images = saveImages(saved, imageUrls);

        return new PortfolioResponseDto(saved, images);
    }

    // 포트폴리오 수정
    @Transactional
    public PortfolioResponseDto updatePortfolio(
            Long portfolioId, PortfolioRequestDto requestDto, User loginUser) {

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));

        if (!portfolio.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (requestDto.getImageUrls() != null
                && requestDto.getImageUrls().size() > MAX_IMAGES) {
            throw new CustomException(ErrorCode.PORTFOLIO_IMAGE_LIMIT);
        }

        portfolio.setTitle(requestDto.getTitle());
        portfolio.setDescription(requestDto.getDescription());

        if (requestDto.getCategoryId() != null) {
            JobCategory category = jobCategoryRepository
                    .findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
            portfolio.setCategory(category);
        }

        // 기존 이미지 삭제 후 재등록
        portfolioImageRepository.deleteByPortfolio(portfolio);
        List<String> imageUrls = requestDto.getImageUrls();
        portfolio.setImageCount(imageUrls != null ? imageUrls.size() : 0);
        Portfolio saved = portfolioRepository.save(portfolio);
        List<PortfolioImage> images = saveImages(saved, imageUrls);

        return new PortfolioResponseDto(saved, images);
    }

    // 내 포트폴리오 목록 조회
    @Transactional(readOnly = true)
    public List<PortfolioResponseDto> getMyPortfolios(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }
        return portfolioRepository.findByUserOrderByCreatedAtDesc(loginUser)
                .stream()
                .map(p -> new PortfolioResponseDto(p,
                        portfolioImageRepository.findByPortfolioOrderBySortOrderAsc(p)))
                .collect(Collectors.toList());
    }

    // 특정 유저 포트폴리오 조회 (기업용)
    @Transactional(readOnly = true)
    public List<PortfolioResponseDto> getUserPortfolios(Long userId) {
        List<Portfolio> portfolios = portfolioRepository
                .findByUserOrderByCreatedAtDesc(
                        new com.example.demo.entity.User() {{ setId(userId); }});
        return portfolios.stream()
                .map(p -> new PortfolioResponseDto(p,
                        portfolioImageRepository.findByPortfolioOrderBySortOrderAsc(p)))
                .collect(Collectors.toList());
    }

    // 포트폴리오 단건 조회
    @Transactional(readOnly = true)
    public PortfolioResponseDto getPortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));
        List<PortfolioImage> images = portfolioImageRepository
                .findByPortfolioOrderBySortOrderAsc(portfolio);
        return new PortfolioResponseDto(portfolio, images);
    }

    // 포트폴리오 삭제
    @Transactional
    public void deletePortfolio(Long portfolioId, User loginUser) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));

        if (!portfolio.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        portfolioImageRepository.deleteByPortfolio(portfolio);
        portfolioRepository.delete(portfolio);
    }

    // 이미지 저장 헬퍼
    private List<PortfolioImage> saveImages(Portfolio portfolio, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return List.of();

        List<PortfolioImage> images = new java.util.ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            PortfolioImage image = new PortfolioImage();
            image.setPortfolio(portfolio);
            image.setImageUrl(imageUrls.get(i));
            image.setSortOrder(i);
            images.add(image);
        }
        return portfolioImageRepository.saveAll(images);
    }
}