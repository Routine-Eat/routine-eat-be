package com.likelion.routineeatbe.domain.recipe.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.routineeatbe.global.response.CursorSliceResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecipeSearchResponseDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("남은 재료 및 추천 유형별 레시피 응답 필드 직렬화 성공")
    void 남은_재료_및_추천_유형별_레시피_응답_필드_직렬화_성공() throws Exception {
        // given
        RecipeIngredientUsageListResponseDto remainFoodIngredient =
                RecipeIngredientUsageListResponseDto.builder()
                        .recipeId(1L)
                        .foodIngredientUsingPercent(72L)
                        .requiredIngredientCost(2500L)
                        .build();
        RecipeIngredientUsageListResponseDto dietRecipe =
                RecipeIngredientUsageListResponseDto.builder()
                        .recipeId(2L)
                        .foodIngredientUsingPercent(72L)
                        .requiredIngredientCost(2500L)
                        .build();
        CursorSliceResponse<RecipeIngredientUsageListResponseDto> remainFoodIngredientSlice =
                CursorSliceResponse.<RecipeIngredientUsageListResponseDto>builder()
                        .content(List.of(remainFoodIngredient))
                        .size(10)
                        .hasNext(false)
                        .build();
        CursorSliceResponse<RecipeIngredientUsageListResponseDto> usageSlice =
                CursorSliceResponse.<RecipeIngredientUsageListResponseDto>builder()
                        .content(List.of(dietRecipe))
                        .size(10)
                        .hasNext(false)
                        .build();
        RecipeSearchResponseDto response = RecipeSearchResponseDto.create(
                "감자",
                remainFoodIngredientSlice,
                remainFoodIngredientSlice,
                usageSlice,
                usageSlice
        );

        // when
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));
        JsonNode remainFoodIngredientContent = json.path("remainFoodIngredient")
                .path("content")
                .get(0);
        JsonNode dietContent = json.path("dietRecipe").path("content").get(0);

        // then
        assertThat(json.path("remainFoodIngredientName").asText()).isEqualTo("감자");
        assertThat(json.has("defaultRecipe")).isFalse();
        assertThat(remainFoodIngredientContent.path("foodIngredientUsingPercent").asLong())
                .isEqualTo(72L);
        assertThat(remainFoodIngredientContent.has("matchedIngredientCount")).isFalse();
        assertThat(remainFoodIngredientContent.has("requiredIngredientCount")).isFalse();
        assertThat(dietContent.path("foodIngredientUsingPercent").asLong()).isEqualTo(72L);
        assertThat(dietContent.has("matchedIngredientCount")).isFalse();
        assertThat(dietContent.has("requiredIngredientCount")).isFalse();
    }

    @Test
    @DisplayName("검색어 기반 레시피 응답 음식 재료 활용률 직렬화 성공")
    void 검색어_기반_레시피_응답_음식_재료_활용률_직렬화_성공() throws Exception {
        // given
        RecipeKeywordSearchResDto response = RecipeKeywordSearchResDto.builder()
                .recipeId(659L)
                .menuName("감자미역국")
                .foodIngredientUsingPercent(100L)
                .isFavoriteRecipe(true)
                .build();

        // when
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        // then
        assertThat(json.path("foodIngredientUsingPercent").asLong()).isEqualTo(100L);
        assertThat(json.path("isFavoriteRecipe").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("레시피 상세 응답 음식 재료 활용률 직렬화 성공")
    void 레시피_상세_응답_음식_재료_활용률_직렬화_성공() throws Exception {
        // given
        RecipeDetailResDto response = RecipeDetailResDto.builder()
                .recipeId(1L)
                .foodIngredientUsingPercent(60L)
                .additionalFoodIngredientCost(1800L)
                .foodIngredients(List.of())
                .additionalFoodIngredients(List.of())
                .similarRecipes(List.of())
                .build();

        // when
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        // then
        assertThat(json.path("foodIngredientUsingPercent").asLong()).isEqualTo(60L);
        assertThat(json.has("additionalFoodIngredientCount")).isFalse();
        assertThat(json.path("additionalFoodIngredientCost").asLong()).isEqualTo(1800L);
    }
}
