package com.likelion.routineeatbe.domain.userStatistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(title = "UserStatisticsRecipeReportResDto", description = "사용자 통계 레시피 리포트 응답 DTO")
public record UserStatisticsRecipeReportResDto(
        @Schema(description = "중복 제거된 요리 레시피 개수", example = "10")
        int count,
        @Schema(description = "중복 제거된 요리 레시피 목록")
        List<UserStatisticsRecipeResDto> recipeList
) {

    public static UserStatisticsRecipeReportResDto create(
            List<UserStatisticsRecipeResDto> recipeList
    ) {
        return UserStatisticsRecipeReportResDto.builder()
                .count(recipeList.size())
                .recipeList(List.copyOf(recipeList))
                .build();
    }
}
