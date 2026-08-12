package com.likelion.routineeatbe.domain.recipe.repository;

import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
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
}
