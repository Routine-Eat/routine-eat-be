package com.likelion.routineeatbe.domain.mealPlan.controller;

import com.likelion.routineeatbe.domain.mealPlan.dto.response.AiMealRecommendationResponse;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Meal Plan", description = "식단 관리 API")
@RequestMapping("/api/v1/meal-plans")
public interface MealPlanControllerDocs {

    @Operation(
            summary = "AI 목적별 식단 3종 추천",
            description = """
                알레르기·비선호 식재료와 미보유 조리도구를 제외한 뒤, \n
                PRACTICE(실력 향상), USEALL(보유 재료만 사용), SIMPLE(간단한 요리) 식단을 각각 서로 다른 메뉴 3개로 추천 \n
                보유 재료만으로 만들 수 있는 USEALL 메뉴가 3개 미만이면 useAll은 null로 반환 \n
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "AI 식단 생성 성공"
            )
    })
    @GetMapping("/{userId}/ai-recommendation")
    GlobalResponse<AiMealRecommendationResponse> recommendThreeMeals(@PathVariable Long userId);
}
