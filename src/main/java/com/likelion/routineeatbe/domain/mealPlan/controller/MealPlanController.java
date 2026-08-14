package com.likelion.routineeatbe.domain.mealPlan.controller;

import com.likelion.routineeatbe.domain.mealPlan.service.MealPlanService;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.AiMealRecommendationResponse;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MealPlanController implements MealPlanControllerDocs{
    private final MealPlanService mealPlanService;

    @Override
    public GlobalResponse<AiMealRecommendationResponse> recommendThreeMeals(Long userId) {
        // HTTP 계층은 요청/공통 응답 포장만 담당하고, 추천 규칙은 Service에 위임합니다.
        return GlobalResponse.success(mealPlanService.recommendThreeMeals(userId));
    }
}
