package com.likelion.routineeatbe.domain.recipe.dto.response;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(title = "RecipeDetailResDto", description = "레시피 상세 조회 응답 DTO")
public record RecipeDetailResDto(
        @Schema(description = "레시피 PK", example = "1")
        Long recipeId,
        @Schema(description = "레시피 이름", example = "계란 야채 볶음밥")
        String recipeName,
        @Schema(description = "레시피 썸네일 URL")
        String recipeThumbnailUrl,
        @Schema(description = "레시피 소요 시간(분)", example = "15")
        Integer recipeTimeRequired,
        @Schema(description = "레시피 난이도", example = "LEVEL_1")
        DifficultyLevel recipeDifficultyLevel,
        @Schema(
                description = "전체 필요 재료 중 사용자가 보유한 재료의 비율(%)",
                example = "60",
                minimum = "0",
                maximum = "100"
        )
        Long foodIngredientUsingPercent,
        @Schema(description = "레시피 조리에 필요한 전체 음식 재료비", example = "3500")
        Long foodIngredientCost,
        @Schema(description = "인분 수", example = "1")
        Integer servings,
        @Schema(description = "필요한 전체 재료 목록")
        List<RecipeIngredientResDto> foodIngredients,
        @Schema(description = "추가로 필요한 재료 목록")
        List<RecipeIngredientResDto> additionalFoodIngredients,
        @Schema(description = "유사 레시피 목록")
        List<SimilarRecipeResDto> similarRecipes
) {

    public static RecipeDetailResDto create(
            Long recipeId,
            String recipeName,
            String recipeThumbnailUrl,
            Integer recipeTimeRequired,
            DifficultyLevel recipeDifficultyLevel,
            Long foodIngredientUsingPercent,
            Long foodIngredientCost,
            Integer servings,
            List<RecipeIngredientResDto> foodIngredients,
            List<RecipeIngredientResDto> additionalFoodIngredients,
            List<SimilarRecipeResDto> similarRecipes
    ) {
        return RecipeDetailResDto.builder()
                .recipeId(recipeId)
                .recipeName(recipeName)
                .recipeThumbnailUrl(recipeThumbnailUrl)
                .recipeTimeRequired(recipeTimeRequired)
                .recipeDifficultyLevel(recipeDifficultyLevel)
                .foodIngredientUsingPercent(foodIngredientUsingPercent)
                .foodIngredientCost(foodIngredientCost)
                .servings(servings)
                .foodIngredients(List.copyOf(foodIngredients))
                .additionalFoodIngredients(List.copyOf(additionalFoodIngredients))
                .similarRecipes(List.copyOf(similarRecipes))
                .build();
    }
}
