package com.likelion.routineeatbe.domain.recipeFoodIngredient.repository;

import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeFoodIngredientRepository extends JpaRepository<RecipeFoodIngredient, Long> {
}
