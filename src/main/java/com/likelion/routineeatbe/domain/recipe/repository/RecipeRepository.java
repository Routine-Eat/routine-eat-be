package com.likelion.routineeatbe.domain.recipe.repository;

import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeRepository extends JpaRepository<Recipe, Long>, RecipeRepositoryCustom {

    /**
     * 레시피 PK로 레시피와 메뉴를 함께 조회
     * @param recipeId 조회할 레시피 PK
     * @return 메뉴가 함께 조회된 레시피
     */
    @Query("""
            select recipe
            from Recipe recipe
            join fetch recipe.menu menu
            where recipe.id = :recipeId
            """)
    Optional<Recipe> findByIdWithMenu(@Param("recipeId") Long recipeId);

    /**
     * 메뉴 PK 목록에 해당하는 기본 레시피를 메뉴와 함께 조회
     * @param menuIds 조회할 메뉴 PK 목록
     * @return 메뉴가 함께 조회된 기본 레시피 목록
     */
    @Query("""
            select recipe
            from Recipe recipe
            join fetch recipe.menu menu
            where recipe.type = com.likelion.routineeatbe.domain.recipe.enums.RecipeType.BASIC
              and menu.id in :menuIds
            order by menu.id
            """)
    List<Recipe> findAllBasicByMenuIdIn(@Param("menuIds") Set<Long> menuIds);

    /**
     * 조리 도구가 아직 초기화되지 않은 기본 레시피를 메뉴 및 조리 단계와 함께 조회
     *
     * @return 조리 도구 초기화 대상 기본 레시피 목록
     */
    @Query("""
            select distinct recipe
            from Recipe recipe
            join fetch recipe.menu menu
            left join fetch recipe.recipeSteps recipeStep
            where recipe.type = com.likelion.routineeatbe.domain.recipe.enums.RecipeType.BASIC
              and not exists (
                  select recipeCookingEquipment.id
                  from RecipeCookingEquipment recipeCookingEquipment
                  where recipeCookingEquipment.recipe = recipe
              )
            order by recipe.id
            """)
    List<Recipe> findAllForCookingEquipmentInitialization();

    /**
     * AI 식단 추천의 원본 후보를 조회
     * Menu와 RecipeFoodIngredient를 fetch join하여 추천 서비스가 메뉴명과 필요 식재료를 사용할 때
     * 발생할 수 있는 N+1 조회를 방지하며 사용자 정의 레시피는 제외하고 BASIC 레시피만 반환
     */
    @Query("""
            select distinct recipe
            from Recipe recipe
            join fetch recipe.menu menu
            left join fetch recipe.recipeFoodIngredients recipeFoodIngredient
            left join fetch recipeFoodIngredient.foodIngredient
            where recipe.type = com.likelion.routineeatbe.domain.recipe.enums.RecipeType.BASIC
            order by recipe.id
            """)
    List<Recipe> findAllBasicWithMenuAndFoodIngredients();

    /**
     * 메뉴 PK 목록으로 레시피 및 필요한 식재료 정보를 한 번에 조회
     * 부족한 식재료(missingIngredients) 계산 시 N+1 조회를 방지
     */
    @Query("""
            select distinct recipe
            from Recipe recipe
            join fetch recipe.menu menu
            left join fetch recipe.recipeFoodIngredients recipeFoodIngredient
            left join fetch recipeFoodIngredient.foodIngredient
            where menu.id in :menuIds
            """)
    List<Recipe> findAllByMenu_IdIn(@Param("menuIds") List<Long> menuIds);
}