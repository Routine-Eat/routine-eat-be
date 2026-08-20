package com.likelion.routineeatbe.domain.favoriteRecipe.dto.response;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
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
        @Schema(description = "필요 요리 시간(분)", example = "20")
        Integer timeRequired,
        @Schema(description = "요리 난이도", example = "LEVEL_2")
        DifficultyLevel difficultyLevel,
        @Schema(description = "음식 재료 활용률(%)", example = "72")
        Long foodIngredientUsingPercent
) {

    public static FavoriteRecipeResDto create(
            Long recipeId,
            String menuName,
            String thumbnailUrl,
            Integer timeRequired,
            DifficultyLevel difficultyLevel,
            Long foodIngredientUsingPercent
    ) {
        return FavoriteRecipeResDto.builder()
                .recipeId(recipeId)
                .menuName(menuName)
                .thumbnailUrl(thumbnailUrl)
                .timeRequired(timeRequired)
                .difficultyLevel(difficultyLevel)
                .foodIngredientUsingPercent(foodIngredientUsingPercent)
                .build();
    }
}
