package com.example.demo.repository;

import com.example.demo.dto.PopularKeywordDto;
import com.example.demo.entity.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    @Query("SELECT DISTINCT s.keyword FROM SearchKeyword s " +
           "WHERE s.userId = :userId " +
           "ORDER BY s.createdAt DESC " +
           "LIMIT 5")
    List<String> findRecentByUserId(@Param("userId") String userId);

    @Query("SELECT new com.example.demo.dto.PopularKeywordDto(s.keyword, COUNT(s)) " +
           "FROM SearchKeyword s " +
           "WHERE s.createdAt >= :since " +
           "GROUP BY s.keyword " +
           "ORDER BY COUNT(s) DESC " +
           "LIMIT 10")
    List<PopularKeywordDto> findPopularKeywords(@Param("since") LocalDateTime since);
}