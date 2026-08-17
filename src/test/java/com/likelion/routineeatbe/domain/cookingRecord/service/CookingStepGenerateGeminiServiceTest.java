package com.likelion.routineeatbe.domain.cookingRecord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.likelion.routineeatbe.domain.cookingTip.entity.CookingTip;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiFunctionDeclarationDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto.GeneratedCookingStep;
import com.likelion.routineeatbe.domain.cookingRecord.service.gemini.CookingStepGenerateGeminiService;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingStepStage;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.entity.RecipeStep;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.SkillLevel;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.global.config.GeminiProperties;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import com.likelion.routineeatbe.global.util.GeminiRetryDelayStrategy;
import com.likelion.routineeatbe.global.util.GeminiUtil;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CookingStepGenerateGeminiServiceTest {

    @Mock
    private GeminiUtil geminiUtil;

    @Mock
    private GeminiRetryDelayStrategy retryDelayStrategy;

    private CookingStepGenerateGeminiService geminiService;

    @BeforeEach
    void setUp() {
        GeminiProperties properties = new GeminiProperties(
                "https://example.com/interactions",
                "test-key",
                "cooking-generate-model",
                "cooking-translate-model",
                "menu-model",
                "food-ingredient-model",
                "cooking-equipment-model",
                "cooking-step-model",
                10,
                new GeminiProperties.Retry(
                        2,
                        Duration.ofMillis(1),
                        Duration.ofMillis(2),
                        0.0
                )
        );
        geminiService = new CookingStepGenerateGeminiService(
                geminiUtil,
                retryDelayStrategy,
                properties
        );
    }

    @Test
    @DisplayName("초보 사용자용 부연 설명을 포함한 요리 단계를 생성한다")
    void 초보_사용자_부연_설명_요리_단계_생성_성공() {
        // given
        CookingStepGenerateGeminiResponseDto response = validResponse(true);
        given(geminiUtil.callFunction(
                eq("cooking-step-model"),
                anyString(),
                any(CookingStepGenerateGeminiFunctionDeclarationDto.class),
                eq(CookingStepGenerateGeminiResponseDto.class)
        )).willReturn(response);

        // when
        CookingStepGenerateGeminiResponseDto result = geminiService.generate(
                createUser(SkillLevel.BEGINNER),
                createRecipe(),
                List.of(createIngredient()),
                List.of(createRecipeStep()),
                List.of(createCookingTip()),
                2
        );

        // then
        assertThat(result.cookingSteps())
                .extracting(GeneratedCookingStep::subContent)
                .allMatch(subContent -> subContent != null && !subContent.isBlank());
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        then(geminiUtil).should().callFunction(
                eq("cooking-step-model"),
                promptCaptor.capture(),
                any(CookingStepGenerateGeminiFunctionDeclarationDto.class),
                eq(CookingStepGenerateGeminiResponseDto.class)
        );
        assertThat(promptCaptor.getValue())
                .contains("cookingTipId=10", "대파 써는 법");
    }

    @Test
    @DisplayName("초보 사용자 단계에 부연 설명이 없으면 재시도 후 실패한다")
    void 초보_사용자_부연_설명_누락_실패() {
        // given
        CookingStepGenerateGeminiResponseDto invalidResponse = validResponse(false);
        given(geminiUtil.callFunction(
                eq("cooking-step-model"),
                anyString(),
                any(CookingStepGenerateGeminiFunctionDeclarationDto.class),
                eq(CookingStepGenerateGeminiResponseDto.class)
        )).willReturn(invalidResponse);

        // when & then
        assertThatThrownBy(() -> geminiService.generate(
                createUser(SkillLevel.BEGINNER),
                createRecipe(),
                List.of(createIngredient()),
                List.of(createRecipeStep()),
                List.of(createCookingTip()),
                1
        )).isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(GeminiErrorCode.INVALID_COOKING_STEP_METADATA));
        then(retryDelayStrategy).should().waitBeforeRetry(1);
    }

    @Test
    @DisplayName("제공되지 않은 요리 팁 PK를 반환하면 재시도 후 실패한다")
    void 제공되지_않은_요리_팁_PK_실패() {
        // given
        CookingStepGenerateGeminiResponseDto invalidResponse = responseWithTipIds(
                true,
                List.of(999L)
        );
        given(geminiUtil.callFunction(
                eq("cooking-step-model"),
                anyString(),
                any(CookingStepGenerateGeminiFunctionDeclarationDto.class),
                eq(CookingStepGenerateGeminiResponseDto.class)
        )).willReturn(invalidResponse);

        // when & then
        assertThatThrownBy(() -> geminiService.generate(
                createUser(SkillLevel.BEGINNER),
                createRecipe(),
                List.of(createIngredient()),
                List.of(createRecipeStep()),
                List.of(createCookingTip()),
                1
        )).isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(GeminiErrorCode.INVALID_COOKING_STEP_METADATA));
        then(retryDelayStrategy).should().waitBeforeRetry(1);
    }

    private User createUser(SkillLevel skillLevel) {
        return User.builder().id(1L).skillLevel(skillLevel).build();
    }

    private Recipe createRecipe() {
        Menu menu = Menu.builder()
                .name("계란 볶음밥")
                .timeRequired(10)
                .build();
        return Recipe.builder().id(2L).menu(menu).build();
    }

    private RecipeFoodIngredient createIngredient() {
        FoodIngredient foodIngredient = FoodIngredient.builder()
                .name("계란")
                .primaryUnit(PrimaryUnit.G)
                .secondaryUnit(SecondaryUnit.AL)
                .build();
        return RecipeFoodIngredient.builder()
                .foodIngredient(foodIngredient)
                .primaryNeedAmountValue(50.0)
                .secondaryNeedAmountValue(1.0)
                .build();
    }

    private RecipeStep createRecipeStep() {
        return RecipeStep.builder().level(1L).contents("계란을 볶는다.").build();
    }

    private CookingTip createCookingTip() {
        return CookingTip.builder().id(10L).title("대파 써는 법").build();
    }

    private CookingStepGenerateGeminiResponseDto validResponse(boolean includeSubContent) {
        return responseWithTipIds(includeSubContent, List.of(10L));
    }

    private CookingStepGenerateGeminiResponseDto responseWithTipIds(
            boolean includeSubContent,
            List<Long> cookingTipIds
    ) {
        String subContent = includeSubContent ? "불을 약하게 조절하세요." : null;
        return CookingStepGenerateGeminiResponseDto.create(
                List.of("손을 씻으세요."),
                List.of(
                        GeneratedCookingStep.create(
                                1, CookingStepStage.PREPARATION, "재료 준비", "계란을 준비한다.",
                                subContent, cookingTipIds
                        ),
                        GeneratedCookingStep.create(
                                2, CookingStepStage.COOKING, "계란 볶기", "계란을 볶는다.",
                                subContent, cookingTipIds
                        ),
                        GeneratedCookingStep.create(
                                3, CookingStepStage.FINISH, "요리 종료", "불을 끈다.",
                                subContent, cookingTipIds
                        )
                )
        );
    }
}
