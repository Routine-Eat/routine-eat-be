package com.likelion.routineeatbe.global.dto.gemini;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class GeminiFunctionDeclarationDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("배치 메뉴 배열과 메타데이터 스키마를 Gemini Function Declaration JSON으로 생성한다")
    void 배치_메뉴_배열_메타데이터_스키마_직렬화_성공() {
        // given
        GeminiFunctionDeclarationDto functionDeclaration = GeminiFunctionDeclarationDto.create();

        // when
        JsonNode jsonNode = objectMapper.valueToTree(functionDeclaration);
        JsonNode menus = jsonNode.at("/parameters/properties/menus");
        JsonNode menuProperties = menus.at("/items/properties");
        JsonNode timeRequired = menuProperties.get("timeRequired");
        JsonNode recommendationType = menuProperties.get("recommendationType");

        // then
        assertThat(menus.get("type").asText()).isEqualTo("array");
        assertThat(menus.at("/items/type").asText()).isEqualTo("object");
        assertThat(timeRequired.has("enum")).isFalse();
        assertThat(timeRequired.get("minimum").asInt()).isEqualTo(1);
        assertThat(timeRequired.get("maximum").asInt()).isEqualTo(1_440);
        assertThat(recommendationType.get("enum").isArray()).isTrue();
        assertThat(menus.at("/items/required").toString())
                .contains("menuName", "menuType", "recommendationType", "timeRequired");
    }
}
