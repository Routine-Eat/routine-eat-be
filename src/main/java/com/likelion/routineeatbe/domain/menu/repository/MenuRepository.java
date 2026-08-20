package com.likelion.routineeatbe.domain.menu.repository;

import com.likelion.routineeatbe.domain.menu.dto.MenuDifficultyCalculationDto;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    /**
     * 입력받은 메뉴명 중 DB에 이미 존재하는 메뉴명을 조회합니다.
     *
     * @param names 존재 여부를 확인할 메뉴명 목록
     * @return DB에 존재하는 메뉴명 집합
     */
    @Query("select menu.name from Menu menu where menu.name in :names")
    Set<String> findExistingNames(@Param("names") Collection<String> names);

    /**
     * 음식 재료 필요량이 아직 초기화되지 않은 메뉴를 ID 순서로 조회합니다.
     *
     * @return 음식 재료 필요량 초기화 대상 메뉴 목록
     */
    @Query("""
            select menu
            from Menu menu
            join menu.recipes recipe
                on recipe.type = com.likelion.routineeatbe.domain.recipe.enums.RecipeType.BASIC
            where not exists (
                select recipeFoodIngredient.id
                from RecipeFoodIngredient recipeFoodIngredient
                where recipeFoodIngredient.recipe = recipe
            )
            order by menu.id
            """)
    List<Menu> findAllForFoodIngredientInitialization();

    /**
     * 전체 메뉴의 난이도 계산에 필요한 기본 레시피 단계 수와 음식 재료 수를 조회합니다.
     *
     * @return 메뉴별 난이도 계산 데이터 목록
     */
    @Query("""
            select new com.likelion.routineeatbe.domain.menu.dto.MenuDifficultyCalculationDto(
                menu,
                count(distinct recipeStep.id),
                count(distinct recipeFoodIngredient.id)
            )
            from Menu menu
            left join menu.recipes recipe
                on recipe.type = com.likelion.routineeatbe.domain.recipe.enums.RecipeType.BASIC
            left join recipe.recipeSteps recipeStep
            left join RecipeFoodIngredient recipeFoodIngredient
                on recipeFoodIngredient.recipe = recipe
            group by menu
            order by menu.id
            """)
    List<MenuDifficultyCalculationDto> findAllForDifficultyLevelInitialization();
}
