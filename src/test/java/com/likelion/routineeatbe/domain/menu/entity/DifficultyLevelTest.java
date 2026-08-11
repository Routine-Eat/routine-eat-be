package com.likelion.routineeatbe.domain.menu.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DifficultyLevelTest {

    @ParameterizedTest
    @CsvSource({
            "1, LEVEL_1",
            "2, LEVEL_2",
            "3, LEVEL_3",
            "4, LEVEL_4",
            "5, LEVEL_5"
    })
    void 난이도_점수_DifficultyLevel_변환_성공(int score, DifficultyLevel expected) {
        // when
        DifficultyLevel result = DifficultyLevel.fromScore(score);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6})
    void 범위를_벗어난_난이도_점수_변환_실패(int score) {
        // when & then
        assertThatThrownBy(() -> DifficultyLevel.fromScore(score))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
