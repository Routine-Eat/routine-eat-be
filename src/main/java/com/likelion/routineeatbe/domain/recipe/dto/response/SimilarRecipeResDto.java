package com.likelion.routineeatbe.domain.recipe.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "SimilarRecipeResDto", description = "유사 레시피 응답 DTO")
public record SimilarRecipeResDto(
        @Schema(description = "레시피 PK", example = "10")
        Long id,
        @Schema(description = "레시피 이름", example = "김치 볶음밥")
        String name,
        @Schema(description = "추가로 필요한 음식 재료 개수", example = "1")
        Long additionalFoodIngredientCount,
        @Schema(description = "레시피 썸네일 이미지", example = "https://...")
        String recipeThumbnailUrl,
        @Schema(description = "사용자가 찜한 레시피 여부", example = "true")
        boolean isFavoriteRecipe
) {

    public static SimilarRecipeResDto create(
            Long id,
            String name,
            Long additionalFoodIngredientCount,
            String recipeThumbnailUrl,
            boolean isFavoriteRecipe
    ) {
        return SimilarRecipeResDto.builder()
                .id(id)
                .name(name)
                .additionalFoodIngredientCount(additionalFoodIngredientCount)
                .recipeThumbnailUrl(recipeThumbnailUrl)
                .isFavoriteRecipe(isFavoriteRecipe)
                .build();
    }
}
