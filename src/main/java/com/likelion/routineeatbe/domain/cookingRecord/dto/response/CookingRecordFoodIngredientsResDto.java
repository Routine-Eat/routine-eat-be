package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(
        title = "CookingRecordFoodIngredientsResDto",
        description = "이번 요리에 사용한 음식 재료 목록 응답 DTO"
)
public record CookingRecordFoodIngredientsResDto(
        @Schema(description = "요리에 사용한 음식 재료 목록")
        List<CookingRecordFoodIngredientAmountResDto> foodIngredients
) {

    public static CookingRecordFoodIngredientsResDto create(
            List<CookingRecordFoodIngredientAmountResDto> foodIngredients
    ) {
        return CookingRecordFoodIngredientsResDto.builder()
                .foodIngredients(List.copyOf(foodIngredients))
                .build();
    }
}
