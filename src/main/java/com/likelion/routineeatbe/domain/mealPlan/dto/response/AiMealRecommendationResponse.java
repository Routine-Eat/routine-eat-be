package com.likelion.routineeatbe.domain.mealPlan.dto.response;

import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanType;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 목적별 식단 추천 응답입니다.
 *
 * <p>{@code useAll}은 보유 식재료만으로 만들 수 있는 서로 다른 메뉴가 세 개 미만이면 {@code null}입니다.
 * 이 경우에도 {@code practice}, {@code simple} 식단은 정상적으로 반환합니다.</p>
 */
public record AiMealRecommendationResponse(Plan practice, Plan useAll, Plan simple, Plan recycling) {

    /** 식단 목적, 식단 전체 추천 이유, 해당 식단을 구성하는 세 메뉴입니다. */
    public record Plan(MealPlanType type, String reason, List<Menu> menus) {
    }

    /** 메뉴 식별자, 표시 이름, 식단 목적에 맞는 개별 추천 정보 */
    public record Menu(
            Long menuId,
            String menuThumbnailUrl,
            String menuName,
            DifficultyLevel difficultyLevel,
            Integer timeRequired,
            @Schema(description = "재료 일치율", example = "88")
            Double sameRate,
            @Schema(description = "예상 재료 가격", example = "1800")
            Long price,
            @Schema(description = "부족한 식재료 목록", example = "[\"양파\", \"대파\"]")
            List<String> missingIngredients
    ) {}
}
