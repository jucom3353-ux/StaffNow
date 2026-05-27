package com.example.demo.repository;

import com.example.demo.entity.BusinessLicenseStatus;
import com.example.demo.entity.Gender;
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
    List<User> findByBusinessLicenseStatus(BusinessLicenseStatus status);
    long countByRole(Role role);
    long countBySuspendedTrue();
    List<User> findByCompanyId(Long companyId);

    @Query("SELECT u FROM User u WHERE u.role = :role " +
           "AND (:name IS NULL OR u.name LIKE %:name%) " +
           "AND u.temperature >= :minRating " +
           "AND u.noShowCount <= :maxNoShow " +
           "AND (:activityRegion IS NULL OR u.activityRegion LIKE %:activityRegion%) " +
           "AND (:mbti IS NULL OR u.mbti = :mbti) " +
           "AND (:availableAlways IS NULL OR u.availableAlways = :availableAlways) " +
           "AND (:gender IS NULL OR u.gender = :gender) " +
           "AND (:minAge IS NULL OR u.age >= :minAge) " +
           "AND (:maxAge IS NULL OR u.age <= :maxAge) " +
           "AND (:timeType IS NULL OR EXISTS (" +
           "    SELECT p FROM PreferredWorkTime p " +
           "    WHERE p.user = u AND p.timeType = :timeType)) " +
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
            @Param("gender") Gender gender,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("timeType") String timeType,
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
           "AND (:gender IS NULL OR u.gender = :gender) " +
           "AND (:minAge IS NULL OR u.age >= :minAge) " +
           "AND (:maxAge IS NULL OR u.age <= :maxAge) " +
           "AND (:timeType IS NULL OR EXISTS (" +
           "    SELECT p FROM PreferredWorkTime p " +
           "    WHERE p.user = u AND p.timeType = :timeType)) " +
           "AND u.suspended = false " +
           "AND u.id NOT IN " +
           "(SELECT b.blocked.id FROM Block b WHERE b.blocker.id = :blockerId) " +
           "ORDER BY " +
           "CASE WHEN u.id IN (" +
           "    SELECT pb.user.id FROM ProfileBoost pb " +
           "    WHERE pb.isActive = true " +
           "    AND pb.startAt <= CURRENT_TIMESTAMP " +
           "    AND pb.endAt >= CURRENT_TIMESTAMP" +
           ") THEN 0 ELSE 1 END ASC, " +
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
            @Param("gender") Gender gender,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("timeType") String timeType,
            @Param("blockerId") Long blockerId,
            Pageable pageable
    );

    // 추천 코드 관련
    Optional<User> findByReferralCode(String referralCode);
    boolean existsByReferralCode(String referralCode);
}