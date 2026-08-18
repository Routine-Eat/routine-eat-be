package com.likelion.routineeatbe.domain.recipe.dto.response;

import com.likelion.routineeatbe.global.response.CursorSliceResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "RecipeSearchResponseDto", description = "남은 재료 및 추천 유형별 레시피 조회 응답 DTO")
public record RecipeSearchResponseDto(
        @Schema(description = "사용자가 가장 많이 보유한 음식 재료 이름", example = "감자")
        String remainFoodIngredientName,
        @Schema(description = "사용자가 가장 많이 보유한 음식 재료가 포함된 레시피 목록")
        CursorSliceResponse<RecipeIngredientUsageListResponseDto> remainFoodIngredient,
        @Schema(description = "조리 시간이 15분 이하인 간단 레시피 목록")
        CursorSliceResponse<RecipeIngredientUsageListResponseDto> simpleRecipe,
        @Schema(description = "다이어트에 좋은 레시피 목록")
        CursorSliceResponse<RecipeIngredientUsageListResponseDto> dietRecipe,
        @Schema(description = "글루텐 프리 식단 레시피 목록")
        CursorSliceResponse<RecipeIngredientUsageListResponseDto> glutenFreeRecipe
) {

    public static RecipeSearchResponseDto create(
            String remainFoodIngredientName,
            CursorSliceResponse<RecipeIngredientUsageListResponseDto> remainFoodIngredient,
            CursorSliceResponse<RecipeIngredientUsageListResponseDto> simpleRecipe,
            CursorSliceResponse<RecipeIngredientUsageListResponseDto> dietRecipe,
            CursorSliceResponse<RecipeIngredientUsageListResponseDto> glutenFreeRecipe
    ) {
        return RecipeSearchResponseDto.builder()
                .remainFoodIngredientName(remainFoodIngredientName)
                .remainFoodIngredient(remainFoodIngredient)
                .simpleRecipe(simpleRecipe)
                .dietRecipe(dietRecipe)
                .glutenFreeRecipe(glutenFreeRecipe)
                .build();
    }
}
