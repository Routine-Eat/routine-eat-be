package com.likelion.routineeatbe.domain.userStatistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "UserStatisticsRecipeResDto", description = "사용자 통계 레시피 응답 DTO")
public record UserStatisticsRecipeResDto(
        @Schema(description = "레시피 PK", example = "1")
        Long recipeId,
        @Schema(description = "레시피 이름", example = "계란 야채 볶음밥")
        String recipeName
) {

    public static UserStatisticsRecipeResDto create(Long recipeId, String recipeName) {
        return UserStatisticsRecipeResDto.builder()
                .recipeId(recipeId)
                .recipeName(recipeName)
                .build();
    }
}
