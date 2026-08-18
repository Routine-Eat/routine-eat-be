package com.likelion.routineeatbe.domain.cookingRecord.dto.gemini;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiFunctionDeclarationDto.Property;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CookingStepGenerateGeminiFunctionDeclarationDtoTest {

    @Test
    @DisplayName("요리 단계 생성 스키마에 요리 팁과 음식 재료 PK 배열을 포함한다")
    void 요리_팁과_음식_재료_PK_배열_스키마_생성_성공() {
        // when
        CookingStepGenerateGeminiFunctionDeclarationDto declaration =
                CookingStepGenerateGeminiFunctionDeclarationDto.create();

        // then
        Property cookingStep = declaration.parameters()
                .properties()
                .get("cookingSteps")
                .items();
        Property cookingTipIds = cookingStep.properties().get("cookingTipIds");
        Property foodIngredientIds = cookingStep.properties().get("foodIngredientIds");

        assertThat(cookingTipIds.type()).isEqualTo("array");
        assertThat(cookingTipIds.items().type()).isEqualTo("integer");
        assertThat(cookingTipIds.items().minimum()).isEqualTo(1);
        assertThat(cookingStep.required()).contains("cookingTipIds");
        assertThat(foodIngredientIds.type()).isEqualTo("array");
        assertThat(foodIngredientIds.items().type()).isEqualTo("integer");
        assertThat(foodIngredientIds.items().minimum()).isEqualTo(1);
        assertThat(cookingStep.required()).contains("foodIngredientIds");
    }
}
