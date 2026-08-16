package com.likelion.routineeatbe.domain.recipe.dto.gemini;

import java.util.List;

public record RecipeReRecommendGeminiResponse(List<RecipeRecommendation> recipes) {
    public record RecipeRecommendation(Long recipeId, String reason) {}
}