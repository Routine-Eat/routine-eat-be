package com.likelion.routineeatbe.domain.foodIngredient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.foodIngredient.repository.FoodIngredientRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FoodIngredientBulkSearchServiceTest {

    @InjectMocks
    private FoodIngredientBulkSearchService foodIngredientBulkSearchService;

    @Mock
    private FoodIngredientRepository foodIngredientRepository;

    @Test
    @DisplayName("전체 음식 재료를 FoodIngredientType별로 그룹화한다")
    void 전체_음식_재료_타입별_그룹화_성공() {
        // given
        FoodIngredient potato = FoodIngredient.builder()
                .id(1L)
                .name("감자")
                .type(FoodIngredientType.POTATO_AND_STARCH)
                .build();
        FoodIngredient carrot = FoodIngredient.builder()
                .id(2L)
                .name("당근")
                .type(FoodIngredientType.VEGETABLE)
                .build();
        given(foodIngredientRepository.findAllByOrderByTypeAscIdAsc())
                .willReturn(List.of(potato, carrot));

        // when
        Map<FoodIngredientType, List<FoodIngredient>> result =
                foodIngredientBulkSearchService.findAllGroupedByType();

        // then
        assertThat(result.get(FoodIngredientType.POTATO_AND_STARCH)).containsExactly(potato);
        assertThat(result.get(FoodIngredientType.VEGETABLE)).containsExactly(carrot);
        then(foodIngredientRepository).should().findAllByOrderByTypeAscIdAsc();
    }
}
