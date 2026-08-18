package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;

@Builder
@Schema(title = "CookingRecordListItemResDto", description = "요리 기록 목록 항목 DTO")
public record CookingRecordListItemResDto(
        @Schema(description = "레시피 PK", example = "659")
        Long recipeId,
        @Schema(description = "메뉴 이름", example = "감자미역국")
        String menuName,
        @Schema(description = "메뉴 썸네일 이미지 URL")
        String thumbnailUrl,
        @Schema(description = "현재 사용자의 레시피 찜 여부", example = "true")
        boolean isFavoriteRecipe,
        @Schema(description = "요리 기록 생성 날짜", example = "2026-08-15")
        LocalDate completedAt,
        @Schema(description = "사용자가 평가한 실제 요리 난이도", example = "LEVEL_1")
        DifficultyLevel userDifficultyLevel,
        @Schema(description = "요리에 사용한 음식 재료 종류 개수", example = "8")
        Long usedFoodIngredientCount
) {

    public static CookingRecordListItemResDto create(
            Long recipeId,
            String menuName,
            String thumbnailUrl,
            boolean isFavoriteRecipe,
            LocalDate completedAt,
            DifficultyLevel userDifficultyLevel,
            Long usedFoodIngredientCount
    ) {
        return CookingRecordListItemResDto.builder()
                .recipeId(recipeId)
                .menuName(menuName)
                .thumbnailUrl(thumbnailUrl)
                .isFavoriteRecipe(isFavoriteRecipe)
                .completedAt(completedAt)
                .userDifficultyLevel(userDifficultyLevel)
                .usedFoodIngredientCount(usedFoodIngredientCount)
                .build();
    }
}
