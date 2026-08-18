package com.likelion.routineeatbe.domain.cookingRecord.dto;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import java.time.LocalDateTime;

public record CookingRecordSearchResult(
        Long recipeId,
        String menuName,
        String thumbnailUrl,
        Boolean favoriteRecipe,
        LocalDateTime createdAt,
        DifficultyLevel userDifficultyLevel,
        Long usedFoodIngredientCount
) {
}
