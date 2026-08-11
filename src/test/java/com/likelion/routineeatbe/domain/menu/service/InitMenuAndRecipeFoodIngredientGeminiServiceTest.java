package com.likelion.routineeatbe.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeFoodIngredientGeminiResponseDto;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeFoodIngredientGeminiResponseDto.FoodIngredientNeedAmount;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeFoodIngredientGeminiResponseDto.MenuFoodIngredients;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.menu.service.gemini.InitMenuAndRecipeFoodIngredientGeminiService;
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
class InitMenuAndRecipeFoodIngredientGeminiServiceTest {

    @Mock
    private GeminiUtil geminiUtil;

    @Mock
    private GeminiRetryDelayStrategy retryDelayStrategy;

    private InitMenuAndRecipeFoodIngredientGeminiService geminiService;

    @BeforeEach
    void setUp() {
        GeminiProperties properties = new GeminiProperties(
                "https://example.com/interactions",
                "test-key",
                "menu-model",
                "food-ingredient-model",
                "cooking-equipment-model",
                10,
                new GeminiProperties.Retry(
                        4,
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(8),
                        0.25
                )
        );
        geminiService = new InitMenuAndRecipeFoodIngredientGeminiService(
                geminiUtil,
                retryDelayStrategy,
                properties
        );
    }

    @Test
    @DisplayName("메뉴와 타입별 음식 재료로 1인분 필요량을 생성한다")
    void 메뉴_타입별_음식_재료_1인분_필요량_생성_성공() {
        // given
        Menu menu = createMenu(1L);
        FoodIngredient foodIngredient = createFoodIngredient(10L);
        InitMenuAndRecipeFoodIngredientGeminiResponseDto response =
                InitMenuAndRecipeFoodIngredientGeminiResponseDto.create(List.of(
                        MenuFoodIngredients.create(
                                1,
                                List.of(FoodIngredientNeedAmount.create(10L, 150.0, 1.0))
                        )
                ));
        given(geminiUtil.callFunction(
                eq("food-ingredient-model"),
                anyString(),
                any(InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto.class),
                eq(InitMenuAndRecipeFoodIngredientGeminiResponseDto.class)
        )).willReturn(response);

        // when
        Map<Long, List<FoodIngredientNeedAmount>> result = geminiService.generateNeedAmounts(
                List.of(menu),
                Map.of(FoodIngredientType.VEGETABLE, List.of(foodIngredient))
        );

        // then
        assertThat(result.get(1L)).containsExactly(response.menus().getFirst().foodIngredients().getFirst());
        then(geminiUtil).should().callFunction(
                eq("food-ingredient-model"),
                anyString(),
                any(InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto.class),
                eq(InitMenuAndRecipeFoodIngredientGeminiResponseDto.class)
        );
    }

    @Test
    @DisplayName("Gemini가 동일한 음식 재료를 중복 반환하면 사용량을 합산한다")
    void Gemini_동일_음식_재료_중복_반환_사용량_합산_성공() {
        // given
        given(geminiUtil.callFunction(
                eq("food-ingredient-model"),
                anyString(),
                any(InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto.class),
                eq(InitMenuAndRecipeFoodIngredientGeminiResponseDto.class)
        )).willReturn(InitMenuAndRecipeFoodIngredientGeminiResponseDto.create(List.of(
                MenuFoodIngredients.create(
                        1,
                        List.of(
                                FoodIngredientNeedAmount.create(10L, 100.0, 1.0),
                                FoodIngredientNeedAmount.create(10L, 50.0, null)
                        )
                )
        )));

        // when
        Map<Long, List<FoodIngredientNeedAmount>> result = geminiService.generateNeedAmounts(
                List.of(createMenu(1L)),
                Map.of(FoodIngredientType.VEGETABLE, List.of(createFoodIngredient(10L)))
        );

        // then
        assertThat(result.get(1L)).containsExactly(
                FoodIngredientNeedAmount.create(10L, 150.0, 1.0)
        );
    }

    @Test
    @DisplayName("Gemini가 빈 음식 재료 목록을 반환하면 배치를 재시도한다")
    void Gemini_빈_음식_재료_목록_반환_배치_재시도_성공() {
        // given
        InitMenuAndRecipeFoodIngredientGeminiResponseDto emptyResponse =
                InitMenuAndRecipeFoodIngredientGeminiResponseDto.create(List.of(
                        MenuFoodIngredients.create(1, List.of())
                ));
        InitMenuAndRecipeFoodIngredientGeminiResponseDto validResponse =
                InitMenuAndRecipeFoodIngredientGeminiResponseDto.create(List.of(
                        MenuFoodIngredients.create(
                                1,
                                List.of(FoodIngredientNeedAmount.create(10L, 150.0, 1.0))
                        )
                ));
        given(geminiUtil.callFunction(
                eq("food-ingredient-model"),
                anyString(),
                any(InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto.class),
                eq(InitMenuAndRecipeFoodIngredientGeminiResponseDto.class)
        )).willReturn(emptyResponse, validResponse);

        // when
        Map<Long, List<FoodIngredientNeedAmount>> result = geminiService.generateNeedAmounts(
                List.of(createMenu(1L)),
                Map.of(FoodIngredientType.VEGETABLE, List.of(createFoodIngredient(10L)))
        );

        // then
        assertThat(result.get(1L)).containsExactly(
                FoodIngredientNeedAmount.create(10L, 150.0, 1.0)
        );
        then(geminiUtil).should(times(2)).callFunction(
                eq("food-ingredient-model"),
                anyString(),
                any(InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto.class),
                eq(InitMenuAndRecipeFoodIngredientGeminiResponseDto.class)
        );
        then(retryDelayStrategy).should().waitBeforeRetry(1);
    }

    @Test
    @DisplayName("Gemini API 응답 시간이 초과되면 배치를 재시도한다")
    void Gemini_API_응답_시간_초과_배치_재시도_성공() {
        // given
        InitMenuAndRecipeFoodIngredientGeminiResponseDto validResponse =
                InitMenuAndRecipeFoodIngredientGeminiResponseDto.create(List.of(
                        MenuFoodIngredients.create(
                                1,
                                List.of(FoodIngredientNeedAmount.create(10L, 150.0, 1.0))
                        )
                ));
        given(geminiUtil.callFunction(
                eq("food-ingredient-model"),
                anyString(),
                any(InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto.class),
                eq(InitMenuAndRecipeFoodIngredientGeminiResponseDto.class)
        )).willThrow(new CustomException(GeminiErrorCode.API_TIMEOUT))
                .willReturn(validResponse);

        // when
        Map<Long, List<FoodIngredientNeedAmount>> result = geminiService.generateNeedAmounts(
                List.of(createMenu(1L)),
                Map.of(FoodIngredientType.VEGETABLE, List.of(createFoodIngredient(10L)))
        );

        // then
        assertThat(result.get(1L)).containsExactly(
                FoodIngredientNeedAmount.create(10L, 150.0, 1.0)
        );
        then(geminiUtil).should(times(2)).callFunction(
                eq("food-ingredient-model"),
                anyString(),
                any(InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto.class),
                eq(InitMenuAndRecipeFoodIngredientGeminiResponseDto.class)
        );
        then(retryDelayStrategy).should().waitBeforeRetry(1);
    }

    @Test
    @DisplayName("Gemini가 제공되지 않은 음식 재료 ID를 반환하면 실패한다")
    void Gemini_미제공_음식_재료_ID_반환_실패() {
        // given
        given(geminiUtil.callFunction(
                eq("food-ingredient-model"),
                anyString(),
                any(InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto.class),
                eq(InitMenuAndRecipeFoodIngredientGeminiResponseDto.class)
        )).willReturn(InitMenuAndRecipeFoodIngredientGeminiResponseDto.create(List.of(
                MenuFoodIngredients.create(
                        1,
                        List.of(FoodIngredientNeedAmount.create(999L, 100.0, null))
                )
        )));

        // when & then
        assertThatThrownBy(() -> geminiService.generateNeedAmounts(
                List.of(createMenu(1L)),
                Map.of(FoodIngredientType.VEGETABLE, List.of(createFoodIngredient(10L)))
        ))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(GeminiErrorCode.INVALID_METADATA);
    }

    private Menu createMenu(Long id) {
        return Menu.builder()
                .id(id)
                .name("감자볶음")
                .type(MenuType.KOREAN)
                .ingredient_info_original("감자 1개, 소금 약간")
                .build();
    }

    private FoodIngredient createFoodIngredient(Long id) {
        return FoodIngredient.builder()
                .id(id)
                .name("감자")
                .type(FoodIngredientType.VEGETABLE)
                .primaryUnit(PrimaryUnit.G)
                .secondaryUnit(SecondaryUnit.GAE)
                .build();
    }
}
