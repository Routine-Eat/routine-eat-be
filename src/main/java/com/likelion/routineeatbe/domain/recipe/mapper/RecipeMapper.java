package com.likelion.routineeatbe.domain.recipe.mapper;

import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeKeywordSearchResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeListResponseDto;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import org.springframework.stereotype.Component;

@Component
public class RecipeMapper {

    /**
     * 레시피 조회 결과를 목록 응답 DTO로 변환합니다.
     * @param result 레시피 정보와 사용자 재료 집계 결과
     * @return 레시피 목록 응답 DTO
     */
    public RecipeListResponseDto toRecipeListResponseDto(RecipeSearchResult result) {
        return RecipeListResponseDto.create(
                result.recipeId(),
                result.menuName(),
                result.thumbnailUrl(),
                result.calory(),
                result.timeRequired(),
                result.difficultyLevel(),
                result.category(),
                result.cookingCount(),
                result.matchedIngredientCount(),
                result.requiredIngredientCount(),
                result.requiredIngredientCost()
        );
    }

    /**
     * Recipe Entity를 검색 결과 응답 DTO로 변환합니다.
     * @param recipe 변환할 기본 레시피 Entity
     * @return 검색 결과 응답 DTO
     */
    public RecipeKeywordSearchResDto toRecipeKeywordSearchResDto(Recipe recipe) {
        return RecipeKeywordSearchResDto.create(
                recipe.getId(),
                recipe.getMenu().getName(),
                recipe.getMenu().getThumbnailUrl(),
                recipe.getMenu().getCalory(),
                recipe.getMenu().getTimeRequired(),
                recipe.getMenu().getDifficultyLevel(),
                recipe.getMenu().getType()
        );
    }
}
