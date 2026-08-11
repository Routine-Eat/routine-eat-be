package com.likelion.routineeatbe.domain.recipeFoodIngredient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.repository.FoodIngredientRepository;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeFoodIngredientGeminiResponseDto.FoodIngredientNeedAmount;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.repository.MenuRepository;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.repository.RecipeFoodIngredientRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeFoodIngredientPersistenceServiceTest {

    @InjectMocks
    private RecipeFoodIngredientPersistenceService persistenceService;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private FoodIngredientRepository foodIngredientRepository;

    @Mock
    private RecipeFoodIngredientRepository recipeFoodIngredientRepository;

    @Test
    @DisplayName("Gemini 필요량을 RecipeFoodIngredient로 일괄 저장한다")
    void Gemini_필요량_RecipeFoodIngredient_일괄_저장_성공() {
        // given
        Menu menu = Menu.builder().id(1L).build();
        FoodIngredient foodIngredient = FoodIngredient.builder().id(10L).build();
        FoodIngredientNeedAmount needAmount = FoodIngredientNeedAmount.create(10L, 100.0, null);
        given(menuRepository.findAllById(any())).willReturn(List.of(menu));
        given(foodIngredientRepository.findAllById(any())).willReturn(List.of(foodIngredient));

        // when
        long result = persistenceService.saveAll(Map.of(1L, List.of(needAmount)));

        // then
        ArgumentCaptor<List<RecipeFoodIngredient>> captor = ArgumentCaptor.forClass(List.class);
        then(recipeFoodIngredientRepository).should().saveAllAndFlush(captor.capture());
        assertThat(result).isEqualTo(1L);
        assertThat(captor.getValue()).hasSize(1);
        RecipeFoodIngredient savedRecipeFoodIngredient = captor.getValue().getFirst();
        assertThat(savedRecipeFoodIngredient.getMenu()).isSameAs(menu);
        assertThat(savedRecipeFoodIngredient.getFoodIngredient()).isSameAs(foodIngredient);
        assertThat(savedRecipeFoodIngredient.getPrimaryNeedAmountValue()).isEqualTo(100.0);
        assertThat(savedRecipeFoodIngredient.getSecondaryNeedAmountValue()).isNull();
    }
}
