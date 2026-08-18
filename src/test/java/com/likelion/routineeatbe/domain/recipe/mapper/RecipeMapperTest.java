package com.likelion.routineeatbe.domain.recipe.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeIngredientResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeIngredientUsageListResponseDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeKeywordSearchResDto;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecipeMapperTest {

    private final RecipeMapper recipeMapper = new RecipeMapper();

    @Test
    @DisplayName("레시피 필요 재료 단위 응답 DTO 변환 성공")
    void 레시피_필요_재료_단위_응답_DTO_변환_성공() {
        // given
        Menu menu = Menu.builder().id(1L).build();
        Recipe recipe = Recipe.builder().id(2L).menu(menu).build();
        FoodIngredient foodIngredient = FoodIngredient.builder()
                .id(10L)
                .name("밥")
                .type(FoodIngredientType.GRAIN)
                .primaryUnit(PrimaryUnit.G)
                .secondaryUnit(SecondaryUnit.GONGGI)
                .build();
        RecipeFoodIngredient recipeFoodIngredient = RecipeFoodIngredient.create(
                recipe,
                foodIngredient,
                250.0,
                1.0
        );

        // when
        RecipeIngredientResDto response = recipeMapper.toRecipeIngredientResDto(
                recipeFoodIngredient,
                500.0,
                2.0
        );

        // then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.primaryNeedAmountValue()).isEqualTo(500.0);
        assertThat(response.primaryUnit()).isEqualTo("g");
        assertThat(response.secondaryNeedAmountValue()).isEqualTo(2.0);
        assertThat(response.secondaryUnit()).isEqualTo("공기");
    }

    @Test
    @DisplayName("레시피 조회 결과 음식 재료 활용률 응답 DTO 변환 성공")
    void 레시피_조회_결과_음식_재료_활용률_응답_DTO_변환_성공() {
        // given
        RecipeSearchResult result = new RecipeSearchResult(
                1L, 2L, "감자 요리", "thumbnail", 100.0, 20,
                DifficultyLevel.LEVEL_1, MenuType.KOREAN, 3L, 8L, 11L, 2500L, true
        );

        // when
        RecipeIngredientUsageListResponseDto response =
                recipeMapper.toRecipeIngredientUsageListResponseDto(result);

        // then
        assertThat(response.recipeId()).isEqualTo(1L);
        assertThat(response.foodIngredientUsingPercent()).isEqualTo(72L);
        assertThat(response.requiredIngredientCost()).isEqualTo(2500L);
        assertThat(response.isFavoriteRecipe()).isTrue();
    }

    @Test
    @DisplayName("필요 재료가 없는 레시피 음식 재료 활용률 0 반환 성공")
    void 필요_재료가_없는_레시피_음식_재료_활용률_0_반환_성공() {
        // given
        RecipeSearchResult result = new RecipeSearchResult(
                1L, 2L, "물", "thumbnail", 0.0, 1,
                DifficultyLevel.LEVEL_1, MenuType.OTHER, 0L, 0L, 0L, 0L
        );

        // when
        RecipeIngredientUsageListResponseDto response =
                recipeMapper.toRecipeIngredientUsageListResponseDto(result);

        // then
        assertThat(response.foodIngredientUsingPercent()).isZero();
    }

    @Test
    @DisplayName("레시피 조회 결과 검색 응답 DTO 변환 성공")
    void 레시피_조회_결과_검색_응답_DTO_변환_성공() {
        // given
        RecipeSearchResult result = new RecipeSearchResult(
                659L, 1L, "감자미역국", "thumbnail", 35.4, 20,
                DifficultyLevel.LEVEL_2, MenuType.KOREAN, 0L, 9L, 10L, 0L
        );

        // when
        RecipeKeywordSearchResDto response =
                recipeMapper.toRecipeKeywordSearchResDto(result);

        // then
        assertThat(response.recipeId()).isEqualTo(659L);
        assertThat(response.menuName()).isEqualTo("감자미역국");
        assertThat(response.calory()).isEqualTo(35.4);
        assertThat(response.difficultyLevel()).isEqualTo(DifficultyLevel.LEVEL_2);
        assertThat(response.category()).isEqualTo(MenuType.KOREAN);
        assertThat(response.foodIngredientUsingPercent()).isEqualTo(90L);
    }

    @Test
    @DisplayName("레시피 상세 조회 음식 재료 활용률 응답 DTO 변환 성공")
    void 레시피_상세_조회_음식_재료_활용률_응답_DTO_변환_성공() {
        // given
        Menu menu = Menu.builder()
                .name("계란 야채 볶음밥")
                .thumbnailUrl("thumbnail")
                .timeRequired(15)
                .difficultyLevel(DifficultyLevel.LEVEL_1)
                .build();
        Recipe recipe = Recipe.builder().id(1L).menu(menu).build();

        // when
        var response = recipeMapper.toRecipeDetailResDto(
                recipe,
                3L,
                5L,
                1800L,
                1,
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of()
        );

        // then
        assertThat(response.foodIngredientUsingPercent()).isEqualTo(60L);
        assertThat(response.additionalFoodIngredientCost()).isEqualTo(1800L);
    }
}
