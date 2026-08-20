package com.likelion.routineeatbe.domain.recipe.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RecipeDetailReqDto(
        @Schema(
                description = "사용자 고유 식별번호",
                example = "1234",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Size(min = 4, max = 4)
        String userNumber,

        @Schema(
                description = "조회할 레시피의 인분 수",
                example = "1",
                defaultValue = "1",
                minimum = "1"
        )
        @Positive
        Integer servings
) {

    public RecipeDetailReqDto {
        servings = servings == null ? 1 : servings;
    }
}
