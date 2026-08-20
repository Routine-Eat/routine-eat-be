package com.likelion.routineeatbe.domain.recipe.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.menu.entity.Menu;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AiRecipeRecommendResponseTest {

    @Test
    @DisplayName("AI 레시피 추천 응답 생성 시 메뉴 썸네일 URL 매핑 성공")
    void AI_레시피_추천_응답_생성_시_메뉴_썸네일_URL_매핑_성공() {
        // given
        String thumbnailUrl = "https://example.com/menu-thumbnail.jpg";
        Menu menu = Menu.builder()
                .id(1L)
                .name("제육볶음")
                .thumbnailUrl(thumbnailUrl)
                .build();

        // when
        AiRecipeRecommendResponse response = AiRecipeRecommendResponse.from(
                menu,
                10L,
                "보유한 재료로 만들기 좋아요."
        );

        // then
        assertThat(response.menuThumbnailUrl()).isEqualTo(thumbnailUrl);
    }
}
