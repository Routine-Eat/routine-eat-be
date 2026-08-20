package com.likelion.routineeatbe.domain.mealPlan.dto.response;

import com.likelion.routineeatbe.domain.mealPlan.entity.PlanMenu;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
@Schema(title = "PlanMenuResponse: 식단 메뉴 응답 DTO")
public record PlanMenuResponse(
        @Schema(description = "식단 메뉴 식별자", example = "1")
        Long planMenuId,
        @Schema(description = "식단 메뉴 완료 날짜", example = "2026-00-00")
        LocalDate planMenuDate,
        @Schema(description = "메뉴 완료 여부", example = "true")
        Boolean planMenuCompleted,
        @Schema(description = "메뉴 아이디", example = "1")
        Long menuId,
        @Schema(description = "메뉴 이름", example = "김치찌개")
        String menuName,
        @Schema(description = "메뉴 난이도", example = "EASY")
        DifficultyLevel difficultyLevel,
        @Schema(description = "소요시간", example = "15")
        Integer timeRequired,
        @Schema(description = "부족한 식재료 목록", example = "[\"양파\", \"대파\"]")
        List<String> missingIngredients
) {
    public static PlanMenuResponse from(
            PlanMenu planMenu,
            List<String> missingIngredients
    ) {
        Menu menu = planMenu.getMenu();
        return PlanMenuResponse.builder()
                .planMenuId(planMenu.getId())
                .planMenuDate(planMenu.getDate())
                .planMenuCompleted(planMenu.getCompleted())
                .menuId(menu.getId())
                .menuName(menu.getName())
                .difficultyLevel(menu.getDifficultyLevel())
                .timeRequired(menu.getTimeRequired())
                .missingIngredients(missingIngredients)
                .build();
    }
}