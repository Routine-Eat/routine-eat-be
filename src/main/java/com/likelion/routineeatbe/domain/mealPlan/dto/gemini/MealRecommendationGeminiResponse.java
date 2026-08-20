package com.likelion.routineeatbe.domain.mealPlan.dto.gemini;

import java.util.List;

/** Gemini Function Calling 결과를 역직렬화하는 내부 DTO입니다. */
public record MealRecommendationGeminiResponse(List<Plan> plans) {
    public record Plan(String type, String reason, List<Menu> menus) {
    }

    public record Menu(Long menuId, String reason) {
    }
}
