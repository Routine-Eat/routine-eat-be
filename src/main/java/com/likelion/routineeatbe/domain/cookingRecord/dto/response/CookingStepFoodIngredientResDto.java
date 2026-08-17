package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(
        title = "CookingStepFoodIngredientResDto",
        description = "현재 요리 단계에서 사용하는 음식 재료 응답 DTO"
)
public record CookingStepFoodIngredientResDto(
        @Schema(description = "요리 기록 음식 재료 PK", example = "1")
        Long cookingRecordFoodIngredientId,
        @Schema(description = "음식 재료 PK", example = "1")
        Long foodIngredientId,
        @Schema(description = "음식 재료 이름", example = "계란")
        String name,
        @Schema(description = "요리에 사용하는 주 단위 음식 재료 양", example = "60")
        Double primaryUsedAmountValue,
        @Schema(description = "주 단위", example = "G")
        PrimaryUnit primaryUnit,
        @Schema(
                description = "요리에 사용하는 보조 단위 음식 재료 양",
                example = "2",
                nullable = true
        )
        Double secondaryUsedAmountValue,
        @Schema(description = "보조 단위", example = "AL", nullable = true)
        SecondaryUnit secondaryUnit
) {

    public static CookingStepFoodIngredientResDto create(
            Long cookingRecordFoodIngredientId,
            Long foodIngredientId,
            String name,
            Double primaryUsedAmountValue,
            PrimaryUnit primaryUnit,
            Double secondaryUsedAmountValue,
            SecondaryUnit secondaryUnit
    ) {
        return CookingStepFoodIngredientResDto.builder()
                .cookingRecordFoodIngredientId(cookingRecordFoodIngredientId)
                .foodIngredientId(foodIngredientId)
                .name(name)
                .primaryUsedAmountValue(primaryUsedAmountValue)
                .primaryUnit(primaryUnit)
                .secondaryUsedAmountValue(secondaryUsedAmountValue)
                .secondaryUnit(secondaryUnit)
                .build();
    }
}
