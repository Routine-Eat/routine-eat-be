package com.likelion.routineeatbe.domain.favoriteRecipe.dto.response;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "FavoriteRecipeResDto", description = "찜한 레시피 조회 항목 DTO")
public record FavoriteRecipeResDto(
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
        @Schema(description = "사용자가 보유한 필요 재료 개수", example = "1")
        Long matchedIngredientCount,
        @Schema(description = "추가로 필요한 음식 재료 개수", example = "5")
        Long requiredIngredientCount,
        @Schema(description = "추가로 필요한 음식 재료비(원)", example = "10000")
        Long requiredIngredientCost
) {

    public static FavoriteRecipeResDto create(
            Long recipeId,
            String menuName,
            String thumbnailUrl,
            Double calory,
            Integer timeRequired,
            DifficultyLevel difficultyLevel,
            MenuType category,
            Long matchedIngredientCount,
            Long requiredIngredientCount,
            Long requiredIngredientCost
    ) {
        return FavoriteRecipeResDto.builder()
                .recipeId(recipeId)
                .menuName(menuName)
                .thumbnailUrl(thumbnailUrl)
                .calory(calory)
                .timeRequired(timeRequired)
                .difficultyLevel(difficultyLevel)
                .category(category)
                .matchedIngredientCount(matchedIngredientCount)
                .requiredIngredientCount(requiredIngredientCount)
                .requiredIngredientCost(requiredIngredientCost)
                .build();
    }
}
