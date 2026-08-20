package com.likelion.routineeatbe.domain.menu.crawling;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 식품안정청 API 관련 속성들을 Bean으로 등록
 * @param baseUrl
 * @param apiKey
 * @param serviceId
 * @param dataType
 * @param maxRequestCount 한 번에 요청할 최대 데이터 건수
 */
@Validated
@ConfigurationProperties(prefix = "data.crawling.food-safety-korea")
public record FoodSafetyKoreaProperties(
        @NotBlank String baseUrl,
        @NotBlank String apiKey,
        @NotBlank String serviceId,
        @NotBlank @Pattern(regexp = "(?i)json") String dataType,
        @NotNull @Min(1) @Max(1000) Integer maxRequestCount
) {
}
