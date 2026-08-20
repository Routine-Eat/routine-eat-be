package com.likelion.routineeatbe.domain.menu.dto.gemini;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("메뉴별 음식 재료 필요량 Function Declaration 스키마를 생성한다")
    void 메뉴별_음식_재료_필요량_Function_Declaration_생성_성공() {
        // given
        InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto declaration =
                InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto.create(10);

        // when
        JsonNode jsonNode = objectMapper.valueToTree(declaration);
        JsonNode menuProperties = jsonNode.at("/parameters/properties/menus/items/properties");
        JsonNode ingredientProperties = menuProperties.at("/foodIngredients/items/properties");

        // then
        assertThat(jsonNode.get("name").asText())
                .isEqualTo(InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto.FUNCTION_NAME);
        assertThat(menuProperties.at("/sequence/minimum").asInt()).isEqualTo(1);
        assertThat(menuProperties.at("/sequence/maximum").asInt()).isEqualTo(10);
        assertThat(ingredientProperties.at("/foodIngredientId/type").asText()).isEqualTo("integer");
        assertThat(ingredientProperties.at("/primaryNeedAmountValue/type").asText()).isEqualTo("number");
        assertThat(menuProperties.at("/foodIngredients/items/required").toString())
                .contains("foodIngredientId", "primaryNeedAmountValue")
                .doesNotContain("secondaryNeedAmountValue");
    }
}
