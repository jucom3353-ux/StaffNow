package com.example.demo.service;

import com.example.demo.dto.SearchSuggestionResponseDto;
import com.example.demo.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchSuggestionService {

    private final JobPostRepository jobPostRepository;

    private static final int MAX_SUGGESTIONS = 5;

    @Transactional(readOnly = true)
    public SearchSuggestionResponseDto getSuggestions(String keyword) {

        if (keyword == null || keyword.isBlank() || keyword.length() < 1) {
            return new SearchSuggestionResponseDto(
                    List.of(), List.of(), List.of());
        }

        PageRequest pageable = PageRequest.of(0, MAX_SUGGESTIONS);

        List<String> titles = jobPostRepository
                .findTitleSuggestions(keyword, pageable);
        List<String> locations = jobPostRepository
                .findLocationSuggestions(keyword, pageable);
        List<String> companyNames = jobPostRepository
                .findCompanyNameSuggestions(keyword, pageable);

        return new SearchSuggestionResponseDto(titles, locations, companyNames);
    }
}