package com.likelion.routineeatbe.domain.recipe.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CanCookReqDto(
        @Schema(
                description = "사용자 고유 식별번호",
                example = "1234",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "사용자 고유 식별번호는 필수입니다.")
        @Size(min = 4, max = 4, message = "사용자 고유 식별번호는 4자리여야 합니다.")
        @Pattern(regexp = "^[0-9]{4}$", message = "사용자 고유 식별번호는 숫자 4자리여야 합니다.")
        String userNumber,

        @Schema(
                description = "요리할 인분 수",
                example = "1",
                defaultValue = "1",
                minimum = "1"
        )
        @Positive(message = "요리 인분 수는 1 이상이어야 합니다.")
        Integer servings
) {

    public CanCookReqDto {
        servings = servings == null ? 1 : servings;
    }
}
