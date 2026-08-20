package com.likelion.routineeatbe.domain.recipe.dto.response;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "RecipeKeywordSearchResDto", description = "검색어 기반 레시피 검색 결과 DTO")
public record RecipeKeywordSearchResDto(
        @Schema(description = "레시피 PK", example = "659")
        Long recipeId,
        @Schema(description = "메뉴/레시피 이름", example = "감자미역국")
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
        @Schema(
                description = "전체 필요 재료 중 사용자가 보유한 재료의 비율(%)",
                example = "100",
                minimum = "0",
                maximum = "100"
        )
        Long foodIngredientUsingPercent,
        @Schema(description = "사용자가 찜한 레시피 여부", example = "true")
        boolean isFavoriteRecipe
) {

    public static RecipeKeywordSearchResDto create(
            Long recipeId,
            String menuName,
            String thumbnailUrl,
            Double calory,
            Integer timeRequired,
            DifficultyLevel difficultyLevel,
            MenuType category,
            Long foodIngredientUsingPercent,
            boolean isFavoriteRecipe
    ) {
        return RecipeKeywordSearchResDto.builder()
                .recipeId(recipeId)
                .menuName(menuName)
                .thumbnailUrl(thumbnailUrl)
                .calory(calory)
                .timeRequired(timeRequired)
                .difficultyLevel(difficultyLevel)
                .category(category)
                .foodIngredientUsingPercent(foodIngredientUsingPercent)
                .isFavoriteRecipe(isFavoriteRecipe)
                .build();
    }
}
