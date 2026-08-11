package com.likelion.routineeatbe.domain.recipeCookingEquipment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import com.likelion.routineeatbe.domain.cookingEquipment.repository.CookingEquipmentRepository;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipeCookingEquipment.entity.RecipeCookingEquipment;
import com.likelion.routineeatbe.domain.recipeCookingEquipment.repository.RecipeCookingEquipmentRepository;
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
class RecipeCookingEquipmentPersistenceServiceTest {

    @InjectMocks
    private RecipeCookingEquipmentPersistenceService persistenceService;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private CookingEquipmentRepository cookingEquipmentRepository;

    @Mock
    private RecipeCookingEquipmentRepository recipeCookingEquipmentRepository;

    @Test
    @DisplayName("Gemini 결과를 RecipeCookingEquipment로 일괄 저장한다")
    void Gemini_결과_RecipeCookingEquipment_일괄_저장_성공() {
        // given
        Recipe recipe = Recipe.builder().id(1L).build();
        CookingEquipment cookingEquipment = CookingEquipment.builder()
                .id(10L)
                .name("칼")
                .build();
        given(recipeRepository.findAllById(any())).willReturn(List.of(recipe));
        given(cookingEquipmentRepository.findAllById(any()))
                .willReturn(List.of(cookingEquipment));

        // when
        long result = persistenceService.saveAll(Map.of(1L, List.of(10L)));

        // then
        ArgumentCaptor<List<RecipeCookingEquipment>> captor = ArgumentCaptor.forClass(List.class);
        then(recipeCookingEquipmentRepository).should().saveAllAndFlush(captor.capture());
        assertThat(result).isEqualTo(1L);
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().getFirst().getRecipe()).isSameAs(recipe);
        assertThat(captor.getValue().getFirst().getCookingEquipment())
                .isSameAs(cookingEquipment);
    }
}
