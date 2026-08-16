package com.likelion.routineeatbe.domain.recipe.dto.gemini;

/** Gemini Function Calling 단일 추천 결과 역직렬화 DTO */
public record RecipeRecommendationGeminiResponse(Long recipeId, String reason) {
}