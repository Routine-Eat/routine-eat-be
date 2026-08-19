package com.likelion.routineeatbe.domain.userStatistics.mapper;

import com.likelion.routineeatbe.domain.userStatistics.dto.response.UserStatisticsFoodIngredientResDto;
import com.likelion.routineeatbe.domain.userStatistics.dto.response.UserStatisticsRecipeReportResDto;
import com.likelion.routineeatbe.domain.userStatistics.dto.response.UserStatisticsRecipeResDto;
import com.likelion.routineeatbe.domain.userStatistics.dto.response.UserStatisticsResDto;
import com.likelion.routineeatbe.domain.userStatistics.entity.UserStatistics;
import com.likelion.routineeatbe.domain.userStatistics.entity.UserStatisticsFoodIngredient;
import com.likelion.routineeatbe.domain.userStatistics.entity.UserStatisticsRecipe;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UserStatisticsMapper {

    /**
     * 사용자 통계와 하위 통계 데이터를 사용자 통계 응답 DTO로 변환합니다.
     *
     * @param userStatistics 사용자 통계
     * @param statisticsRecipes 통계에 포함된 레시피 목록
     * @param statisticsFoodIngredients 통계에 포함된 음식 재료 목록
     * @return 사용자 통계 응답 DTO
     */
    public UserStatisticsResDto toUserStatisticsResDto(
            UserStatistics userStatistics,
            List<UserStatisticsRecipe> statisticsRecipes,
            List<UserStatisticsFoodIngredient> statisticsFoodIngredients
    ) {
        List<UserStatisticsRecipeResDto> recipeList = statisticsRecipes.stream()
                .map(this::toUserStatisticsRecipeResDto)
                .toList();
        List<UserStatisticsFoodIngredientResDto> foodIngredientList =
                statisticsFoodIngredients.stream()
                        .map(this::toUserStatisticsFoodIngredientResDto)
                        .toList();
        return UserStatisticsResDto.create(
                UserStatisticsRecipeReportResDto.create(recipeList),
                foodIngredientList,
                userStatistics.getCookedRecordAverageDifficultyLevel()
        );
    }

    /**
     * 사용자 통계 레시피 Entity를 응답 DTO로 변환합니다.
     *
     * @param statisticsRecipe 사용자 통계 레시피 Entity
     * @return 사용자 통계 레시피 응답 DTO
     */
    public UserStatisticsRecipeResDto toUserStatisticsRecipeResDto(
            UserStatisticsRecipe statisticsRecipe
    ) {
        return UserStatisticsRecipeResDto.create(
                statisticsRecipe.getRecipe().getId(),
                statisticsRecipe.getRecipe().getMenu().getName()
        );
    }

    /**
     * 사용자 통계 음식 재료 Entity를 응답 DTO로 변환합니다.
     *
     * @param statisticsFoodIngredient 사용자 통계 음식 재료 Entity
     * @return 사용자 통계 음식 재료 응답 DTO
     */
    public UserStatisticsFoodIngredientResDto toUserStatisticsFoodIngredientResDto(
            UserStatisticsFoodIngredient statisticsFoodIngredient
    ) {
        return UserStatisticsFoodIngredientResDto.create(
                statisticsFoodIngredient.getFoodIngredient().getId(),
                statisticsFoodIngredient.getFoodIngredient().getName()
        );
    }
}
