package com.likelion.routineeatbe.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MenuDifficultyLevelCalculatorTest {

    private final MenuDifficultyLevelCalculator calculator =
            new MenuDifficultyLevelCalculator();

    @ParameterizedTest
    @CsvSource({
            "15, 1", "16, 2", "30, 2", "31, 3", "45, 3",
            "46, 4", "60, 4", "61, 5"
    })
    void 조리_시간_점수_경계값_계산_성공(int timeRequired, int expected) {
        // when
        int result = calculator.calculateTimeScore(timeRequired);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "5, 1", "6, 2", "8, 2", "9, 3", "12, 3",
            "13, 4", "15, 4", "16, 5"
    })
    void 레시피_단계_점수_경계값_계산_성공(long recipeStepCount, int expected) {
        // when
        int result = calculator.calculateRecipeStepScore(recipeStepCount);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "4, 1", "5, 2", "7, 2", "8, 3", "11, 3",
            "12, 4", "14, 4", "15, 5"
    })
    void 음식_재료_점수_경계값_계산_성공(long ingredientCount, int expected) {
        // when
        int result = calculator.calculateIngredientScore(ingredientCount);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "15, 5, 4, LEVEL_1",
            "30, 8, 7, LEVEL_2",
            "45, 12, 11, LEVEL_3",
            "60, 12, 11, LEVEL_4",
            "61, 16, 15, LEVEL_5",
            "0, 0, 0, LEVEL_1"
    })
    void 메뉴_난이도_구간별_계산_성공(
            int timeRequired,
            long recipeStepCount,
            long ingredientCount,
            DifficultyLevel expected
    ) {
        // when
        DifficultyLevel result = calculator.calculate(
                timeRequired,
                recipeStepCount,
                ingredientCount
        );

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "3, LEVEL_1", "5, LEVEL_1",
            "6, LEVEL_2", "7, LEVEL_2",
            "8, LEVEL_3", "9, LEVEL_3",
            "10, LEVEL_4", "11, LEVEL_4",
            "12, LEVEL_5", "15, LEVEL_5"
    })
    void 메뉴_난이도_합계_구간별_계산_성공(
            int totalScore,
            DifficultyLevel expected
    ) {
        // when
        DifficultyLevel result = calculator.calculateDifficultyLevel(totalScore);

        // then
        assertThat(result).isEqualTo(expected);
    }
}
