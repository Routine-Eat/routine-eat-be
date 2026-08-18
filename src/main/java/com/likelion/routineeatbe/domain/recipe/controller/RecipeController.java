package com.likelion.routineeatbe.domain.recipe.controller;

import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeDetailReqDto;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeKeywordSearchReqDto;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeReRecommendRequest;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.AiRecipeRecommendResponse;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeDetailResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeKeywordSearchResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeSearchResponseDto;
import com.likelion.routineeatbe.domain.recipe.service.RecipeAiRecommendService;
import com.likelion.routineeatbe.domain.recipe.service.RecipeService;
import com.likelion.routineeatbe.domain.userSearchHistory.dto.request.UserSearchHistoryReqDto;
import com.likelion.routineeatbe.domain.userSearchHistory.dto.response.UserSearchHistoryResDto;
import com.likelion.routineeatbe.domain.userSearchHistory.service.UserSearchHistoryService;
import com.likelion.routineeatbe.global.response.CursorSliceResponse;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RecipeController implements RecipeControllerDocs {

    private final RecipeService recipeService;
    private final RecipeAiRecommendService recipeAiRecommendService;
    private final UserSearchHistoryService userSearchHistoryService;

    @Override
    public ResponseEntity<GlobalResponse<RecipeDetailResDto>> getRecipeDetail(
            Long recipeId,
            RecipeDetailReqDto request
    ) {
        RecipeDetailResDto result = recipeService.getRecipeDetail(recipeId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GlobalResponse.success(
                        HttpStatus.CREATED.value(),
                        "레시피 상세 조회에 성공했습니다.",
                        result
                ));
    }

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

    @Override
    public ResponseEntity<GlobalResponse<UserSearchHistoryResDto>> getSearchHistories(
            UserSearchHistoryReqDto request
    ) {
        UserSearchHistoryResDto result = userSearchHistoryService.getSearchHistories(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GlobalResponse.success(
                        HttpStatus.CREATED.value(),
                        "성공했습니다.",
                        result
                ));
    }

    @Override
    public GlobalResponse<AiRecipeRecommendResponse> getAiRecipeRecommendSingle(Long userId){
        AiRecipeRecommendResponse response=recipeAiRecommendService.recommendSingleRecipe(userId);
        return GlobalResponse.success(200,"레시피 단일 추천을 성공했습니다.",response);
    }

    @Override
    public GlobalResponse<List<AiRecipeRecommendResponse>> getAiRecipeRecommendThree(Long userId, RecipeReRecommendRequest request){
        List<AiRecipeRecommendResponse> responseList=recipeAiRecommendService.reRecommendRecipes(userId,request);
        return GlobalResponse.success(200,"레시피 재추천에 성공했습니다",responseList);
    }
}
