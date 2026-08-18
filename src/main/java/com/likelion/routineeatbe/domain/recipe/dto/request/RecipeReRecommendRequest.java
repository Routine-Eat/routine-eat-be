package com.likelion.routineeatbe.domain.recipe.dto.request;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RecipeReRecommendRequest(
        @Schema(description = "난이도 선택 (미선택 시 null)", example = "LEVEL_1")
        DifficultyLevel difficultyLevel,

        @Schema(description = "조리 시간 선택 (QUICK: 15분 이내, MEDIUM: 15~30분, LONG: 30분 초과)", example = "QUICK")
        CookingTimeFilter timeFilter,

        @Schema(description = "희망 식재료 ID 목록 (최소 1개 포함)", example = "1")
        List<Long> desiredIngredientIds,

        @Schema(description = "처음에 추천 받은 레시피 id", example = "1")
        @NotNull
        Long previousRecipeId
) {
    public enum CookingTimeFilter {
        QUICK,   // 15분 이내
        MEDIUM,  // 15분 초과 ~ 30분 이내
        LONG     // 30분 초과
    }
}