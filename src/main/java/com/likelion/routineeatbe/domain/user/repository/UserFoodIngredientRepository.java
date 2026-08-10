package com.likelion.routineeatbe.domain.user.repository;

import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFoodIngredientRepository extends JpaRepository<UserFoodIngredient,Long> {
    List<UserFoodIngredient> findByUserId(Long id);
    List<UserFoodIngredient> findByUserIdAndRelationType(Long userId, UserFoodIngredientType relationType);
}
