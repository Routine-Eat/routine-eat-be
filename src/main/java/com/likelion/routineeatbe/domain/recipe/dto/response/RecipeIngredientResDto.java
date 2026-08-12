package com.likelion.routineeatbe.domain.recipe.dto.response;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "RecipeIngredientResDto", description = "레시피 필요 재료 응답 DTO")
public record RecipeIngredientResDto(
        @Schema(description = "음식 재료 PK", example = "1")
        Long id,
        @Schema(description = "음식 재료 이름", example = "밥")
        String name,
        @Schema(description = "음식 재료 타입", example = "GRAIN")
        FoodIngredientType type,
        @Schema(description = "주 단위 필요량", example = "250")
        Double primaryNeedAmountValue,
        @Schema(description = "주 단위", example = "g")
        String primaryUnit,
        @Schema(description = "보조 단위 필요량", example = "1")
        Double secondaryNeedAmountValue,
        @Schema(description = "보조 단위", example = "공기")
        String secondaryUnit
) {

    public static RecipeIngredientResDto create(
            Long id,
            String name,
            FoodIngredientType type,
            Double primaryNeedAmountValue,
            String primaryUnit,
            Double secondaryNeedAmountValue,
            String secondaryUnit
    ) {
        return RecipeIngredientResDto.builder()
                .id(id)
                .name(name)
                .type(type)
                .primaryNeedAmountValue(primaryNeedAmountValue)
                .primaryUnit(primaryUnit)
                .secondaryNeedAmountValue(secondaryNeedAmountValue)
                .secondaryUnit(secondaryUnit)
                .build();
    }
}
