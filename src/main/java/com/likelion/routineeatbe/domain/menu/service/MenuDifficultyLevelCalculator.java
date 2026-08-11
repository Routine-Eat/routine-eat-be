package com.likelion.routineeatbe.domain.menu.service;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MenuDifficultyLevelCalculator {

    /**
     * 메뉴의 조리 시간, 레시피 단계 수, 음식 재료 수를 점수화하여 난이도를 계산합니다.
     *
     * @param timeRequired 조리 시간(분)
     * @param recipeStepCount 기본 레시피 단계 수
     * @param ingredientCount 메뉴에 연결된 음식 재료 수
     * @return 세 점수 합계 구간에 해당하는 메뉴 난이도
     */
    DifficultyLevel calculate(
            int timeRequired,
            long recipeStepCount,
            long ingredientCount
    ) {
        log.debug(
                "[MenuDifficultyLevelCalculator] 메뉴 난이도 계산 시작 | calculate() - START | timeRequired: {}, recipeStepCount: {}, ingredientCount: {}",
                timeRequired,
                recipeStepCount,
                ingredientCount
        );

        int timeScore = calculateTimeScore(timeRequired);
        int recipeStepScore = calculateRecipeStepScore(recipeStepCount);
        int ingredientScore = calculateIngredientScore(ingredientCount);
        int totalScore = timeScore + recipeStepScore + ingredientScore;
        DifficultyLevel result = calculateDifficultyLevel(totalScore);

        log.debug(
                "[MenuDifficultyLevelCalculator] 메뉴 난이도 계산 종료 | calculate() - END | timeScore: {}, recipeStepScore: {}, ingredientScore: {}, totalScore: {}, result: {}",
                timeScore,
                recipeStepScore,
                ingredientScore,
                totalScore,
                result
        );
        return result;
    }

    /**
     * 세 기준 점수의 합계를 메뉴 난이도로 변환합니다.
     *
     * @param totalScore 조리 시간, 레시피 단계, 음식 재료 점수의 합계
     * @return 합계 점수 구간에 해당하는 메뉴 난이도
     */
    DifficultyLevel calculateDifficultyLevel(int totalScore) {
        if (totalScore <= 5) {
            return DifficultyLevel.LEVEL_1;
        }
        if (totalScore <= 7) {
            return DifficultyLevel.LEVEL_2;
        }
        if (totalScore <= 9) {
            return DifficultyLevel.LEVEL_3;
        }
        if (totalScore <= 11) {
            return DifficultyLevel.LEVEL_4;
        }
        return DifficultyLevel.LEVEL_5;
    }

    /**
     * 조리 시간을 난이도 점수로 변환합니다.
     *
     * @param timeRequired 조리 시간(분)
     * @return 조리 시간 점수
     */
    int calculateTimeScore(int timeRequired) {
        return timeRequired <= 15 ? 1
                : timeRequired <= 30 ? 2
                : timeRequired <= 45 ? 3
                : timeRequired <= 60 ? 4 : 5;
    }

    /**
     * 기본 레시피 단계 수를 난이도 점수로 변환합니다.
     *
     * @param recipeStepCount 기본 레시피 단계 수
     * @return 레시피 단계 점수
     */
    int calculateRecipeStepScore(long recipeStepCount) {
        return recipeStepCount <= 5 ? 1
                : recipeStepCount <= 8 ? 2
                : recipeStepCount <= 12 ? 3
                : recipeStepCount <= 15 ? 4 : 5;
    }

    /**
     * 메뉴에 연결된 음식 재료 수를 난이도 점수로 변환합니다.
     *
     * @param ingredientCount 메뉴에 연결된 음식 재료 수
     * @return 음식 재료 점수
     */
    int calculateIngredientScore(long ingredientCount) {
        return ingredientCount <= 4 ? 1
                : ingredientCount <= 7 ? 2
                : ingredientCount <= 11 ? 3
                : ingredientCount <= 14 ? 4 : 5;
    }
}
