package com.likelion.routineeatbe.domain.recipe.dto.gemini;

import com.likelion.routineeatbe.global.dto.gemini.GeminiFunctionDeclaration;
import java.util.List;
import java.util.Map;

public record RecipeRecommendationFunctionDeclaration(
        String type, String name, String description, Object parameters
) implements GeminiFunctionDeclaration {
    public static final String FUNCTION_NAME = "recommend_single_recipe";

    public static RecipeRecommendationFunctionDeclaration create() {
        return new RecipeRecommendationFunctionDeclaration(
                "function",
                FUNCTION_NAME,
                "사용자의 요리 실력, 보유 식재료, 요리 기록을 종합하여 가장 적합한 단 하나의 레시피를 추천합니다.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "recipeId", Map.of("type", "integer"),
                                "reason", Map.of("type", "string")
                        ),
                        "required", List.of("recipeId", "reason")
                )
        );
    }
}