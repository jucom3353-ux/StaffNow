package com.example.demo.service;

import com.example.demo.dto.WorkerSearchResponseDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkerSearchService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<WorkerSearchResponseDto> searchWorkers(
            String name,
            double minRating,
            int maxNoShow,
            String activityRegion,
            String mbti,
            String sort,
            int page,
            int size,
            Long blockerId
    ) {
        // 상위 추천 정렬 (사진 5장 이상 우선)
        // JPQL ORDER BY 고정이라 Pageable sort 무시하고 별도 쿼리 사용
        Pageable pageable = PageRequest.of(page, size);

        if (sort != null && sort.equals("noShow")) {
            // 노쇼 적은 순 정렬 시에는 기존 쿼리 사용
            Pageable noShowPageable = PageRequest.of(page, size,
                    Sort.by(Sort.Direction.ASC, "noShowCount"));
            return userRepository.findWorkers(
                    Role.INDIVIDUAL, name, minRating, maxNoShow,
                    activityRegion, mbti, blockerId, noShowPageable)
                    .map(WorkerSearchResponseDto::new);
        }

        // 기본: 상위 추천(사진 5장↑) 우선 → 온도 높은 순
        return userRepository.findWorkersWithTopRecommended(
                Role.INDIVIDUAL, name, minRating, maxNoShow,
                activityRegion, mbti, blockerId, pageable)
                .map(WorkerSearchResponseDto::new);
    }

    @Transactional(readOnly = true)
    public WorkerSearchResponseDto getWorker(Long workerId) {
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("근로자 없음"));

        if (worker.getRole() != Role.INDIVIDUAL) {
            throw new RuntimeException("근로자만 조회 가능합니다.");
        }

        return new WorkerSearchResponseDto(worker);
    }
}