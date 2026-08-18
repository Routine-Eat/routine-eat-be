package com.likelion.routineeatbe.domain.recipe.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeDetailReqDto;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeKeywordSearchReqDto;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeDetailResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeIngredientUsageListResponseDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeKeywordSearchResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeSearchResponseDto;
import com.likelion.routineeatbe.domain.recipe.service.RecipeService;
import com.likelion.routineeatbe.global.response.CursorSliceResponse;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest {

    @InjectMocks
    private RecipeController recipeController;

    @Mock
    private RecipeService recipeService;

    @Test
    @DisplayName("레시피 상세 조회 API 201 응답 성공")
    void 레시피_상세_조회_API_201_응답_성공() {
        // given
        RecipeDetailReqDto request = new RecipeDetailReqDto("1234", null);
        RecipeDetailResDto serviceResult = RecipeDetailResDto.builder()
                .recipeId(1L)
                .recipeName("계란 야채 볶음밥")
                .foodIngredientUsingPercent(60L)
                .servings(1)
                .foodIngredients(List.of())
                .additionalFoodIngredients(List.of())
                .similarRecipes(List.of())
                .build();
        given(recipeService.getRecipeDetail(1L, request)).willReturn(serviceResult);

        // when
        ResponseEntity<GlobalResponse<RecipeDetailResDto>> response =
                recipeController.getRecipeDetail(1L, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getCode()).isEqualTo(201);
        assertThat(response.getBody().getMessage()).isEqualTo("레시피 상세 조회에 성공했습니다.");
        assertThat(response.getBody().getData()).isEqualTo(serviceResult);
        assertThat(response.getBody().getData().foodIngredientUsingPercent()).isEqualTo(60L);
        assertThat(response.getBody().getData().servings()).isEqualTo(1);
    }

    @Test
    @DisplayName("전체 레시피 목록 조회 API 201 응답 성공")
    void 전체_레시피_목록_조회_API_201_응답_성공() {
        // given
        RecipeSearchRequestDto request = new RecipeSearchRequestDto(
                "1234", null, null, null, null, null, null
        );
        RecipeIngredientUsageListResponseDto usageRecipe =
                RecipeIngredientUsageListResponseDto.builder()
                        .recipeId(10L)
                        .menuName("감자 요리")
                        .foodIngredientUsingPercent(50L)
                        .requiredIngredientCost(2500L)
                        .build();
        CursorSliceResponse<RecipeIngredientUsageListResponseDto> usageSlice =
                CursorSliceResponse.<RecipeIngredientUsageListResponseDto>builder()
                        .content(List.of(usageRecipe))
                        .size(10)
                        .hasNext(true)
                        .nextCursor(11L)
                        .build();
        RecipeSearchResponseDto serviceResult = RecipeSearchResponseDto.create(
                usageSlice, usageSlice, usageSlice, usageSlice
        );
        given(recipeService.getRecipes(request)).willReturn(serviceResult);

        // when
        ResponseEntity<GlobalResponse<RecipeSearchResponseDto>> response =
                recipeController.getRecipes(request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getCode()).isEqualTo(201);
        assertThat(response.getBody().getMessage()).isEqualTo("전체 레시피 조회에 성공했습니다.");
        assertThat(response.getBody().getData().defaultRecipe().content())
                .containsExactly(usageRecipe);
        assertThat(response.getBody().getData().dietRecipe().content())
                .containsExactly(usageRecipe);
        assertThat(response.getBody().getData().glutenFreeRecipe().nextCursor()).isEqualTo(11L);
    }

    @Test
    @DisplayName("검색어 기반 레시피 검색 API 200 응답 성공")
    void 검색어_기반_레시피_검색_API_200_응답_성공() {
        // given
        RecipeKeywordSearchReqDto request = new RecipeKeywordSearchReqDto(
                "1234", "감자", 1L, 10
        );
        RecipeKeywordSearchResDto recipe = RecipeKeywordSearchResDto.builder()
                .recipeId(659L)
                .menuName("감자미역국")
                .foodIngredientUsingPercent(100L)
                .build();
        CursorSliceResponse<RecipeKeywordSearchResDto> serviceResult =
                CursorSliceResponse.<RecipeKeywordSearchResDto>builder()
                        .content(List.of(recipe))
                        .size(10)
                        .hasNext(true)
                        .nextCursor(11L)
                        .build();
        given(recipeService.searchRecipesByMenuName(request)).willReturn(serviceResult);

        // when
        ResponseEntity<GlobalResponse<CursorSliceResponse<RecipeKeywordSearchResDto>>> response =
                recipeController.searchRecipesByMenuName(request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage())
                .isEqualTo("주어진 검색어로 레시피 검색에 성공했습니다.");
        assertThat(response.getBody().getData().content()).containsExactly(recipe);
        assertThat(response.getBody().getData().content().getFirst().foodIngredientUsingPercent())
                .isEqualTo(100L);
        assertThat(response.getBody().getData().nextCursor()).isEqualTo(11L);
    }
}
