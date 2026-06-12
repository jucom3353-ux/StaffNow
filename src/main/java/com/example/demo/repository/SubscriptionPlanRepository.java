// SubscriptionPlanRepository.java
package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.PlanType;
import com.example.demo.entity.SubscriptionPlan;


public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    Optional<SubscriptionPlan> findByPlanType(PlanType planType);
}