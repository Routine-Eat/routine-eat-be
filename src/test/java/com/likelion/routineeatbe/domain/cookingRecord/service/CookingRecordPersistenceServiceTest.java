package com.likelion.routineeatbe.domain.cookingRecord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto.GeneratedCookingStep;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecordFoodIngredient;
import com.likelion.routineeatbe.domain.cookingRecord.exception.CookingRecordErrorCode;
import com.likelion.routineeatbe.domain.cookingRecord.repository.CookingRecordRepository;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingStepStage;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.repository.RecipeFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CookingRecordPersistenceServiceTest {

    @InjectMocks
    private CookingRecordPersistenceService persistenceService;

    @Mock private UserRepository userRepository;
    @Mock private RecipeRepository recipeRepository;
    @Mock private RecipeFoodIngredientRepository recipeFoodIngredientRepository;
    @Mock private CookingRecordRepository cookingRecordRepository;

    @Test
    @DisplayName("체크리스트를 level 0으로 저장하고 실제 단계 수만 세션에 기록한다")
    void 체크리스트_level_0_저장_성공() {
        // given
        User user = User.builder().id(1L).build();
        Recipe recipe = Recipe.builder().id(2L).build();
        FoodIngredient greenOnion = FoodIngredient.builder().id(10L).build();
        FoodIngredient egg = FoodIngredient.builder().id(11L).build();
        List<RecipeFoodIngredient> recipeFoodIngredients = List.of(
                RecipeFoodIngredient.builder()
                        .recipe(recipe)
                        .foodIngredient(greenOnion)
                        .primaryNeedAmountValue(50.0)
                        .secondaryNeedAmountValue(0.5)
                        .build(),
                RecipeFoodIngredient.builder()
                        .recipe(recipe)
                        .foodIngredient(egg)
                        .primaryNeedAmountValue(60.0)
                        .secondaryNeedAmountValue(null)
                        .build()
        );
        CookingStepGenerateGeminiResponseDto generated = CookingStepGenerateGeminiResponseDto.create(
                List.of("손을 씻으세요.", "도구를 확인하세요."),
                List.of(
                        GeneratedCookingStep.create(1, CookingStepStage.PREPARATION, "준비", "준비", null),
                        GeneratedCookingStep.create(2, CookingStepStage.COOKING, "조리", "조리", null),
                        GeneratedCookingStep.create(3, CookingStepStage.FINISH, "완료", "완료", null)
                )
        );
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(recipeRepository.findById(2L)).willReturn(Optional.of(recipe));
        given(cookingRecordRepository.existsBlockingSession(eq(1L), eq(2L), anyCollection()))
                .willReturn(false);
        given(recipeFoodIngredientRepository.findAllByRecipeIdInWithFoodIngredient(List.of(2L)))
                .willReturn(recipeFoodIngredients);
        given(cookingRecordRepository.saveAndFlush(any(CookingRecord.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        CookingRecord result = persistenceService.save(1L, 2L, 2, generated);

        // then
        assertThat(result.getCookingSession().getStatus())
                .isEqualTo(CookingSessionStatus.IN_PROGRESS);
        assertThat(result.getCookingSession().getCookingStepCount()).isEqualTo(3);
        assertThat(result.getCookingSession().getCookingSteps()).hasSize(5);
        assertThat(result.getCookingSession().getCookingSteps())
                .filteredOn(cookingStep -> cookingStep.getLevel() == 0L)
                .extracting(CookingStep::getContent)
                .containsExactly("손을 씻으세요.", "도구를 확인하세요.");
        assertThat(result.getFoodIngredients()).hasSize(2);
        assertThat(result.getFoodIngredients())
                .extracting(
                        CookingRecordFoodIngredient::getPrimaryUsedAmountValue,
                        CookingRecordFoodIngredient::getSecondaryUsedAmountValue
                )
                .containsExactlyInAnyOrder(
                        tuple(100.0, 1.0),
                        tuple(120.0, null)
                );
        assertThat(result.getFoodIngredients())
                .extracting(foodIngredient -> foodIngredient.getFoodIngredient().getId())
                .containsExactlyInAnyOrder(10L, 11L);
    }

    @Test
    @DisplayName("잠금 획득 후 활성 세션이 확인되면 저장을 차단한다")
    void 잠금_후_활성_세션_확인_실패() {
        // given
        User user = User.builder().id(1L).build();
        Recipe recipe = Recipe.builder().id(2L).build();
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(recipeRepository.findById(2L)).willReturn(Optional.of(recipe));
        given(cookingRecordRepository.existsBlockingSession(eq(1L), eq(2L), anyCollection()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> persistenceService.save(
                1L,
                2L,
                1,
                CookingStepGenerateGeminiResponseDto.create(List.of("확인"), List.of())
        )).isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingRecordErrorCode.COOKING_ALREADY_STARTED));
        then(cookingRecordRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("레시피 음식 재료가 없으면 요리 시작 데이터 저장에 실패한다")
    void 레시피_음식_재료_없음_저장_실패() {
        // given
        User user = User.builder().id(1L).build();
        Recipe recipe = Recipe.builder().id(2L).build();
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(recipeRepository.findById(2L)).willReturn(Optional.of(recipe));
        given(cookingRecordRepository.existsBlockingSession(eq(1L), eq(2L), anyCollection()))
                .willReturn(false);
        given(recipeFoodIngredientRepository.findAllByRecipeIdInWithFoodIngredient(List.of(2L)))
                .willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> persistenceService.save(
                1L,
                2L,
                2,
                CookingStepGenerateGeminiResponseDto.create(List.of("확인"), List.of())
        )).isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingRecordErrorCode.RECIPE_FOOD_INGREDIENT_EMPTY));
        then(cookingRecordRepository).should(never()).saveAndFlush(any(CookingRecord.class));
    }
}
