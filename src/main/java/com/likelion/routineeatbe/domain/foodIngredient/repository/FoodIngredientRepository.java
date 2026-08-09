package com.likelion.routineeatbe.domain.foodIngredient.repository;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodIngredientRepository extends JpaRepository<FoodIngredient,Long> {

}
