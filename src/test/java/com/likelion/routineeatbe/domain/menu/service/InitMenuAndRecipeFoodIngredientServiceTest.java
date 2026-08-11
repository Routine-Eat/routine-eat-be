package com.likelion.routineeatbe.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.foodIngredient.service.FoodIngredientBulkSearchService;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeFoodIngredientGeminiResponseDto.FoodIngredientNeedAmount;
import com.likelion.routineeatbe.domain.menu.dto.response.InitMenuAndRecipeFoodIngredientResDto;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.repository.MenuRepository;
import com.likelion.routineeatbe.domain.menu.service.gemini.InitMenuAndRecipeFoodIngredientGeminiService;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.service.RecipeFoodIngredientPersistenceService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InitMenuAndRecipeFoodIngredientServiceTest {

    @InjectMocks
    private InitMenuAndRecipeFoodIngredientService initService;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private FoodIngredientBulkSearchService foodIngredientBulkSearchService;

    @Mock
    private InitMenuAndRecipeFoodIngredientGeminiService geminiService;

    @Mock
    private RecipeFoodIngredientPersistenceService persistenceService;

    @Test
    @DisplayName("메뉴와 음식 재료를 Gemini로 분석한 후 필요량을 저장한다")
    void 메뉴_음식_재료_Gemini_분석_필요량_저장_성공() {
        // given
        Menu menu = Menu.builder().id(1L).build();
        FoodIngredient foodIngredient = FoodIngredient.builder()
                .id(10L)
                .type(FoodIngredientType.VEGETABLE)
                .build();
        FoodIngredientNeedAmount needAmount = FoodIngredientNeedAmount.create(10L, 100.0, null);
        Map<FoodIngredientType, List<FoodIngredient>> groupedIngredients =
                Map.of(FoodIngredientType.VEGETABLE, List.of(foodIngredient));
        Map<Long, List<FoodIngredientNeedAmount>> needAmountsByMenuId =
                Map.of(1L, List.of(needAmount));
        given(menuRepository.findAllForFoodIngredientInitialization()).willReturn(List.of(menu));
        given(foodIngredientBulkSearchService.findAllGroupedByType()).willReturn(groupedIngredients);
        given(geminiService.generateNeedAmounts(List.of(menu), groupedIngredients))
                .willReturn(needAmountsByMenuId);
        given(persistenceService.saveAll(needAmountsByMenuId)).willReturn(1L);

        // when
        InitMenuAndRecipeFoodIngredientResDto result = initService.initialize();

        // then
        assertThat(result.initCount()).isEqualTo(1L);
        then(persistenceService).should().saveAll(needAmountsByMenuId);
    }

    @Test
    @DisplayName("초기화 대상 메뉴가 없으면 외부 호출 없이 0을 반환한다")
    void 초기화_대상_메뉴_없음_외부_호출_생략_성공() {
        // given
        given(menuRepository.findAllForFoodIngredientInitialization()).willReturn(List.of());

        // when
        InitMenuAndRecipeFoodIngredientResDto result = initService.initialize();

        // then
        assertThat(result.initCount()).isZero();
        then(foodIngredientBulkSearchService).should(never()).findAllGroupedByType();
        then(geminiService).shouldHaveNoInteractions();
        then(persistenceService).shouldHaveNoInteractions();
    }
}
