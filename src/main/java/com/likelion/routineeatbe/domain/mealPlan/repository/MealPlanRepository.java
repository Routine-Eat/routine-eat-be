package com.likelion.routineeatbe.domain.mealPlan.repository;

import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealPlanRepository extends JpaRepository<MealPlan,Long> {
}
