package com.likelion.routineeatbe.domain.recipe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeWithSimilarRecipes;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.repository.RecipeFoodIngredientRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindSimilarRecipeServiceTest {

    @InjectMocks
    private FindSimilarRecipeService findSimilarRecipeService;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeFoodIngredientRepository recipeFoodIngredientRepository;

    @Test
    @DisplayName("재료 차이 0부터 2까지 후보를 확장한 유사 레시피 조회 성공")
    void 재료_차이_0부터_2까지_후보를_확장한_유사_레시피_조회_성공() {
        // given
        Menu targetMenu = Menu.builder().id(10L).build();
        Recipe targetRecipe = Recipe.builder().id(1L).menu(targetMenu).build();
        Recipe exactRecipe = Recipe.builder().id(2L).build();
        Recipe differenceOneRecipe = Recipe.builder().id(3L).build();
        Recipe differenceTwoRecipe = Recipe.builder().id(4L).build();
        Set<Long> targetFoodIngredientIds = Set.of(100L, 200L);

        given(recipeRepository.findByIdWithMenu(1L)).willReturn(Optional.of(targetRecipe));
        given(recipeFoodIngredientRepository.findFoodIngredientIdsByRecipeId(1L))
                .willReturn(List.copyOf(targetFoodIngredientIds));
        given(recipeRepository.findRecipeCandidatesByExactIngredientDifference(
                1L, targetFoodIngredientIds, 0, 3
        )).willReturn(List.of(exactRecipe));
        given(recipeRepository.findRecipeCandidatesByExactIngredientDifference(
                1L, targetFoodIngredientIds, 1, 2
        )).willReturn(List.of(differenceOneRecipe));
        given(recipeRepository.findRecipeCandidatesByExactIngredientDifference(
                1L, targetFoodIngredientIds, 2, 1
        )).willReturn(List.of(differenceTwoRecipe));

        // when
        RecipeWithSimilarRecipes result =
                findSimilarRecipeService.findRecipeWithSimilarRecipes(1L);

        // then
        assertThat(result.recipe()).isEqualTo(targetRecipe);
        assertThat(result.similarRecipes()).containsExactly(
                exactRecipe,
                differenceOneRecipe,
                differenceTwoRecipe
        );
        verify(recipeRepository).findRecipeCandidatesByExactIngredientDifference(
                1L, targetFoodIngredientIds, 0, 3
        );
        verify(recipeRepository).findRecipeCandidatesByExactIngredientDifference(
                1L, targetFoodIngredientIds, 1, 2
        );
        verify(recipeRepository).findRecipeCandidatesByExactIngredientDifference(
                1L, targetFoodIngredientIds, 2, 1
        );
    }

    @Test
    @DisplayName("재료 차이 4까지 후보를 확장한 유사 레시피 조회 성공")
    void 재료_차이_4까지_후보를_확장한_유사_레시피_조회_성공() {
        // given
        Menu targetMenu = Menu.builder().id(10L).build();
        Recipe targetRecipe = Recipe.builder().id(1L).menu(targetMenu).build();
        Recipe differenceThreeRecipe = Recipe.builder().id(3L).build();
        Recipe differenceFourRecipe = Recipe.builder().id(4L).build();
        Set<Long> targetFoodIngredientIds = Set.of(100L, 200L, 300L, 400L);

        given(recipeRepository.findByIdWithMenu(1L)).willReturn(Optional.of(targetRecipe));
        given(recipeFoodIngredientRepository.findFoodIngredientIdsByRecipeId(1L))
                .willReturn(List.copyOf(targetFoodIngredientIds));
        given(recipeRepository.findRecipeCandidatesByExactIngredientDifference(
                1L, targetFoodIngredientIds, 0, 3
        )).willReturn(List.of());
        given(recipeRepository.findRecipeCandidatesByExactIngredientDifference(
                1L, targetFoodIngredientIds, 1, 3
        )).willReturn(List.of());
        given(recipeRepository.findRecipeCandidatesByExactIngredientDifference(
                1L, targetFoodIngredientIds, 2, 3
        )).willReturn(List.of());
        given(recipeRepository.findRecipeCandidatesByExactIngredientDifference(
                1L, targetFoodIngredientIds, 3, 3
        )).willReturn(List.of(differenceThreeRecipe));
        given(recipeRepository.findRecipeCandidatesByExactIngredientDifference(
                1L, targetFoodIngredientIds, 4, 2
        )).willReturn(List.of(differenceFourRecipe));

        // when
        RecipeWithSimilarRecipes result =
                findSimilarRecipeService.findRecipeWithSimilarRecipes(1L);

        // then
        assertThat(result.similarRecipes()).containsExactly(
                differenceThreeRecipe,
                differenceFourRecipe
        );
        verify(recipeRepository).findRecipeCandidatesByExactIngredientDifference(
                1L, targetFoodIngredientIds, 3, 3
        );
        verify(recipeRepository).findRecipeCandidatesByExactIngredientDifference(
                1L, targetFoodIngredientIds, 4, 2
        );
    }

    @Test
    @DisplayName("재료 차이 0 후보가 충분하면 추가 후보 조회 생략 성공")
    void 재료_차이_0_후보가_충분하면_추가_후보_조회_생략_성공() {
        // given
        Menu targetMenu = Menu.builder().id(10L).build();
        Recipe targetRecipe = Recipe.builder().id(1L).menu(targetMenu).build();
        Set<Long> targetFoodIngredientIds = Set.of(100L);
        List<Recipe> exactRecipes = List.of(
                Recipe.builder().id(2L).build(),
                Recipe.builder().id(3L).build(),
                Recipe.builder().id(4L).build()
        );

        given(recipeRepository.findByIdWithMenu(1L)).willReturn(Optional.of(targetRecipe));
        given(recipeFoodIngredientRepository.findFoodIngredientIdsByRecipeId(1L))
                .willReturn(List.copyOf(targetFoodIngredientIds));
        given(recipeRepository.findRecipeCandidatesByExactIngredientDifference(
                1L, targetFoodIngredientIds, 0, 3
        )).willReturn(exactRecipes);

        // when
        RecipeWithSimilarRecipes result =
                findSimilarRecipeService.findRecipeWithSimilarRecipes(1L);

        // then
        assertThat(result.similarRecipes()).containsExactlyElementsOf(exactRecipes);
        verify(recipeRepository, never()).findRecipeCandidatesByExactIngredientDifference(
                eq(1L), eq(targetFoodIngredientIds), eq(1), anyInt()
        );
        verify(recipeRepository, never()).findRecipeCandidatesByExactIngredientDifference(
                eq(1L), eq(targetFoodIngredientIds), eq(2), anyInt()
        );
        verify(recipeRepository, never()).findRecipeCandidatesByExactIngredientDifference(
                eq(1L), eq(targetFoodIngredientIds), eq(3), anyInt()
        );
        verify(recipeRepository, never()).findRecipeCandidatesByExactIngredientDifference(
                eq(1L), eq(targetFoodIngredientIds), eq(4), anyInt()
        );
    }

    @Test
    @DisplayName("대상 레시피 조회 실패 - 존재하지 않는 레시피")
    void 대상_레시피_조회_실패_존재하지_않는_레시피() {
        // given
        given(recipeRepository.findByIdWithMenu(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findSimilarRecipeService.findRecipeWithSimilarRecipes(999L))
                .isInstanceOf(CustomException.class);
        verify(recipeRepository).findByIdWithMenu(999L);
    }
}
