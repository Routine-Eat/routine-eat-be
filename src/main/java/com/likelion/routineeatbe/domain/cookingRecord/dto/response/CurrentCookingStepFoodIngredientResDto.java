package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(
        title = "CurrentCookingStepFoodIngredientResDto",
        description = "현재 요리 단계 음식 재료 응답 DTO"
)
public record CurrentCookingStepFoodIngredientResDto(
        @Schema(description = "요리 기록 음식 재료 PK", example = "1")
        Long cookingRecordFoodIngredientId,
        @Schema(description = "음식 재료 PK", example = "1")
        Long foodIngredientId,
        @Schema(description = "음식 재료 이름", example = "계란")
        String name,
        @Schema(description = "주 단위 음식 재료 양", example = "80")
        Double primaryAmountValue,
        @Schema(description = "주 단위", example = "G")
        PrimaryUnit primaryUnit,
        @Schema(description = "보조 단위 음식 재료 양", example = "3", nullable = true)
        Double secondaryAmountValue,
        @Schema(description = "보조 단위", example = "AL", nullable = true)
        SecondaryUnit secondaryUnit
) {

    public static CurrentCookingStepFoodIngredientResDto create(
            Long cookingRecordFoodIngredientId,
            Long foodIngredientId,
            String name,
            Double primaryAmountValue,
            PrimaryUnit primaryUnit,
            Double secondaryAmountValue,
            SecondaryUnit secondaryUnit
    ) {
        return CurrentCookingStepFoodIngredientResDto.builder()
                .cookingRecordFoodIngredientId(cookingRecordFoodIngredientId)
                .foodIngredientId(foodIngredientId)
                .name(name)
                .primaryAmountValue(primaryAmountValue)
                .primaryUnit(primaryUnit)
                .secondaryAmountValue(secondaryAmountValue)
                .secondaryUnit(secondaryUnit)
                .build();
    }
}
