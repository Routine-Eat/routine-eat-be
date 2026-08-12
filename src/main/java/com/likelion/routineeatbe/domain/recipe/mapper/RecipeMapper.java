package com.likelion.routineeatbe.domain.recipe.mapper;

import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeDetailResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeIngredientResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeKeywordSearchResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeListResponseDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.SimilarRecipeResDto;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RecipeMapper {

    /**
     * 레시피 필요 재료와 계산된 필요량을 재료 응답 DTO로 변환합니다.
     * @param recipeFoodIngredient 레시피와 음식 재료 관계 Entity
     * @param primaryNeedAmountValue 주 단위 필요량
     * @param secondaryNeedAmountValue 보조 단위 필요량
     * @return 레시피 필요 재료 응답 DTO
     */
    public RecipeIngredientResDto toRecipeIngredientResDto(
            RecipeFoodIngredient recipeFoodIngredient,
            Double primaryNeedAmountValue,
            Double secondaryNeedAmountValue
    ) {
        return RecipeIngredientResDto.create(
                recipeFoodIngredient.getFoodIngredient().getId(),
                recipeFoodIngredient.getFoodIngredient().getName(),
                recipeFoodIngredient.getFoodIngredient().getType(),
                primaryNeedAmountValue,
                switch (recipeFoodIngredient.getFoodIngredient().getPrimaryUnit()) {
                    case G -> "g";
                    case ML -> "ml";
                },
                secondaryNeedAmountValue,
                recipeFoodIngredient.getFoodIngredient().getSecondaryUnit() == null
                        ? null
                        : recipeFoodIngredient.getFoodIngredient().getSecondaryUnit().getDescription()
        );
    }

    /**
     * 유사 Recipe Entity와 추가 필요 재료 개수를 유사 레시피 응답 DTO로 변환합니다.
     * @param recipe 유사 레시피 Entity
     * @param additionalFoodIngredientCount 추가로 필요한 음식 재료 개수
     * @return 유사 레시피 응답 DTO
     */
    public SimilarRecipeResDto toSimilarRecipeResDto(
            Recipe recipe,
            Long additionalFoodIngredientCount
    ) {
        return SimilarRecipeResDto.create(
                recipe.getId(),
                recipe.getMenu().getName(),
                additionalFoodIngredientCount
        );
    }

    /**
     * 레시피 Entity와 상세 조회 계산 결과를 상세 응답 DTO로 변환합니다.
     * @param recipe 대상 레시피 Entity
     * @param additionalFoodIngredientCount 추가로 필요한 음식 재료 개수
     * @param additionalFoodIngredientCost 추가로 필요한 음식 재료비
     * @param servings 인분 수
     * @param foodIngredients 필요한 전체 재료 목록
     * @param additionalFoodIngredients 추가로 필요한 재료 목록
     * @param similarRecipes 유사 레시피 목록
     * @return 레시피 상세 조회 응답 DTO
     */
    public RecipeDetailResDto toRecipeDetailResDto(
            Recipe recipe,
            Long additionalFoodIngredientCount,
            Long additionalFoodIngredientCost,
            Integer servings,
            List<RecipeIngredientResDto> foodIngredients,
            List<RecipeIngredientResDto> additionalFoodIngredients,
            List<SimilarRecipeResDto> similarRecipes
    ) {
        return RecipeDetailResDto.create(
                recipe.getId(),
                recipe.getMenu().getName(),
                recipe.getMenu().getThumbnailUrl(),
                recipe.getMenu().getTimeRequired(),
                recipe.getMenu().getDifficultyLevel(),
                additionalFoodIngredientCount,
                additionalFoodIngredientCost,
                servings,
                foodIngredients,
                additionalFoodIngredients,
                similarRecipes
        );
    }

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
