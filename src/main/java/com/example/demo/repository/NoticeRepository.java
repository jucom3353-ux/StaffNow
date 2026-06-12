package com.example.demo.repository;

import com.example.demo.entity.Notice;
import com.example.demo.entity.NoticeCategory;
import com.example.demo.entity.NoticeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByIsActiveTrueOrderByIsPinnedDescCreatedAtDesc();
    List<Notice> findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(NoticeCategory category);
    List<Notice> findByTitleContainingAndIsActiveTrue(String keyword);
    List<Notice> findByNoticeTypeAndIsActiveTrueOrderByIsPinnedDescCreatedAtDesc(NoticeType noticeType);
    List<Notice> findByJobPostIdAndIsActiveTrueOrderByCreatedAtDesc(Long jobPostId);

    @Query("SELECT n FROM Notice n LEFT JOIN FETCH n.author LEFT JOIN FETCH n.jobPost " +
           "WHERE n.isActive = true AND " +
           "(n.noticeType = com.example.demo.entity.NoticeType.ADMIN_NOTICE OR n.author.id = :companyId) " +
           "ORDER BY n.isPinned DESC, n.createdAt DESC")
    List<Notice> findCompanyNotices(@Param("companyId") Long companyId);

    @Query("SELECT n FROM Notice n LEFT JOIN FETCH n.author LEFT JOIN FETCH n.jobPost " +
           "WHERE n.isActive = true AND " +
           "(n.noticeType = com.example.demo.entity.NoticeType.ADMIN_NOTICE OR " +
           "n.author.id IN :companyIds OR " +
           "(:hasJobPosts = true AND n.jobPost.id IN :jobPostIds)) " +
           "ORDER BY n.isPinned DESC, n.createdAt DESC")
    List<Notice> findWorkerNotices(
        @Param("companyIds") List<Long> companyIds,
        @Param("jobPostIds") List<Long> jobPostIds,
        @Param("hasJobPosts") boolean hasJobPosts);
}