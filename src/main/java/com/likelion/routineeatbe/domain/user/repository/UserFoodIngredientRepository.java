package com.likelion.routineeatbe.domain.user.repository;

import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFoodIngredientRepository extends JpaRepository<UserFoodIngredient,Long> {
}
