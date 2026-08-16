package com.likelion.routineeatbe.domain.recipe.dto.gemini;

import com.likelion.routineeatbe.global.dto.gemini.GeminiFunctionDeclaration;
import java.util.List;
import java.util.Map;

public record RecipeReRecommendFunctionDeclaration(
        String type, String name, String description, Object parameters
) implements GeminiFunctionDeclaration {
    public static final String FUNCTION_NAME = "recommend_three_recipes";

    public static RecipeReRecommendFunctionDeclaration create() {
        Map<String, Object> recipe = Map.of(
                "type", "object",
                "properties", Map.of(
                        "recipeId", Map.of("type", "integer"),
                        "reason", Map.of("type", "string")
                ),
                "required", List.of("recipeId", "reason")
        );
        return new RecipeReRecommendFunctionDeclaration(
                "function",
                FUNCTION_NAME,
                "사용자 요구사항에 맞춘 3가지 레시피와 각각의 추천 이유를 반환합니다.",
                Map.of(
                        "type", "object",
                        "properties", Map.of("recipes", Map.of("type", "array", "items", recipe, "minItems", 3, "maxItems", 3)),
                        "required", List.of("recipes")
                )
        );
    }
}