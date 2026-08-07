package com.likelion.routineeatbe.domain.menu.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MenuAndRecipeCrawlingDtoTest {

    @Test
    @DisplayName("조리 설명과 이미지를 원본 순서대로 변환한다")
    void createRecipeRowList_manualAndImagesInOrder_success() {
        // given
        FoodSafetyKoreaRecipeApiResponseDto.RecipeRow row = FoodSafetyKoreaRecipeApiResponseDto.RecipeRow.builder()
                .manual01("첫 번째 설명")
                .manualImage01("https://example.com/1.png")
                .manual02("두 번째 설명")
                .manualImage02("https://example.com/2.png")
                .manual20("마지막 설명")
                .manualImage20("https://example.com/20.png")
                .build();

        // when
        List<MenuAndRecipeCrawlingDto.RecipeRow> result = MenuAndRecipeCrawlingDto.createRecipeRowList(row);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).contents()).isEqualTo("첫 번째 설명");
        assertThat(result.get(1).contents()).isEqualTo("두 번째 설명");
        assertThat(result.get(2).contents()).isEqualTo("마지막 설명");
        assertThat(result.get(2).imageUrl()).isEqualTo("https://example.com/20.png");
    }

    @Test
    @DisplayName("설명 없이 이미지만 존재하는 단계는 포함하지 않는다")
    void createRecipeRowList_imageOnly_excluded() {
        // given
        FoodSafetyKoreaRecipeApiResponseDto.RecipeRow row = FoodSafetyKoreaRecipeApiResponseDto.RecipeRow.builder()
                .manualImage01("https://example.com/1.png")
                .build();

        // when
        List<MenuAndRecipeCrawlingDto.RecipeRow> result = MenuAndRecipeCrawlingDto.createRecipeRowList(row);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이미지 없이 설명만 존재하는 단계는 포함한다")
    void createRecipeRowList_descriptionOnly_included() {
        // given
        FoodSafetyKoreaRecipeApiResponseDto.RecipeRow row = FoodSafetyKoreaRecipeApiResponseDto.RecipeRow.builder()
                .manual01("설명만 있는 단계")
                .build();

        // when
        List<MenuAndRecipeCrawlingDto.RecipeRow> result = MenuAndRecipeCrawlingDto.createRecipeRowList(row);

        // then
        assertThat(result).singleElement()
                .satisfies(recipeRow -> {
                    assertThat(recipeRow.contents()).isEqualTo("설명만 있는 단계");
                    assertThat(recipeRow.imageUrl()).isNull();
                });
    }

    @Test
    @DisplayName("모든 조리 설명이 비어 있으면 빈 리스트를 반환한다")
    void createRecipeRowList_allDescriptionsEmpty_returnsEmptyList() {
        // given
        FoodSafetyKoreaRecipeApiResponseDto.RecipeRow row = FoodSafetyKoreaRecipeApiResponseDto.RecipeRow.builder()
                .manual01(" ")
                .manualImage01("https://example.com/1.png")
                .build();

        // when
        List<MenuAndRecipeCrawlingDto.RecipeRow> result = MenuAndRecipeCrawlingDto.createRecipeRowList(row);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("식품안전나라 API의 모든 row를 크롤링 DTO 목록으로 변환한다")
    void fromFoodSafetyKoreaRecipeApiResponseDtoToList_allRows_success() {
        // given
        FoodSafetyKoreaRecipeApiResponseDto.RecipeRow firstRow = FoodSafetyKoreaRecipeApiResponseDto.RecipeRow.builder()
                .menuName("첫 번째 메뉴")
                .calories("100")
                .ingredientDetails("첫 번째 재료")
                .manual01("첫 번째 조리법")
                .build();
        FoodSafetyKoreaRecipeApiResponseDto.RecipeRow secondRow = FoodSafetyKoreaRecipeApiResponseDto.RecipeRow.builder()
                .menuName("두 번째 메뉴")
                .calories("200")
                .ingredientDetails("두 번째 재료")
                .manual01("두 번째 조리법")
                .build();
        FoodSafetyKoreaRecipeApiResponseDto response = FoodSafetyKoreaRecipeApiResponseDto.builder()
                .cookRecipeData(FoodSafetyKoreaRecipeApiResponseDto.CookRecipeData.builder()
                        .rows(List.of(firstRow, secondRow))
                        .build())
                .build();

        // when
        List<MenuAndRecipeCrawlingDto> result = MenuAndRecipeCrawlingDto
                .fromFoodSafetyKoreaRecipeApiResponseDtoToList(response);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).menuName()).isEqualTo("첫 번째 메뉴");
        assertThat(result.get(0).recipes()).hasSize(1);
        assertThat(result.get(1).calories()).isEqualTo(200);
    }
}
