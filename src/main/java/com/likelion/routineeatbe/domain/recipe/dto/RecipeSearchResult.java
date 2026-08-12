package com.likelion.routineeatbe.domain.recipe.dto;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;

public record RecipeSearchResult(
        Long recipeId,
        Long menuId,
        String menuName,
        String thumbnailUrl,
        Double calory,
        Integer timeRequired,
        DifficultyLevel difficultyLevel,
        MenuType category,
        Long cookingCount,
        Long matchedIngredientCount,
        Long requiredIngredientCount,
        Long requiredIngredientCost
) {

    public RecipeSearchResult withRequiredIngredientCost(Long cost) {
        return new RecipeSearchResult(
                recipeId,
                menuId,
                menuName,
                thumbnailUrl,
                calory,
                timeRequired,
                difficultyLevel,
                category,
                cookingCount,
                matchedIngredientCount,
                requiredIngredientCount,
                cost
        );
    }
}
