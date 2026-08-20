package com.likelion.routineeatbe.domain.mealPlan.repository;

import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlan;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MealPlanRepository extends JpaRepository<MealPlan,Long> {
    List<MealPlan> findByUser_Id(Long userId);

    List<MealPlan> findByUser_IdAndStatus(Long userId, MealPlanStatus status);

    boolean existsById(Long id);

    Optional<MealPlan> findByIdAndUser_Id(Long mealPlanId, Long userId);
}
