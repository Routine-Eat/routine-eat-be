package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(
        title = "CookingRecordFoodIngredientAmountResDto",
        description = "이번 요리에 사용한 음식 재료 수량 응답 DTO"
)
public record CookingRecordFoodIngredientAmountResDto(
        @Schema(description = "요리 기록 음식 재료 PK", example = "1")
        Long cookingRecordFoodIngredientId,
        @Schema(description = "음식 재료 PK", example = "1")
        Long foodIngredientId,
        @Schema(description = "음식 재료 이름", example = "계란")
        String name,
        @Schema(description = "요리 전 사용자 보유 주 단위 음식 재료 양", example = "80")
        Double prevPrimaryAmountValue,
        @Schema(description = "요리 후 사용자 보유 주 단위 음식 재료 예상량", example = "20")
        Double currentPrimaryAmountValue,
        @Schema(description = "주 단위", example = "G")
        PrimaryUnit primaryUnit,
        @Schema(description = "요리 전 사용자 보유 보조 단위 음식 재료 양", example = "3", nullable = true)
        Double prevSecondaryAmountValue,
        @Schema(description = "요리 후 사용자 보유 보조 단위 음식 재료 예상량", example = "1", nullable = true)
        Double currentSecondaryAmountValue,
        @Schema(description = "보조 단위", example = "AL", nullable = true)
        SecondaryUnit secondaryUnit
) {

    public static CookingRecordFoodIngredientAmountResDto create(
            Long cookingRecordFoodIngredientId,
            Long foodIngredientId,
            String name,
            Double prevPrimaryAmountValue,
            Double currentPrimaryAmountValue,
            PrimaryUnit primaryUnit,
            Double prevSecondaryAmountValue,
            Double currentSecondaryAmountValue,
            SecondaryUnit secondaryUnit
    ) {
        return CookingRecordFoodIngredientAmountResDto.builder()
                .cookingRecordFoodIngredientId(cookingRecordFoodIngredientId)
                .foodIngredientId(foodIngredientId)
                .name(name)
                .prevPrimaryAmountValue(prevPrimaryAmountValue)
                .currentPrimaryAmountValue(currentPrimaryAmountValue)
                .primaryUnit(primaryUnit)
                .prevSecondaryAmountValue(prevSecondaryAmountValue)
                .currentSecondaryAmountValue(currentSecondaryAmountValue)
                .secondaryUnit(secondaryUnit)
                .build();
    }
}
