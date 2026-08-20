package com.likelion.routineeatbe.domain.userStatistics.dto.response;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(title = "UserStatisticsResDto", description = "사용자 통계 조회 응답 DTO")
public record UserStatisticsResDto(
        @Schema(description = "레시피 리포트")
        UserStatisticsRecipeReportResDto recipeReport,
        @Schema(description = "많이 사용한 음식 재료 목록")
        List<UserStatisticsFoodIngredientResDto> mostUsedFoodIngredientList,
        @Schema(description = "최근 요리한 5개 레시피의 평균 메뉴 난이도", example = "LEVEL_3")
        DifficultyLevel averageDifficultyLevel
) {

    public static UserStatisticsResDto create(
            UserStatisticsRecipeReportResDto recipeReport,
            List<UserStatisticsFoodIngredientResDto> mostUsedFoodIngredientList,
            DifficultyLevel averageDifficultyLevel
    ) {
        return UserStatisticsResDto.builder()
                .recipeReport(recipeReport)
                .mostUsedFoodIngredientList(List.copyOf(mostUsedFoodIngredientList))
                .averageDifficultyLevel(averageDifficultyLevel)
                .build();
    }
}
