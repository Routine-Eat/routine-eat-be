package com.likelion.routineeatbe.domain.menu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 식품 안전 레시피 API 요청 DTO
 */
public record FoodSafetyKoreaRecipeApiRequestDto(
        @NotBlank String keyId,
        @NotBlank String serviceId,
        @NotBlank String dataType,
        @Min(1) int startIdx,
        @Min(1) int endIdx
) {

    public static FoodSafetyKoreaRecipeApiRequestDto create(
            String keyId,
            String serviceId,
            String dataType,
            int startIdx,
            int endIdx
    ) {
        return new FoodSafetyKoreaRecipeApiRequestDto(keyId, serviceId, dataType, startIdx, endIdx);
    }
}
