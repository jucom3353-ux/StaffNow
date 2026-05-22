package com.example.demo.service;

import com.example.demo.dto.WorkerSearchResponseDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkerSearchService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<WorkerSearchResponseDto> searchWorkers(
            String name, double minRating, int maxNoShow,
            String activityRegion, String mbti, Boolean availableAlways,
            String sort, int page, int size, Long blockerId) {

        Pageable pageable = PageRequest.of(page, size);

        if (sort != null && sort.equals("noShow")) {
            Pageable noShowPageable = PageRequest.of(page, size,
                    Sort.by(Sort.Direction.ASC, "noShowCount"));
            return userRepository.findWorkers(Role.INDIVIDUAL, name, minRating, maxNoShow,
                    activityRegion, mbti, availableAlways, blockerId, noShowPageable)
                    .map(WorkerSearchResponseDto::new);
        }

        return userRepository.findWorkersWithTopRecommended(Role.INDIVIDUAL, name, minRating,
                maxNoShow, activityRegion, mbti, availableAlways, blockerId, pageable)
                .map(WorkerSearchResponseDto::new);
    }

    @Transactional(readOnly = true)
    public WorkerSearchResponseDto getWorker(Long workerId) {
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKER_NOT_FOUND));

        if (worker.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        return new WorkerSearchResponseDto(worker);
    }
}