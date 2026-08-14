package com.likelion.routineeatbe.domain.mealPlan.dto.response;

import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlan;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanStatus;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(title = "MealPlanResponse: 식단 목록 응답 DTO")
public record MealPlanResponse(
        Long mealPlanId,
        MealPlanType mealPlanType,
        MealPlanStatus mealPlanStatus,
        List<Long> planMenuIdList
) {
    public static MealPlanResponse from(
            MealPlan mealPlan,
            List<Long> planMenuIdList
    ){
        return MealPlanResponse.builder()
                .mealPlanId(mealPlan.getId())
                .mealPlanType(mealPlan.getType())
                .mealPlanStatus(mealPlan.getStatus())
                .planMenuIdList(planMenuIdList)
                .build();
    }
}
