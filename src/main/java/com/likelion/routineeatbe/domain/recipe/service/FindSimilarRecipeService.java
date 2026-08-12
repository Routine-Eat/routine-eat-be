package com.likelion.routineeatbe.domain.recipe.service;

import com.likelion.routineeatbe.domain.recipe.dto.RecipeWithSimilarRecipes;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.exception.RecipeErrorCode;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.repository.RecipeFoodIngredientRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindSimilarRecipeService {

    private static final int MAX_SIMILAR_RECIPE_COUNT = 3;
    private static final int MAX_INGREDIENT_DIFFERENCE = 4;

    private final RecipeRepository recipeRepository;
    private final RecipeFoodIngredientRepository recipeFoodIngredientRepository;

    /**
     * 대상 레시피와 재료 구성이 유사한 레시피를 함께 조회합니다.
     * - 재료 차이가 0인 후보부터 조회하고, 후보가 부족하면 차이 1부터 4까지 범위를 확장합니다.
     * - 최종 유사 레시피는 최대 3개이며 재료 차이가 4를 초과하는 후보는 포함하지 않습니다.
     *
     * @param recipeId 대상 레시피 PK
     * @return 대상 레시피와 단계적으로 선정한 유사 레시피 목록
     */
    @Transactional(readOnly = true)
    public RecipeWithSimilarRecipes findRecipeWithSimilarRecipes(Long recipeId) {
        log.info(
                "[FindSimilarRecipeService] 대상 및 유사 레시피 조회 | findRecipeWithSimilarRecipes() - START | recipeId: {}",
                recipeId
        );

        /*
            1. 대상 레시피 조회
            - 레시피와 메뉴를 함께 조회하고 존재하지 않으면 RECIPE_NOT_FOUND 예외를 발생시킵니다.
         */
        Recipe recipe = recipeRepository.findByIdWithMenu(recipeId)
                .orElseThrow(() -> new CustomException(RecipeErrorCode.RECIPE_NOT_FOUND));

        /*
            2. 대상 레시피의 음식 재료 집합 조회
            - 후보 레시피와의 대칭 차집합 크기를 계산할 수 있도록 대상 음식 재료 PK를 집합으로 변환합니다.
         */
        Set<Long> targetFoodIngredientIds = new HashSet<>(
                recipeFoodIngredientRepository.findFoodIngredientIdsByRecipeId(recipe.getId())
        );

        /*
            3. 유사 레시피 후보 단계적 조회
            - 정확한 차이값 0부터 4까지 순차 조회하며 남은 개수만 Repository에 요청합니다.
         */
        List<Recipe> similarRecipes = new ArrayList<>();
        for (int ingredientDifference = 0;
             ingredientDifference <= MAX_INGREDIENT_DIFFERENCE
                     && similarRecipes.size() < MAX_SIMILAR_RECIPE_COUNT;
             ingredientDifference++) {
            int remainingCount = MAX_SIMILAR_RECIPE_COUNT - similarRecipes.size();
            recipeRepository.findRecipeCandidatesByExactIngredientDifference(
                            recipe.getId(),
                            targetFoodIngredientIds,
                            ingredientDifference,
                            remainingCount
                    ).stream()
                    .limit(remainingCount)
                    .forEach(similarRecipes::add);
        }

        /*
            4. 대상 및 유사 레시피 조회 결과 조합
            - 변경 불가능한 목록으로 변환하여 상세 조회 서비스에 전달합니다.
         */
        RecipeWithSimilarRecipes result = RecipeWithSimilarRecipes.create(recipe, similarRecipes);

        log.info(
                "[FindSimilarRecipeService] 대상 및 유사 레시피 조회 | findRecipeWithSimilarRecipes() - END | recipeId: {}, similarRecipeCount: {}",
                recipeId,
                result.similarRecipes().size()
        );
        return result;
    }
}
