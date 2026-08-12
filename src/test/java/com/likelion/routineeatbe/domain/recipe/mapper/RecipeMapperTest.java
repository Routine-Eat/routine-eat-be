package com.likelion.routineeatbe.domain.recipe.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeListResponseDto;
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
}
