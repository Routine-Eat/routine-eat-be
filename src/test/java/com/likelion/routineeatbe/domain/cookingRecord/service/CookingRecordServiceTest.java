package com.likelion.routineeatbe.domain.cookingRecord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto.GeneratedCookingStep;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingStartReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.exception.CookingRecordErrorCode;
import com.likelion.routineeatbe.domain.cookingRecord.mapper.CookingRecordMapper;
import com.likelion.routineeatbe.domain.cookingRecord.repository.CookingRecordRepository;
import com.likelion.routineeatbe.domain.cookingRecord.service.gemini.CookingStepGenerateGeminiService;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingStepStage;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.entity.RecipeStep;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeStepRepository;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CookingRecordServiceTest {

    @InjectMocks
    private CookingRecordService cookingRecordService;

    @Mock private UserRepository userRepository;
    @Mock private RecipeRepository recipeRepository;
    @Mock private RecipeStepRepository recipeStepRepository;
    @Mock private RecipeFoodIngredientRepository recipeFoodIngredientRepository;
    @Mock private CookingRecordRepository cookingRecordRepository;
    @Mock private CookingStepGenerateGeminiService geminiService;
    @Mock private CookingRecordPersistenceService persistenceService;
    @Mock private CookingRecordMapper cookingRecordMapper;

    @Test
    @DisplayName("사용자 맞춤 요리 단계를 생성하고 요리 기록을 저장한다")
    void 사용자_맞춤_요리_단계_생성_요리_기록_저장_성공() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        Recipe recipe = Recipe.builder().id(2L).menu(Menu.builder().name("볶음밥").build()).build();
        RecipeFoodIngredient ingredient = RecipeFoodIngredient.builder().build();
        RecipeStep recipeStep = RecipeStep.builder().level(1L).build();
        CookingStartReqDto request = new CookingStartReqDto(2L, 2);
        CookingStepGenerateGeminiResponseDto generated = generatedResponse();
        CookingRecord saved = CookingRecord.builder().id(3L).build();
        CookingStartResDto expected = CookingStartResDto.create(
                3L, "볶음밥", null, 10, List.of("손을 씻으세요."), 3, List.of()
        );

        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(recipeRepository.findByIdWithMenu(2L)).willReturn(Optional.of(recipe));
        given(cookingRecordRepository.existsBlockingSession(eq(1L), eq(2L), anyCollection()))
                .willReturn(false);
        given(recipeFoodIngredientRepository.findAllByRecipeIdInWithFoodIngredient(List.of(2L)))
                .willReturn(List.of(ingredient));
        given(recipeStepRepository.findAllByRecipeIdOrderByLevelAsc(2L))
                .willReturn(List.of(recipeStep));
        given(geminiService.generate(user, recipe, List.of(ingredient), List.of(recipeStep), 2))
                .willReturn(generated);
        given(persistenceService.save(1L, 2L, 2, generated)).willReturn(saved);
        given(cookingRecordMapper.toCookingStartResDto(saved, recipe, generated))
                .willReturn(expected);

        // when
        CookingStartResDto result = cookingRecordService.startCooking("1234", request);

        // then
        assertThat(result).isSameAs(expected);
        then(persistenceService).should().save(1L, 2L, 2, generated);
    }

    @Test
    @DisplayName("진행 중이거나 완료된 동일 요리가 있으면 Gemini 호출 전에 차단한다")
    void 동일_요리_활성_세션_존재_실패() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        Recipe recipe = Recipe.builder().id(2L).build();
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(recipeRepository.findByIdWithMenu(2L)).willReturn(Optional.of(recipe));
        given(cookingRecordRepository.existsBlockingSession(eq(1L), eq(2L), anyCollection()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> cookingRecordService.startCooking(
                "1234",
                new CookingStartReqDto(2L, 1)
        )).isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingRecordErrorCode.COOKING_ALREADY_STARTED));
        then(geminiService).shouldHaveNoInteractions();
        then(persistenceService).shouldHaveNoInteractions();
    }

    private CookingStepGenerateGeminiResponseDto generatedResponse() {
        return CookingStepGenerateGeminiResponseDto.create(
                List.of("손을 씻으세요."),
                List.of(
                        GeneratedCookingStep.create(1, CookingStepStage.PREPARATION, "준비", "준비", null),
                        GeneratedCookingStep.create(2, CookingStepStage.COOKING, "조리", "조리", null),
                        GeneratedCookingStep.create(3, CookingStepStage.FINISH, "완료", "완료", null)
                )
        );
    }
}
