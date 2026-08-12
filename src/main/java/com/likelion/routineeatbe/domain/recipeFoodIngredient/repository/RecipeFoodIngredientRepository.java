package com.likelion.routineeatbe.domain.recipeFoodIngredient.repository;

import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeFoodIngredientRepository extends JpaRepository<RecipeFoodIngredient, Long> {

    /**
     * 레시피 PK에 연결된 음식 재료 PK를 조회합니다.
     * @param recipeId 조회할 레시피 PK
     * @return 레시피의 음식 재료 PK 목록
     */
    @Query("""
            select recipeFoodIngredient.foodIngredient.id
            from RecipeFoodIngredient recipeFoodIngredient
            where recipeFoodIngredient.recipe.id = :recipeId
            order by recipeFoodIngredient.foodIngredient.id
            """)
    List<Long> findFoodIngredientIdsByRecipeId(@Param("recipeId") Long recipeId);

    /**
     * 여러 레시피의 필요 재료를 음식 재료와 함께 조회합니다.
     * @param recipeIds 조회할 레시피 PK 목록
     * @return 레시피별 필요 재료 목록
     */
    @Query("""
            select recipeFoodIngredient
            from RecipeFoodIngredient recipeFoodIngredient
            join fetch recipeFoodIngredient.foodIngredient foodIngredient
            where recipeFoodIngredient.recipe.id in :recipeIds
            order by recipeFoodIngredient.recipe.id, foodIngredient.id
            """)
    List<RecipeFoodIngredient> findAllByRecipeIdInWithFoodIngredient(
            @Param("recipeIds") Collection<Long> recipeIds
    );
}
