package com.likelion.routineeatbe.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import com.likelion.routineeatbe.domain.cookingEquipment.service.CookingEquipmentBulkSearchService;
import com.likelion.routineeatbe.domain.menu.dto.response.InitMenuAndRecipeCookingEquipmentResDto;
import com.likelion.routineeatbe.domain.menu.service.gemini.InitMenuAndRecipeCookingEquipmentGeminiService;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipeCookingEquipment.service.RecipeCookingEquipmentPersistenceService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InitMenuAndRecipeCookingEquipmentServiceTest {

    @InjectMocks
    private InitMenuAndRecipeCookingEquipmentService initService;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private CookingEquipmentBulkSearchService cookingEquipmentBulkSearchService;

    @Mock
    private InitMenuAndRecipeCookingEquipmentGeminiService geminiService;

    @Mock
    private RecipeCookingEquipmentPersistenceService persistenceService;

    @Test
    @DisplayName("레시피와 조리 도구를 Gemini로 분석한 후 연결 데이터를 저장한다")
    void 레시피_조리_도구_Gemini_분석_연결_저장_성공() {
        // given
        Recipe recipe = Recipe.builder().id(1L).build();
        CookingEquipment cookingEquipment = CookingEquipment.builder()
                .id(10L)
                .name("칼")
                .build();
        Map<Long, List<Long>> equipmentIdsByRecipeId = Map.of(1L, List.of(10L));
        given(recipeRepository.findAllForCookingEquipmentInitialization())
                .willReturn(List.of(recipe));
        given(cookingEquipmentBulkSearchService.findAll())
                .willReturn(List.of(cookingEquipment));
        given(geminiService.generateCookingEquipmentIds(
                List.of(recipe),
                List.of(cookingEquipment)
        )).willReturn(equipmentIdsByRecipeId);
        given(persistenceService.saveAll(equipmentIdsByRecipeId)).willReturn(1L);

        // when
        InitMenuAndRecipeCookingEquipmentResDto result = initService.initialize();

        // then
        assertThat(result.initCount()).isEqualTo(1L);
        then(persistenceService).should().saveAll(equipmentIdsByRecipeId);
    }

    @Test
    @DisplayName("초기화 대상 레시피가 없으면 외부 호출 없이 0을 반환한다")
    void 초기화_대상_레시피_없음_외부_호출_생략_성공() {
        // given
        given(recipeRepository.findAllForCookingEquipmentInitialization()).willReturn(List.of());

        // when
        InitMenuAndRecipeCookingEquipmentResDto result = initService.initialize();

        // then
        assertThat(result.initCount()).isZero();
        then(cookingEquipmentBulkSearchService).should(never()).findAll();
        then(geminiService).shouldHaveNoInteractions();
        then(persistenceService).shouldHaveNoInteractions();
    }
}
