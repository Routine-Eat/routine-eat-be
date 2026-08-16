package com.likelion.routineeatbe.domain.recipe.dto.response;

import com.likelion.routineeatbe.domain.menu.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "AiRecipeRecommendResponse: AI 레시피 추천 응답 DTO")
public record AiRecipeRecommendResponse(
        @Schema(description = "메뉴 식별 id", example = "1")
        Long menuId,
        @Schema(description = "메뉴명", example = "제육볶음")
        String menuName,
        @Schema(description = "메뉴 썸네일 URL", example = "url/fsjfbgvkb...")
        String menuThumbnailUrl,
        @Schema(description = "레시피 식별 id", example = "1")
        Long recipeId,
        @Schema(description = "레시피 추천 이유", example = "제육은 인정이지")
        String reason
) {
    public static AiRecipeRecommendResponse from(
            Menu menu,
            Long recipeId,
            String reason
    ){
        return AiRecipeRecommendResponse.builder()
                .menuId(menu.getId())
                .menuName(menu.getName())
                .recipeId(recipeId)
                .reason(reason)
                .build();
    }
}
