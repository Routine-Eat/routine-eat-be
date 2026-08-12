package com.likelion.routineeatbe.domain.recipe.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeListResponseDto;
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
    @DisplayName("전체 레시피 목록 조회 API 201 응답 성공")
    void 전체_레시피_목록_조회_API_201_응답_성공() {
        // given
        RecipeSearchRequestDto request = new RecipeSearchRequestDto(
                "1234", null, null, null, null, null, null
        );
        RecipeListResponseDto recipe = RecipeListResponseDto.builder()
                .recipeId(10L)
                .menuName("감자 요리")
                .requiredIngredientCost(2500L)
                .build();
        CursorSliceResponse<RecipeListResponseDto> slice = CursorSliceResponse.<RecipeListResponseDto>builder()
                .content(List.of(recipe))
                .size(10)
                .hasNext(true)
                .nextCursor(11L)
                .build();
        RecipeSearchResponseDto serviceResult = RecipeSearchResponseDto.create(
                slice, slice, slice, slice
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
        assertThat(response.getBody().getData().defaultRecipe().content()).containsExactly(recipe);
        assertThat(response.getBody().getData().glutenFreeRecipe().nextCursor()).isEqualTo(11L);
    }
}
