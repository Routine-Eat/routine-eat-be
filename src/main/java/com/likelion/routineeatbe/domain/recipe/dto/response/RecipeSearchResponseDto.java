package com.likelion.routineeatbe.domain.recipe.dto.response;

import com.likelion.routineeatbe.global.response.CursorSliceResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "RecipeSearchResponseDto", description = "추천 유형별 전체 레시피 조회 응답 DTO")
public record RecipeSearchResponseDto(
        @Schema(description = "추천 유형 조건이 없는 전체 레시피 목록")
        CursorSliceResponse<RecipeIngredientUsageListResponseDto> defaultRecipe,
        @Schema(description = "자취생 간단 레시피 목록")
        CursorSliceResponse<RecipeIngredientUsageListResponseDto> simpleRecipe,
        @Schema(description = "다이어트에 좋은 레시피 목록")
        CursorSliceResponse<RecipeIngredientUsageListResponseDto> dietRecipe,
        @Schema(description = "글루텐 프리 식단 레시피 목록")
        CursorSliceResponse<RecipeIngredientUsageListResponseDto> glutenFreeRecipe
) {

    public static RecipeSearchResponseDto create(
            CursorSliceResponse<RecipeIngredientUsageListResponseDto> defaultRecipe,
            CursorSliceResponse<RecipeIngredientUsageListResponseDto> simpleRecipe,
            CursorSliceResponse<RecipeIngredientUsageListResponseDto> dietRecipe,
            CursorSliceResponse<RecipeIngredientUsageListResponseDto> glutenFreeRecipe
    ) {
        return RecipeSearchResponseDto.builder()
                .defaultRecipe(defaultRecipe)
                .simpleRecipe(simpleRecipe)
                .dietRecipe(dietRecipe)
                .glutenFreeRecipe(glutenFreeRecipe)
                .build();
    }
}
