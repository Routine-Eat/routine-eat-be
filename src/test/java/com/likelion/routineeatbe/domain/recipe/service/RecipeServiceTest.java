package com.likelion.routineeatbe.domain.recipe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeListResponseDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeSearchResponseDto;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeSortType;
import com.likelion.routineeatbe.domain.recipe.mapper.RecipeMapper;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @InjectMocks
    private RecipeService recipeService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeMapper recipeMapper;

    @Test
    @DisplayName("전체 및 추천 유형별 레시피 목록 조회 성공")
    void 전체_및_추천_유형별_레시피_목록_조회_성공() {
        // given
        RecipeSearchRequestDto request = createRequest("1234");
        User user = User.builder().id(1L).loginNumber("1234").build();
        RecipeSearchResult searchResult = new RecipeSearchResult(
                10L, 20L, "감자 요리", null, 100.0, 20,
                null, null, 3L, 1L, 2L, 2500L
        );
        RecipeListResponseDto responseDto = RecipeListResponseDto.builder()
                .recipeId(10L)
                .menuName("감자 요리")
                .requiredIngredientCost(2500L)
                .build();
        Slice<RecipeSearchResult> slice = new SliceImpl<>(
                List.of(searchResult), PageRequest.of(0, 10), true
        );

        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        for (RecommendationType type : RecommendationType.values()) {
            given(recipeRepository.searchRecipes(user.getId(), request, type)).willReturn(slice);
        }
        given(recipeMapper.toRecipeListResponseDto(searchResult)).willReturn(responseDto);

        // when
        RecipeSearchResponseDto result = recipeService.getRecipes(request);

        // then
        assertThat(result.defaultRecipe().content()).containsExactly(responseDto);
        assertThat(result.simpleRecipe().nextCursor()).isEqualTo(11L);
        assertThat(result.dietRecipe().nextCursor()).isEqualTo(11L);
        assertThat(result.glutenFreeRecipe().nextCursor()).isEqualTo(11L);
        for (RecommendationType type : RecommendationType.values()) {
            verify(recipeRepository).searchRecipes(user.getId(), request, type);
        }
    }

    @Test
    @DisplayName("존재하지 않는 사용자 전체 레시피 조회 실패")
    void 존재하지_않는_사용자_전체_레시피_조회_실패() {
        // given
        RecipeSearchRequestDto request = createRequest("9999");
        given(userRepository.findByLoginNumber("9999")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> recipeService.getRecipes(request))
                .isInstanceOf(CustomException.class);
    }

    private RecipeSearchRequestDto createRequest(String userNumber) {
        return new RecipeSearchRequestDto(
                userNumber, 1L, 10, null, null, null, RecipeSortType.DEFAULT
        );
    }
}
