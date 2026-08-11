package com.likelion.routineeatbe.domain.menu.dto.gemini;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeCookingEquipmentGeminiFunctionDeclarationDto.Property;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InitMenuAndRecipeCookingEquipmentGeminiFunctionDeclarationDtoTest {

    @Test
    @DisplayName("레시피 배치 크기를 반영한 조리 도구 Function Declaration을 생성한다")
    void 레시피_배치_크기_조리_도구_Function_Declaration_생성_성공() {
        // when
        InitMenuAndRecipeCookingEquipmentGeminiFunctionDeclarationDto result =
                InitMenuAndRecipeCookingEquipmentGeminiFunctionDeclarationDto.create(10);

        // then
        assertThat(result.name()).isEqualTo(
                InitMenuAndRecipeCookingEquipmentGeminiFunctionDeclarationDto.FUNCTION_NAME
        );
        Property recipes = result.parameters().properties().get("recipes");
        Property sequence = recipes.items().properties().get("sequence");
        Property cookingEquipmentIds = recipes.items().properties().get("cookingEquipmentIds");
        assertThat(sequence.minimum()).isEqualTo(1);
        assertThat(sequence.maximum()).isEqualTo(10);
        assertThat(cookingEquipmentIds.items().type()).isEqualTo("integer");
    }
}
