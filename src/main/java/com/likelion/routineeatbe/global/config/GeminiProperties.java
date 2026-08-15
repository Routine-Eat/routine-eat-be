package com.likelion.routineeatbe.global.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Gemini API 호출에 필요한 설정을 관리합니다.
 *
 * @param baseUrl Gemini Interactions API 주소
 * @param apiKey Gemini API 인증 키
 * @param menuAnalyzeModel 메뉴 호출할 Gemini 모델명
 * @param foodIngredientAnalyzeModel 음식 재료 분석에 사용할 Gemini 모델명
 * @param cookingEquipmentAnalyzeModel 조리 도구 분석에 사용할 Gemini 모델명
 * @param cookingStepGenerateModel 요리 단계 생성에 사용할 Gemini 모델명
 */
@Validated
@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        @NotBlank String baseUrl,
        @NotBlank String apiKey,
        @NotBlank String menuAnalyzeModel,
        @NotBlank String foodIngredientAnalyzeModel,
        @NotBlank String cookingEquipmentAnalyzeModel,
        @NotBlank String cookingStepGenerateModel,
        @Min(1) int batchSize,
        @Valid @NotNull Retry retry
) {

    public record Retry(
            @Min(1) int maxAttempts,
            @NotNull Duration initialDelay,
            @NotNull Duration maxDelay,
            @DecimalMin("0.0") @DecimalMax("1.0") double jitterRatio
    ) {

        public Retry {
            if (initialDelay != null && (initialDelay.isZero() || initialDelay.isNegative())) {
                throw new IllegalArgumentException("Gemini retry initialDelay must be positive");
            }
            if (maxDelay != null && (maxDelay.isZero() || maxDelay.isNegative())) {
                throw new IllegalArgumentException("Gemini retry maxDelay must be positive");
            }
            if (initialDelay != null && maxDelay != null && maxDelay.compareTo(initialDelay) < 0) {
                throw new IllegalArgumentException("Gemini retry maxDelay must be greater than initialDelay");
            }
        }
    }
}
