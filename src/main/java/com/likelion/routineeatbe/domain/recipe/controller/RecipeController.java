package com.likelion.routineeatbe.domain.recipe.controller;

import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeKeywordSearchReqDto;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeKeywordSearchResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeSearchResponseDto;
import com.likelion.routineeatbe.domain.recipe.service.RecipeService;
import com.likelion.routineeatbe.global.response.CursorSliceResponse;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecipeController implements RecipeControllerDocs {

    private final RecipeService recipeService;

    @Override
    public ResponseEntity<GlobalResponse<RecipeSearchResponseDto>> getRecipes(
            RecipeSearchRequestDto request
    ) {
        RecipeSearchResponseDto result = recipeService.getRecipes(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GlobalResponse.success(
                        HttpStatus.CREATED.value(),
                        "전체 레시피 조회에 성공했습니다.",
                        result
                ));
    }

    @Override
    public ResponseEntity<GlobalResponse<CursorSliceResponse<RecipeKeywordSearchResDto>>> searchRecipesByMenuName(
            RecipeKeywordSearchReqDto request
    ) {
        CursorSliceResponse<RecipeKeywordSearchResDto> result =
                recipeService.searchRecipesByMenuName(request);
        return ResponseEntity.ok(GlobalResponse.success(
                "주어진 검색어로 레시피 검색에 성공했습니다.",
                result
        ));
    }
}
