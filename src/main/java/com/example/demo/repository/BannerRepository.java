package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Banner;
import com.example.demo.entity.BannerPosition;


public interface BannerRepository extends JpaRepository<Banner, Long> {

    @Query("SELECT b FROM Banner b WHERE b.isActive = true " +
           "AND (b.startAt IS NULL OR b.startAt <= :now) " +
           "AND (b.endAt IS NULL OR b.endAt >= :now) " +
           "ORDER BY b.orderIndex ASC")
    List<Banner> findActiveBanners(@Param("now") LocalDateTime now);

    @Query("SELECT b FROM Banner b WHERE b.isActive = true " +
           "AND b.position = :position " +
           "AND (b.startAt IS NULL OR b.startAt <= :now) " +
           "AND (b.endAt IS NULL OR b.endAt >= :now) " +
           "ORDER BY b.orderIndex ASC")
    List<Banner> findActiveBannersByPosition(
            @Param("position") BannerPosition position,
            @Param("now") LocalDateTime now);
}