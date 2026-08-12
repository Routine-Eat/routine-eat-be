package com.likelion.routineeatbe.domain.recipe.service;

import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeListResponseDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeSearchResponseDto;
import com.likelion.routineeatbe.domain.recipe.exception.RecipeErrorCode;
import com.likelion.routineeatbe.domain.recipe.mapper.RecipeMapper;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.response.CursorSliceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;

    /**
     * 사용자와 필터 조건을 기준으로 전체 및 추천 유형별 레시피 목록을 조회합니다.
     * - 동일한 위치 커서와 조회 크기를 네 개 목록에 적용합니다.
     * - DEFAULT는 전체, 나머지는 SIMPLE, DIET, GLUTEN_FREE 유형만 조회합니다.
     *
     * @param request 레시피 조회 조건
     * @return 전체 및 추천 유형별 레시피 목록 응답
     */
    @Transactional(readOnly = true)
    public RecipeSearchResponseDto getRecipes(RecipeSearchRequestDto request) {
        log.info(
                "[RecipeService] 전체 레시피 목록 조회 | getRecipes() - START | userNumber: {}, cursor: {}",
                request.userNumber(),
                request.cursor()
        );

        /*
            1. 사용자 조회
            - 사용자 고유 식별번호로 재료 일치도와 부족 재료비를 계산할 사용자를 조회합니다.
         */
        User user = userRepository.findByLoginNumber(request.userNumber())
                .orElseThrow(() -> new CustomException(RecipeErrorCode.USER_NOT_FOUND));

        /*
            2. 전체 및 추천 유형별 목록 조회
            - 네 목록은 동일한 필터, 정렬, 커서, 조회 크기를 공유합니다.
         */
        CursorSliceResponse<RecipeListResponseDto> defaultRecipe = getRecipeSlice(
                user.getId(), request, RecommendationType.DEFAULT
        );
        CursorSliceResponse<RecipeListResponseDto> simpleRecipe = getRecipeSlice(
                user.getId(), request, RecommendationType.SIMPLE
        );
        CursorSliceResponse<RecipeListResponseDto> dietRecipe = getRecipeSlice(
                user.getId(), request, RecommendationType.DIET
        );
        CursorSliceResponse<RecipeListResponseDto> glutenFreeRecipe = getRecipeSlice(
                user.getId(), request, RecommendationType.GLUTEN_FREE
        );

        /*
            3. 최상위 응답 조합
            - 조회한 네 CursorSliceResponse를 명세의 응답 필드에 맞게 조합합니다.
         */
        RecipeSearchResponseDto result = RecipeSearchResponseDto.create(
                defaultRecipe,
                simpleRecipe,
                dietRecipe,
                glutenFreeRecipe
        );

        log.info(
                "[RecipeService] 전체 레시피 목록 조회 | getRecipes() - END | default: {}, simple: {}, diet: {}, glutenFree: {}",
                defaultRecipe.content().size(),
                simpleRecipe.content().size(),
                dietRecipe.content().size(),
                glutenFreeRecipe.content().size()
        );
        return result;
    }

    /**
     * 하나의 추천 유형에 해당하는 레시피 Slice를 커서 응답으로 변환합니다.
     * @param userId 사용자 ID
     * @param request 레시피 조회 조건
     * @param recommendationType 조회할 추천 유형
     * @return 해당 추천 유형의 커서 기반 레시피 목록
     */
    private CursorSliceResponse<RecipeListResponseDto> getRecipeSlice(
            Long userId,
            RecipeSearchRequestDto request,
            RecommendationType recommendationType
    ) {
        log.debug(
                "[RecipeService] 추천 유형별 레시피 조회 | getRecipeSlice() - START | recommendationType: {}",
                recommendationType
        );

        Slice<RecipeSearchResult> recipeSlice = recipeRepository.searchRecipes(
                userId,
                request,
                recommendationType
        );
        Long nextCursor = recipeSlice.hasNext()
                ? request.cursor() + request.size()
                : null;
        CursorSliceResponse<RecipeListResponseDto> result = CursorSliceResponse.of(
                recipeSlice,
                recipeMapper::toRecipeListResponseDto,
                nextCursor
        );

        log.debug(
                "[RecipeService] 추천 유형별 레시피 조회 | getRecipeSlice() - END | resultSize: {}, nextCursor: {}",
                result.content().size(),
                result.nextCursor()
        );
        return result;
    }
}
