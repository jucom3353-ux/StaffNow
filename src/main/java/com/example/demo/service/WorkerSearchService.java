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
        Sort sorting = switch (sort) {
            case "temperature" -> Sort.by(Sort.Direction.DESC, "temperature");
            case "noShow" -> Sort.by(Sort.Direction.ASC, "noShowCount");
            default -> Sort.by(Sort.Direction.DESC, "temperature");
        };

        Pageable pageable = PageRequest.of(page, size, sorting);

        return userRepository.findWorkers(
                Role.INDIVIDUAL,
                name,
                minRating,
                maxNoShow,
                activityRegion,
                mbti,
                blockerId,
                pageable
        ).map(WorkerSearchResponseDto::new);
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