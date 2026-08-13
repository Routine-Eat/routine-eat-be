package com.likelion.routineeatbe.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeCookingEquipmentGeminiFunctionDeclarationDto;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeCookingEquipmentGeminiResponseDto;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeCookingEquipmentGeminiResponseDto.RecipeCookingEquipments;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.menu.service.gemini.InitMenuAndRecipeCookingEquipmentGeminiService;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.entity.RecipeStep;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeType;
import com.likelion.routineeatbe.global.config.GeminiProperties;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import com.likelion.routineeatbe.global.util.GeminiRetryDelayStrategy;
import com.likelion.routineeatbe.global.util.GeminiUtil;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InitMenuAndRecipeCookingEquipmentGeminiServiceTest {

    @Mock
    private GeminiUtil geminiUtil;

    @Mock
    private GeminiRetryDelayStrategy retryDelayStrategy;

    private InitMenuAndRecipeCookingEquipmentGeminiService geminiService;

    @BeforeEach
    void setUp() {
        GeminiProperties properties = new GeminiProperties(
                "https://example.com/interactions",
                "test-key",
                "menu-model",
                "food-ingredient-model",
                "cooking-equipment-model",
                "cooking-step-model",
                10,
                new GeminiProperties.Retry(
                        4,
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(8),
                        0.25
                )
        );
        geminiService = new InitMenuAndRecipeCookingEquipmentGeminiService(
                geminiUtil,
                retryDelayStrategy,
                properties
        );
    }

    @Test
    @DisplayName("레시피 단계와 조리 도구로 필요한 조리 도구 식별자를 생성한다")
    void 레시피_단계_조리_도구_식별자_생성_성공() {
        // given
        Recipe recipe = createRecipe(1L);
        List<CookingEquipment> cookingEquipments = createCookingEquipments();
        given(geminiUtil.callFunction(
                eq("cooking-equipment-model"),
                anyString(),
                any(InitMenuAndRecipeCookingEquipmentGeminiFunctionDeclarationDto.class),
                eq(InitMenuAndRecipeCookingEquipmentGeminiResponseDto.class)
        )).willReturn(InitMenuAndRecipeCookingEquipmentGeminiResponseDto.create(List.of(
                RecipeCookingEquipments.create(1, List.of(10L, 20L, 10L))
        )));

        // when
        Map<Long, List<Long>> result = geminiService.generateCookingEquipmentIds(
                List.of(recipe),
                cookingEquipments
        );

        // then
        assertThat(result.get(1L)).containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("Gemini API 응답 시간이 초과되면 배치를 재시도한다")
    void Gemini_API_응답_시간_초과_배치_재시도_성공() {
        // given
        given(geminiUtil.callFunction(
                eq("cooking-equipment-model"),
                anyString(),
                any(InitMenuAndRecipeCookingEquipmentGeminiFunctionDeclarationDto.class),
                eq(InitMenuAndRecipeCookingEquipmentGeminiResponseDto.class)
        )).willThrow(new CustomException(GeminiErrorCode.API_TIMEOUT))
                .willReturn(InitMenuAndRecipeCookingEquipmentGeminiResponseDto.create(List.of(
                        RecipeCookingEquipments.create(1, List.of(10L))
                )));

        // when
        Map<Long, List<Long>> result = geminiService.generateCookingEquipmentIds(
                List.of(createRecipe(1L)),
                createCookingEquipments()
        );

        // then
        assertThat(result.get(1L)).containsExactly(10L);
        then(geminiUtil).should(times(2)).callFunction(
                eq("cooking-equipment-model"),
                anyString(),
                any(InitMenuAndRecipeCookingEquipmentGeminiFunctionDeclarationDto.class),
                eq(InitMenuAndRecipeCookingEquipmentGeminiResponseDto.class)
        );
        then(retryDelayStrategy).should().waitBeforeRetry(1);
    }

    private Recipe createRecipe(Long recipeId) {
        Menu menu = Menu.builder()
                .id(100L)
                .name("감자볶음")
                .type(MenuType.KOREAN)
                .ingredient_info_original("감자 1개, 소금 약간")
                .build();
        Recipe recipe = Recipe.builder()
                .id(recipeId)
                .type(RecipeType.BASIC)
                .menu(menu)
                .build();
        RecipeStep recipeStep = RecipeStep.builder()
                .id(1000L)
                .level(1L)
                .contents("감자를 칼로 썰고 프라이팬에 볶는다.")
                .recipe(recipe)
                .build();
        return Recipe.builder()
                .id(recipeId)
                .type(RecipeType.BASIC)
                .menu(menu)
                .recipeSteps(List.of(recipeStep))
                .build();
    }

    private List<CookingEquipment> createCookingEquipments() {
        return List.of(
                CookingEquipment.builder().id(10L).name("칼").build(),
                CookingEquipment.builder().id(20L).name("프라이팬").build()
        );
    }
}
