package com.example.demo.service;

import com.example.demo.dto.JobPostCalendarResponseDto;
import com.example.demo.entity.JobPost;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostCalendarService {

    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;

    // 월별 공고 캘린더 조회
    @Transactional(readOnly = true)
    public JobPostCalendarResponseDto getCalendar(int year, int month, String region) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);

        List<JobPost> jobPosts = (region != null && !region.isBlank())
                ? jobPostRepository.findByWorkStartDateBetweenAndRegion(
                        startDate, endDate, region)
                : jobPostRepository.findByWorkStartDateBetween(startDate, endDate);

        // 날짜별 그룹핑
        Map<String, List<JobPostCalendarResponseDto.JobPostSummaryDto>> calendar =
                jobPosts.stream()
                        .collect(Collectors.groupingBy(
                                j -> j.getWorkStartDate().toString(),
                                Collectors.mapping(
                                        JobPostCalendarResponseDto.JobPostSummaryDto::new,
                                        Collectors.toList()
                                )
                        ));

        return new JobPostCalendarResponseDto(year, month, region, calendar);
    }

    // 이번 달 공고 있는 지역 목록
    @Transactional(readOnly = true)
    public List<String> getRegions(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);
        return jobPostRepository.findDistinctRegionsByMonth(startDate, endDate);
    }
}