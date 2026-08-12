package com.likelion.routineeatbe.domain.recipe.dto;

import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import java.util.List;

public record RecipeWithSimilarRecipes(
        Recipe recipe,
        List<Recipe> similarRecipes
) {

    public static RecipeWithSimilarRecipes create(
            Recipe recipe,
            List<Recipe> similarRecipes
    ) {
        return new RecipeWithSimilarRecipes(recipe, List.copyOf(similarRecipes));
    }
}
