// SubscriptionPlanRepository.java
package com.example.demo.repository;

import com.example.demo.entity.PlanType;
import com.example.demo.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    Optional<SubscriptionPlan> findByPlanType(PlanType planType);
}