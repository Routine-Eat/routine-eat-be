package com.likelion.routineeatbe.domain.mealPlan.dto.response;

import com.likelion.routineeatbe.domain.mealPlan.entity.PlanMenu;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@Schema(title = "PlanMenuResponse: 식단 메뉴 응답 DTO")
public record PlanMenuResponse(
        @Schema(description = "식단 메뉴 식별자",example = "1")
        Long planMenuId,
        @Schema(description = "식단 메뉴 완료 날짜",example = "2026-00-00")
        LocalDate planMenuDate,
        @Schema(description = "메뉴 완료 여부",example = "true")
        Boolean planMenuCompleted,
        @Schema(description = "메뉴 아이디",example = "true")
        Long menuId,
        @Schema(description = "메뉴 완료 여부",example = "true")
        String menuName
) {
    public static PlanMenuResponse from(
            PlanMenu planMenu
    ){
        return PlanMenuResponse.builder()
                .planMenuId(planMenu.getId())
                .planMenuDate(planMenu.getDate())
                .planMenuCompleted(planMenu.getCompleted())
                .menuId(planMenu.getMenu().getId())
                .menuName(planMenu.getMenu().getName())
                .build();
    }
}
