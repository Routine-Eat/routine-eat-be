package com.likelion.routineeatbe.global.dto.gemini;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto;
import com.likelion.routineeatbe.domain.menu.dto.gemini.MenuAndRecipeGeminiFunctionDeclarationDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class GeminiInteractionReqDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("서로 다른 Function Declaration을 공통 tools 목록으로 직렬화한다")
    void 서로_다른_Function_Declaration_공통_tools_직렬화_성공() {
        // given
        List<GeminiFunctionDeclaration> tools = List.of(
                MenuAndRecipeGeminiFunctionDeclarationDto.create(2),
                InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto.create(2)
        );
        GeminiInteractionReqDto request = GeminiInteractionReqDto.create(
                "test-model",
                "test-input",
                tools,
                tools.stream().map(GeminiFunctionDeclaration::name).toList()
        );

        // when
        JsonNode jsonNode = objectMapper.valueToTree(request);

        // then
        assertThat(request.tools()).hasSize(2);
        assertThat(jsonNode.at("/tools/0/name").asText())
                .isEqualTo(MenuAndRecipeGeminiFunctionDeclarationDto.FUNCTION_NAME);
        assertThat(jsonNode.at("/tools/1/name").asText())
                .isEqualTo(InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto.FUNCTION_NAME);
        assertThat(jsonNode.at("/generation_config/tool_choice/allowed_tools/tools").size())
                .isEqualTo(2);
    }
}
