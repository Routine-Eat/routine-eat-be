package com.likelion.routineeatbe.domain.mealPlan.dto.response;

import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlan;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanStatus;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(title = "MealPlanDetailResponse: 식단 상세 응답 DTO")
public record MealPlanDetailResponse(
        Long mealPlanId,
        MealPlanType mealPlanType,
        MealPlanStatus mealPlanStatus,
        List<PlanMenuResponse> planMenuList

){
    public static MealPlanDetailResponse from(
            MealPlan mealPlan,
            List<PlanMenuResponse> planMenuList
    ){
        return MealPlanDetailResponse.builder()
                .mealPlanId(mealPlan.getId())
                .mealPlanType(mealPlan.getType())
                .mealPlanStatus(mealPlan.getStatus())
                .planMenuList(planMenuList)
                .build();
    }
}
