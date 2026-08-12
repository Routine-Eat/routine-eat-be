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
     * 레시피 PK로 레시피와 메뉴를 함께 조회합니다.
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
     * 메뉴 PK 목록에 해당하는 기본 레시피를 메뉴와 함께 조회합니다.
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
     * 조리 도구가 아직 초기화되지 않은 기본 레시피를 메뉴 및 조리 단계와 함께 조회합니다.
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
}
