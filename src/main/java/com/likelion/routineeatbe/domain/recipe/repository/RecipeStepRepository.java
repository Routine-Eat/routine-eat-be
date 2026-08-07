package com.likelion.routineeatbe.domain.recipe.repository;

import com.likelion.routineeatbe.domain.recipe.entity.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {
}
