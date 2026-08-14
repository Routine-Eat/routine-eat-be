package com.likelion.routineeatbe.domain.mealPlan.dto.response;

import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanStatus;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(title = "MealPlanResponse: 식단 응답 DTO")
public record MealPlanResponse (
        Long mealPlanId,
        MealPlanType mealPlanType,
        MealPlanStatus mealPlanStatus,
        List<PlanMenuResponse> planMenuList

){
    public static MealPlanResponse from(
            Long mealPlanId,
            MealPlanType mealPlanType,
            MealPlanStatus mealPlanStatus,
            List<PlanMenuResponse> planMenuList
    ){
        return MealPlanResponse.builder()
                .mealPlanId(mealPlanId)
                .mealPlanType(mealPlanType)
                .mealPlanStatus(mealPlanStatus)
                .planMenuList(planMenuList)
                .build();
    }
}
