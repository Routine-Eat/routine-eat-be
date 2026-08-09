package com.likelion.routineeatbe.domain.foodIngredient.repository;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FoodIngredientRepository extends JpaRepository<FoodIngredient,Long> {

    List<FoodIngredient> findByNameContaining(String name);

}
