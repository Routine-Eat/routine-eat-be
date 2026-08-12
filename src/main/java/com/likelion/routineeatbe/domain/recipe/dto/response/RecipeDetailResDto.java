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
        @Schema(description = "추가로 필요한 음식 재료 개수", example = "2")
        Long additionalFoodIngredientCount,
        @Schema(description = "추가로 필요한 음식 재료비", example = "1800")
        Long additionalFoodIngredientCost,
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
            Long additionalFoodIngredientCount,
            Long additionalFoodIngredientCost,
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
                .additionalFoodIngredientCount(additionalFoodIngredientCount)
                .additionalFoodIngredientCost(additionalFoodIngredientCost)
                .servings(servings)
                .foodIngredients(List.copyOf(foodIngredients))
                .additionalFoodIngredients(List.copyOf(additionalFoodIngredients))
                .similarRecipes(List.copyOf(similarRecipes))
                .build();
    }
}
