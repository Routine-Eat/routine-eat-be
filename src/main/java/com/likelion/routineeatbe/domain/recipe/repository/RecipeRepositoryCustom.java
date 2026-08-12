package com.likelion.routineeatbe.domain.recipe.repository;

import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import org.springframework.data.domain.Slice;

public interface RecipeRepositoryCustom {

    /**
     * 필터, 정렬 및 추천 유형 조건에 따라 기본 레시피 목록을 위치 커서 기반으로 조회합니다.
     * @param userId 재료 일치도와 부족 재료비를 계산할 사용자 ID
     * @param request 레시피 조회 조건
     * @param recommendationType Service에서 지정한 추천 유형 조건
     * @return 레시피 조회 결과 Slice
     */
    Slice<RecipeSearchResult> searchRecipes(
            Long userId,
            RecipeSearchRequestDto request,
            RecommendationType recommendationType
    );

    /**
     * 메뉴명에 검색어가 포함된 기본 레시피를 일치도 및 인기순으로 조회합니다.
     * @param searchWord 메뉴/레시피명 검색어
     * @param cursor 1부터 시작하는 조회 위치
     * @param size 한 번에 조회할 레시피 개수
     * @return 검색된 기본 레시피 Slice
     */
    Slice<Recipe> searchRecipesByMenuName(String searchWord, Long cursor, Integer size);
}
