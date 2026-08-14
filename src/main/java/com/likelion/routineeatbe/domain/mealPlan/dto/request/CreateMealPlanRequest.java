package com.likelion.routineeatbe.domain.mealPlan.dto.request;

import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanStatus;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(title = "CreateMealPlanRequest: 식단 저장 요청 DTO")
public record CreateMealPlanRequest(
        @Schema(description = "식단 종류",example = "PRACTICE")
        MealPlanType mealPlanType,
        @Schema(description = "식단 저장 상태",example = "SAVED")
        MealPlanStatus mealPlanStatus,
        @Schema(description = "식단 메뉴 리스트",example = "[1,2,3]")
        List<Long> planMenuIdList
) {
}
