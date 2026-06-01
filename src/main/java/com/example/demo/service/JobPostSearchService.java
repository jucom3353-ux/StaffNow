package com.example.demo.service;

import com.example.demo.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobPostSearchService {

    private final JobPostRepository jobPostRepository;

    @Transactional(readOnly = true)
    public Map<String, List<String>> autocomplete(String keyword) {
        if (keyword == null || keyword.isBlank() || keyword.length() < 1) {
            return Map.of(
                    "titles", List.of(),
                    "locations", List.of(),
                    "companyNames", List.of()
            );
        }

        PageRequest pageable = PageRequest.of(0, 5);

        List<String> titles = jobPostRepository
                .findTitleSuggestions(keyword, pageable);
        List<String> locations = jobPostRepository
                .findLocationSuggestions(keyword, pageable);
        List<String> companyNames = jobPostRepository
                .findCompanyNameSuggestions(keyword, pageable);

        Map<String, List<String>> result = new HashMap<>();
        result.put("titles", titles);
        result.put("locations", locations);
        result.put("companyNames", companyNames);
        return result;
    }
}