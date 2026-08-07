package com.likelion.routineeatbe.global.util;

import com.likelion.routineeatbe.global.config.GeminiProperties;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiRetryDelayStrategy {

    private final GeminiProperties properties;

    /**
     * Gemini 429 재시도 횟수에 따라 지수 백오프와 jitter를 적용하여 대기합니다.
     *
     * @param retryCount 첫 번째 재시도를 1로 사용하는 재시도 횟수
     * @return 실제 대기한 밀리초
     */
    public long waitBeforeRetry(int retryCount) {
        long initialDelayMillis = properties.retry().initialDelay().toMillis();
        long maxDelayMillis = properties.retry().maxDelay().toMillis();
        int exponent = Math.min(retryCount - 1, Long.SIZE - 2);
        long multiplier = 1L << exponent;
        long baseDelayMillis = initialDelayMillis > maxDelayMillis / multiplier
                ? maxDelayMillis
                : Math.min(initialDelayMillis * multiplier, maxDelayMillis);
        long maxJitterMillis = (long) (baseDelayMillis * properties.retry().jitterRatio());
        long jitterMillis = maxJitterMillis == 0
                ? 0
                : ThreadLocalRandom.current().nextLong(maxJitterMillis + 1);
        long delayMillis = baseDelayMillis + jitterMillis;

        log.warn(
                "[GeminiRetryDelayStrategy] Gemini API 재시도 대기 | retryCount: {}, delayMillis: {}",
                retryCount,
                delayMillis
        );

        try {
            Thread.sleep(delayMillis);
            return delayMillis;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CustomException(GeminiErrorCode.API_CALL_FAILED);
        }
    }
}
