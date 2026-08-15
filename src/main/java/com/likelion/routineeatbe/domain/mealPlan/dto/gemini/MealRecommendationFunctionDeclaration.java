package com.likelion.routineeatbe.domain.mealPlan.dto.gemini;

import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanType;
import com.likelion.routineeatbe.global.dto.gemini.GeminiFunctionDeclaration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Gemini가 세 식단과 식단별 메뉴 세 개를 구조화된 JSON으로 반환하게 하는 Function Calling 스키마입니다. */
public record MealRecommendationFunctionDeclaration(
        String type, String name, String description, Object parameters
) implements GeminiFunctionDeclaration {
    public static final String FUNCTION_NAME = "recommend_three_meal_plans";

    public static MealRecommendationFunctionDeclaration create(boolean includeUseAll) {
        List<String> planTypes = Arrays.stream(MealPlanType.values())
                .map(Enum::name)
                .filter(type -> includeUseAll || !MealPlanType.USEALL.name().equals(type))
                .toList();
        int planCount = planTypes.size();
        Map<String, Object> menu = Map.of(
                "type", "object",
                "properties", Map.of(
                        "menuId", Map.of("type", "integer"),
                        "reason", Map.of("type", "string")
                ),
                "required", List.of("menuId", "reason")
        );
        Map<String, Object> plan = Map.of(
                "type", "object",
                "properties", Map.of(
                        "type", Map.of("type", "string", "enum", planTypes),
                        "reason", Map.of("type", "string"),
                        "menus", Map.of("type", "array", "items", menu, "minItems", 3, "maxItems", 3)
                ),
                "required", List.of("type", "reason", "menus")
        );
        return new MealRecommendationFunctionDeclaration(
                "function",
                FUNCTION_NAME,
                "PRACTICE, USEALL, SIMPLE 식단을 각각 메뉴 세 개로 추천합니다.",
                Map.of(
                        "type", "object",
                        "properties", Map.of("plans", Map.of("type", "array", "items", plan, "minItems", planCount, "maxItems", planCount)),
                        "required", List.of("plans")
                )
        );
    }
}
