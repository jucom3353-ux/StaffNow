package com.example.demo.service;

import com.example.demo.dto.PopularKeywordDto;
import com.example.demo.dto.SearchSuggestionsResponse;
import com.example.demo.entity.SearchKeyword;
import com.example.demo.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchKeywordService {

    private final SearchKeywordRepository searchKeywordRepository;

    @Transactional
    public void save(String userId, String keyword) {
        if (keyword == null || keyword.isBlank()) return;
        searchKeywordRepository.save(SearchKeyword.builder()
                .userId(userId)
                .keyword(keyword.trim())
                .build());
    }

    @Transactional(readOnly = true)
    public SearchSuggestionsResponse getSuggestions(String userId) {
        List<String> recent = searchKeywordRepository.findRecentByUserId(userId);
        List<PopularKeywordDto> popular = searchKeywordRepository.findPopularKeywords(
                LocalDateTime.now().minusDays(7)
        );
        return new SearchSuggestionsResponse(recent, popular);
    }
}