package com.likelion.routineeatbe.domain.recipe.service;

import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeKeywordSearchReqDto;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeKeywordSearchResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeListResponseDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeSearchResponseDto;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
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
     * 사용자와 메뉴/레시피명 검색어를 기준으로 레시피 목록을 조회합니다.
     * - 사용자 고유 식별번호의 존재 여부를 확인합니다.
     * - 메뉴명 일치도 순으로 조회한 결과를 커서 기반 응답으로 변환합니다.
     *
     * @param request 사용자 식별번호, 검색어 및 커서 조회 조건
     * @return 검색어와 일치하는 레시피 커서 목록
     */
    @Transactional(readOnly = true)
    public CursorSliceResponse<RecipeKeywordSearchResDto> searchRecipesByMenuName(
            RecipeKeywordSearchReqDto request
    ) {
        log.info(
                "[RecipeService] 검색어 기반 레시피 검색 | searchRecipesByMenuName() - START | userNumber: {}, searchWord: {}, cursor: {}",
                request.userNumber(),
                request.searchWord(),
                request.cursor()
        );

        /*
            1. 사용자 존재 여부 확인
            - 사용자 고유 식별번호가 존재하지 않으면 USER_NOT_FOUND 예외를 발생시킵니다.
         */
        userRepository.findByLoginNumber(request.userNumber())
                .orElseThrow(() -> new CustomException(RecipeErrorCode.USER_NOT_FOUND));

        /*
            2. 검색어 기반 레시피 조회
            - 검색어 앞뒤 공백을 제거하고 메뉴명 일치도 순으로 기본 레시피를 조회합니다.
         */
        Slice<Recipe> recipeSlice = recipeRepository.searchRecipesByMenuName(
                        request.searchWord().strip(),
                        request.cursor(),
                        request.size()
                );

        /*
            3. 커서 기반 응답 변환
            - 다음 데이터가 존재하면 다음 조회 위치를 계산하고 Mapper로 응답 DTO를 생성합니다.
         */
        Long nextCursor = recipeSlice.hasNext()
                ? request.cursor() + request.size()
                : null;
        CursorSliceResponse<RecipeKeywordSearchResDto> result = CursorSliceResponse.of(
                recipeSlice,
                recipeMapper::toRecipeKeywordSearchResDto,
                nextCursor
        );

        log.info(
                "[RecipeService] 검색어 기반 레시피 검색 | searchRecipesByMenuName() - END | resultSize: {}, nextCursor: {}",
                result.content().size(),
                result.nextCursor()
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
