package com.likelion.routineeatbe.domain.recipe.dto.request;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeSortType;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeTimeRequiredFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecipeSearchRequestDto(
        @Schema(
                description = "사용자 고유 식별번호",
                example = "1234",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Size(min = 4, max = 4)
        String userNumber,

        @Schema(
                description = "1부터 시작하는 조회 위치. 이후 조회는 응답의 nextCursor를 사용합니다.",
                example = "1",
                defaultValue = "1",
                minimum = "1",
                maximum = "2147483647"
        )
        @Min(1)
        @Max(Integer.MAX_VALUE)
        Long cursor,

        @Schema(
                description = "한 번에 유형별로 조회할 레시피 개수",
                example = "10",
                defaultValue = "10",
                minimum = "1",
                maximum = "100"
        )
        @Min(1)
        @Max(100)
        Integer size,

        @Schema(
                description = "요리 시간 구간 필터",
                example = "WITHIN_30_MINUTES",
                allowableValues = {
                        "WITHIN_15_MINUTES",
                        "WITHIN_30_MINUTES",
                        "OVER_30_MINUTES"
                }
        )
        RecipeTimeRequiredFilter timeRequired,

        @Schema(
                description = "요리 난이도",
                example = "LEVEL_2",
                allowableValues = {"LEVEL_1", "LEVEL_2", "LEVEL_3", "LEVEL_4", "LEVEL_5"}
        )
        DifficultyLevel difficultyLevel,

        @Schema(
                description = "메뉴 카테고리",
                example = "KOREAN",
                allowableValues = {"KOREAN", "CHINESE", "JAPANESE", "WESTERN", "OTHER"}
        )
        MenuType category,

        @Schema(
                description = "정렬 방식. DEFAULT는 인기순, FOOD_INTEGRATION은 보유 재료 일치도순입니다.",
                example = "DEFAULT",
                defaultValue = "DEFAULT",
                allowableValues = {"DEFAULT", "FOOD_INTEGRATION"}
        )
        RecipeSortType sortType
) {

    public RecipeSearchRequestDto {
        cursor = cursor == null ? 1L : cursor;
        size = size == null ? 10 : size;
        sortType = sortType == null ? RecipeSortType.DEFAULT : sortType;
    }
}
