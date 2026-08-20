package com.likelion.routineeatbe.domain.recipe.mapper;

import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeDetailResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeIngredientResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeIngredientUsageListResponseDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeKeywordSearchResDto;
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
     * 유사 Recipe Entity와 추가 필요 재료 개수 및 찜 여부를 유사 레시피 응답 DTO로 변환합니다.
     * @param recipe 유사 레시피 Entity
     * @param additionalFoodIngredientCount 추가로 필요한 음식 재료 개수
     * @param isFavoriteRecipe 사용자가 찜한 레시피 여부
     * @return 유사 레시피 응답 DTO
     */
    public SimilarRecipeResDto toSimilarRecipeResDto(
            Recipe recipe,
            Long additionalFoodIngredientCount,
            String recipeThumbnailUrl,
            boolean isFavoriteRecipe
    ) {
        return SimilarRecipeResDto.create(
                recipe.getId(),
                recipe.getMenu().getName(),
                additionalFoodIngredientCount,
                recipeThumbnailUrl,
                isFavoriteRecipe
        );
    }

    /**
     * 레시피 Entity와 상세 조회 계산 결과를 상세 응답 DTO로 변환합니다.
     * @param recipe 대상 레시피 Entity
     * @param matchedIngredientCount 사용자가 보유한 필요 재료 개수
     * @param requiredIngredientCount 전체 필요 재료 개수
     * @param foodIngredientCost 레시피 조리에 필요한 전체 음식 재료비
     * @param servings 인분 수
     * @param foodIngredients 필요한 전체 재료 목록
     * @param additionalFoodIngredients 추가로 필요한 재료 목록
     * @param similarRecipes 유사 레시피 목록
     * @return 레시피 상세 조회 응답 DTO
     */
    public RecipeDetailResDto toRecipeDetailResDto(
            Recipe recipe,
            Long matchedIngredientCount,
            Long requiredIngredientCount,
            Long foodIngredientCost,
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
                calculateFoodIngredientUsingPercent(
                        matchedIngredientCount,
                        requiredIngredientCount
                ),
                foodIngredientCost,
                servings,
                foodIngredients,
                additionalFoodIngredients,
                similarRecipes
        );
    }

    /**
     * 레시피 조회 결과를 음식 재료 활용률 목록 응답 DTO로 변환합니다.
     * - 전체 필요 재료가 없으면 활용률을 0으로 반환합니다.
     * - 그 외에는 전체 필요 재료 중 사용자가 보유한 재료의 비율을 정수 백분율로 계산합니다.
     * @param result 레시피 정보와 사용자 재료 집계 결과
     * @return 음식 재료 활용률이 포함된 레시피 목록 응답 DTO
     */
    public RecipeIngredientUsageListResponseDto toRecipeIngredientUsageListResponseDto(
            RecipeSearchResult result
    ) {
        Long foodIngredientUsingPercent = calculateFoodIngredientUsingPercent(
                result.matchedIngredientCount(),
                result.requiredIngredientCount()
        );
        return RecipeIngredientUsageListResponseDto.create(
                result.recipeId(),
                result.menuName(),
                result.thumbnailUrl(),
                result.calory(),
                result.timeRequired(),
                result.difficultyLevel(),
                result.category(),
                result.cookingCount(),
                foodIngredientUsingPercent,
                result.requiredIngredientCost(),
                Boolean.TRUE.equals(result.isFavoriteRecipe())
        );
    }

    /**
     * 사용자가 보유한 필요 재료 수를 전체 필요 재료 수로 나누어 정수 백분율을 계산합니다.
     * @param matchedIngredientCount 사용자가 보유한 필요 재료 개수
     * @param requiredIngredientCount 전체 필요 재료 개수
     * @return 0부터 100 사이의 음식 재료 활용률
     */
    private Long calculateFoodIngredientUsingPercent(
            Long matchedIngredientCount,
            Long requiredIngredientCount
    ) {
        if (requiredIngredientCount == null || requiredIngredientCount == 0L) {
            return 0L;
        }
        long matchedCount = matchedIngredientCount == null ? 0L : matchedIngredientCount;
        long percent = (long) Math.floor(matchedCount * 100.0 / requiredIngredientCount);
        return Math.max(0L, Math.min(percent, 100L));
    }

    /**
     * 레시피 조회 결과를 검색 결과 응답 DTO로 변환합니다.
     * @param result 레시피 정보와 사용자 재료 집계 결과
     * @return 검색 결과 응답 DTO
     */
    public RecipeKeywordSearchResDto toRecipeKeywordSearchResDto(RecipeSearchResult result) {
        return RecipeKeywordSearchResDto.create(
                result.recipeId(),
                result.menuName(),
                result.thumbnailUrl(),
                result.calory(),
                result.timeRequired(),
                result.difficultyLevel(),
                result.category(),
                calculateFoodIngredientUsingPercent(
                        result.matchedIngredientCount(),
                        result.requiredIngredientCount()
                ),
                Boolean.TRUE.equals(result.isFavoriteRecipe())
        );
    }
}
