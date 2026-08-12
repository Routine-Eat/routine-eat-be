package com.likelion.routineeatbe.domain.recipe.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeKeywordSearchResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeListResponseDto;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecipeMapperTest {

    private final RecipeMapper recipeMapper = new RecipeMapper();

    @Test
    @DisplayName("레시피 조회 결과 응답 DTO 변환 성공")
    void 레시피_조회_결과_응답_DTO_변환_성공() {
        // given
        RecipeSearchResult result = new RecipeSearchResult(
                1L, 2L, "감자 요리", "thumbnail", 100.0, 20,
                DifficultyLevel.LEVEL_1, MenuType.KOREAN, 3L, 1L, 2L, 2500L
        );

        // when
        RecipeListResponseDto response = recipeMapper.toRecipeListResponseDto(result);

        // then
        assertThat(response.recipeId()).isEqualTo(1L);
        assertThat(response.requiredIngredientCost()).isEqualTo(2500L);
    }

    @Test
    @DisplayName("레시피 Entity 검색 결과 응답 DTO 변환 성공")
    void 레시피_Entity_검색_결과_응답_DTO_변환_성공() {
        // given
        Menu menu = Menu.builder()
                .name("감자미역국")
                .thumbnailUrl("thumbnail")
                .calory(35.4)
                .timeRequired(20)
                .difficultyLevel(DifficultyLevel.LEVEL_2)
                .type(MenuType.KOREAN)
                .build();
        Recipe recipe = Recipe.builder()
                .id(659L)
                .type(RecipeType.BASIC)
                .menu(menu)
                .build();

        // when
        RecipeKeywordSearchResDto response = recipeMapper.toRecipeKeywordSearchResDto(recipe);

        // then
        assertThat(response.recipeId()).isEqualTo(659L);
        assertThat(response.menuName()).isEqualTo("감자미역국");
        assertThat(response.calory()).isEqualTo(35.4);
        assertThat(response.difficultyLevel()).isEqualTo(DifficultyLevel.LEVEL_2);
        assertThat(response.category()).isEqualTo(MenuType.KOREAN);
    }
}
