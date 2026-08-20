package com.likelion.routineeatbe.domain.mealPlan.dto.request;

import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlan;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "CreatePlanMealRequest: 식단 메뉴 생성 요청 DTO")
public record CreatePlanMenuRequest(
        @Schema(description = "식단")
        MealPlan mealPlan,
        @Schema(description = "메뉴")
        Menu menu
) {
        public static CreatePlanMenuRequest from(MealPlan mealPlan,Menu menu){
                return CreatePlanMenuRequest.builder()
                        .mealPlan(mealPlan)
                        .menu(menu)
                        .build();
        }
}
