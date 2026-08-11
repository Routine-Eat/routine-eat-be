package com.likelion.routineeatbe.domain.menu.dto.gemini;

import java.util.List;
import lombok.Builder;

@Builder
public record InitMenuAndRecipeFoodIngredientGeminiResponseDto(
        List<MenuFoodIngredients> menus
) {

    public static InitMenuAndRecipeFoodIngredientGeminiResponseDto create(
            List<MenuFoodIngredients> menus
    ) {
        return InitMenuAndRecipeFoodIngredientGeminiResponseDto.builder()
                .menus(menus)
                .build();
    }

    @Builder
    public record MenuFoodIngredients(
            Integer sequence,
            List<FoodIngredientNeedAmount> foodIngredients
    ) {

        public static MenuFoodIngredients create(
                Integer sequence,
                List<FoodIngredientNeedAmount> foodIngredients
        ) {
            return MenuFoodIngredients.builder()
                    .sequence(sequence)
                    .foodIngredients(foodIngredients)
                    .build();
        }
    }

    @Builder
    public record FoodIngredientNeedAmount(
            Long foodIngredientId,
            Double primaryNeedAmountValue,
            Double secondaryNeedAmountValue
    ) {

        public static FoodIngredientNeedAmount create(
                Long foodIngredientId,
                Double primaryNeedAmountValue,
                Double secondaryNeedAmountValue
        ) {
            return FoodIngredientNeedAmount.builder()
                    .foodIngredientId(foodIngredientId)
                    .primaryNeedAmountValue(primaryNeedAmountValue)
                    .secondaryNeedAmountValue(secondaryNeedAmountValue)
                    .build();
        }
    }
}
