package com.likelion.routineeatbe.domain.recipe.repository;

import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecipeRepository extends JpaRepository<Recipe, Long>, RecipeRepositoryCustom {

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
