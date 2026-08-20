package com.likelion.routineeatbe.domain.cookingRecord.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(
        title = "ModifiedCookingRecordFoodIngredientReqDto",
        description = "요리 기록 음식 재료 실제 사용량 수정 요청 DTO"
)
public record ModifiedCookingRecordFoodIngredientReqDto(
        @Schema(description = "요리 기록 음식 재료 PK", example = "1")
        @NotNull(message = "요리 기록 음식 재료 PK는 필수입니다.")
        @Positive(message = "요리 기록 음식 재료 PK는 양수여야 합니다.")
        Long cookingRecordFoodIngredientId,

        @Schema(
                description = "실제로 사용한 주 단위 음식 재료 양, null이면 기존 값 유지",
                example = "80",
                nullable = true
        )
        @PositiveOrZero(message = "주 단위 사용량은 0 이상이어야 합니다.")
        Double usedPrimaryAmountValue,

        @Schema(
                description = "실제로 사용한 보조 단위 음식 재료 양, null이면 기존 값 유지",
                example = "2",
                nullable = true
        )
        @PositiveOrZero(message = "보조 단위 사용량은 0 이상이어야 합니다.")
        Double usedSecondaryAmountValue
) {
}
