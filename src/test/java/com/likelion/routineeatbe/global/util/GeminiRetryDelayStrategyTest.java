package com.likelion.routineeatbe.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.global.config.GeminiProperties;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GeminiRetryDelayStrategyTest {

    @Test
    @DisplayName("재시도 횟수에 따라 지수 백오프를 적용하고 최대 대기시간을 넘지 않는다")
    void 재시도_횟수_지수_백오프_최대값_적용_성공() {
        // given
        GeminiProperties properties = new GeminiProperties(
                "https://example.com/interactions",
                "test-key",
                "menu-model",
                "food-ingredient-model",
                "cooking-equipment-model",
                "cooking-step-model",
                10,
                new GeminiProperties.Retry(
                        4,
                        Duration.ofMillis(1),
                        Duration.ofMillis(4),
                        0.0
                )
        );
        GeminiRetryDelayStrategy strategy = new GeminiRetryDelayStrategy(properties);

        // when
        long firstDelay = strategy.waitBeforeRetry(1);
        long secondDelay = strategy.waitBeforeRetry(2);
        long thirdDelay = strategy.waitBeforeRetry(3);
        long fourthDelay = strategy.waitBeforeRetry(4);

        // then
        assertThat(firstDelay).isEqualTo(1);
        assertThat(secondDelay).isEqualTo(2);
        assertThat(thirdDelay).isEqualTo(4);
        assertThat(fourthDelay).isEqualTo(4);
    }
}
