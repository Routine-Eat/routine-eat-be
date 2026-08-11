package com.likelion.routineeatbe.domain.menu.dto.gemini;

import java.util.List;
import lombok.Builder;

@Builder
public record InitMenuAndRecipeCookingEquipmentGeminiResponseDto(
        List<RecipeCookingEquipments> recipes
) {

    public static InitMenuAndRecipeCookingEquipmentGeminiResponseDto create(
            List<RecipeCookingEquipments> recipes
    ) {
        return InitMenuAndRecipeCookingEquipmentGeminiResponseDto.builder()
                .recipes(recipes)
                .build();
    }

    @Builder
    public record RecipeCookingEquipments(
            Integer sequence,
            List<Long> cookingEquipmentIds
    ) {

        public static RecipeCookingEquipments create(
                Integer sequence,
                List<Long> cookingEquipmentIds
        ) {
            return RecipeCookingEquipments.builder()
                    .sequence(sequence)
                    .cookingEquipmentIds(cookingEquipmentIds)
                    .build();
        }
    }
}
