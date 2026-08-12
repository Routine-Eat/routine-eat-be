package com.likelion.routineeatbe.domain.recipe.dto.response;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "RecipeListResponseDto", description = "레시피 목록 조회 항목 DTO")
public record RecipeListResponseDto(
        @Schema(description = "레시피 PK", example = "659")
        Long recipeId,
        @Schema(description = "메뉴 이름", example = "감자미역국")
        String menuName,
        @Schema(description = "메뉴 썸네일 이미지 URL")
        String thumbnailUrl,
        @Schema(description = "칼로리", example = "35.4")
        Double calory,
        @Schema(description = "필요 요리 시간(분)", example = "20")
        Integer timeRequired,
        @Schema(description = "요리 난이도", example = "LEVEL_2")
        DifficultyLevel difficultyLevel,
        @Schema(description = "메뉴 카테고리", example = "KOREAN")
        MenuType category,
        @Schema(description = "요리 횟수", example = "0")
        Long cookingCount,
        @Schema(description = "사용자가 보유한 필요 재료 개수", example = "1")
        Long matchedIngredientCount,
        @Schema(description = "전체 필요 재료 개수", example = "5")
        Long requiredIngredientCount,
        @Schema(description = "부족한 재료를 추가 구매하는 데 필요한 비용(원)", example = "10000")
        Long requiredIngredientCost
) {

    public static RecipeListResponseDto create(
            Long recipeId,
            String menuName,
            String thumbnailUrl,
            Double calory,
            Integer timeRequired,
            DifficultyLevel difficultyLevel,
            MenuType category,
            Long cookingCount,
            Long matchedIngredientCount,
            Long requiredIngredientCount,
            Long requiredIngredientCost
    ) {
        return RecipeListResponseDto.builder()
                .recipeId(recipeId)
                .menuName(menuName)
                .thumbnailUrl(thumbnailUrl)
                .calory(calory)
                .timeRequired(timeRequired)
                .difficultyLevel(difficultyLevel)
                .category(category)
                .cookingCount(cookingCount)
                .matchedIngredientCount(matchedIngredientCount)
                .requiredIngredientCount(requiredIngredientCount)
                .requiredIngredientCost(requiredIngredientCost)
                .build();
    }
}
