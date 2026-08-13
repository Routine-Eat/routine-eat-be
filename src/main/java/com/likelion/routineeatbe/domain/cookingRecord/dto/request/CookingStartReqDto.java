package com.likelion.routineeatbe.domain.cookingRecord.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CookingStartReqDto(
        @Schema(description = "레시피 PK", example = "1")
        @NotNull(message = "레시피 PK는 필수입니다.")
        @Positive(message = "레시피 PK는 양수여야 합니다.")
        Long recipeId,

        @Schema(description = "요리 인분 수", example = "2")
        @NotNull(message = "요리 인분 수는 필수입니다.")
        @Min(value = 1, message = "요리 인분 수는 1 이상이어야 합니다.")
        Integer servings
) {
}
