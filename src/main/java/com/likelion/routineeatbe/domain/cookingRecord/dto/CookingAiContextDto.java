package com.likelion.routineeatbe.domain.cookingRecord.dto;

import java.util.List;

public record CookingAiContextDto(
        Long userId,
        Long cookingRecordId,
        Long cookingSessionId,
        Long recipeId,
        Integer servings,
        Integer currentCookingStepLevel,
        Integer cookingStepCount,
        String menuName,
        String menuType,
        String difficultyLevel,
        Integer timeRequired,
        Double calory,
        List<CookingStepContext> cookingSteps,
        List<FoodIngredientContext> foodIngredients
) {

    public record CookingStepContext(
            Long level,
            String title,
            String content,
            String subContent
    ) {
    }

    public record FoodIngredientContext(
            String name,
            Double primaryAmount,
            String primaryUnit,
            Double secondaryAmount,
            String secondaryUnit
    ) {
    }
}
