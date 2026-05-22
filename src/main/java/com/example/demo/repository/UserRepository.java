package com.example.demo.repository;

import com.example.demo.entity.BusinessLicenseStatus;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);

    // ✅ 추가
    List<User> findByBusinessLicenseStatus(BusinessLicenseStatus status);

    @Query("SELECT u FROM User u WHERE u.role = :role " +
           "AND (:name IS NULL OR u.name LIKE %:name%) " +
           "AND u.temperature >= :minRating " +
           "AND u.noShowCount <= :maxNoShow " +
           "AND (:activityRegion IS NULL OR u.activityRegion LIKE %:activityRegion%) " +
           "AND (:mbti IS NULL OR u.mbti = :mbti) " +
           "AND (:availableAlways IS NULL OR u.availableAlways = :availableAlways) " +
           "AND u.suspended = false " +
           "AND u.id NOT IN " +
           "(SELECT b.blocked.id FROM Block b WHERE b.blocker.id = :blockerId)")
    Page<User> findWorkers(
            @Param("role") Role role,
            @Param("name") String name,
            @Param("minRating") double minRating,
            @Param("maxNoShow") int maxNoShow,
            @Param("activityRegion") String activityRegion,
            @Param("mbti") String mbti,
            @Param("availableAlways") Boolean availableAlways,
            @Param("blockerId") Long blockerId,
            Pageable pageable
    );

    @Query("SELECT u FROM User u WHERE u.role = :role " +
           "AND (:name IS NULL OR u.name LIKE %:name%) " +
           "AND u.temperature >= :minRating " +
           "AND u.noShowCount <= :maxNoShow " +
           "AND (:activityRegion IS NULL OR u.activityRegion LIKE %:activityRegion%) " +
           "AND (:mbti IS NULL OR u.mbti = :mbti) " +
           "AND (:availableAlways IS NULL OR u.availableAlways = :availableAlways) " +
           "AND u.suspended = false " +
           "AND u.id NOT IN " +
           "(SELECT b.blocked.id FROM Block b WHERE b.blocker.id = :blockerId) " +
           "ORDER BY " +
           "CASE WHEN u.profileImageCount >= 5 THEN 0 ELSE 1 END ASC, " +
           "u.temperature DESC")
    Page<User> findWorkersWithTopRecommended(
            @Param("role") Role role,
            @Param("name") String name,
            @Param("minRating") double minRating,
            @Param("maxNoShow") int maxNoShow,
            @Param("activityRegion") String activityRegion,
            @Param("mbti") String mbti,
            @Param("availableAlways") Boolean availableAlways,
            @Param("blockerId") Long blockerId,
            Pageable pageable
    );
}