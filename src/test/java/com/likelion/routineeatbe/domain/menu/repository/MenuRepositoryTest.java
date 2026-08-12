package com.likelion.routineeatbe.domain.menu.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import com.likelion.routineeatbe.domain.menu.dto.MenuDifficultyCalculationDto;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.entity.RecipeStep;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeStepType;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeType;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest
class MenuRepositoryTest {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("기본 레시피 단계와 메뉴 음식 재료를 중복 없이 집계한다")
    void 메뉴_난이도_계산_데이터_집계_성공() {
        // given
        Menu targetMenu = persistMenu("집계 메뉴", 30);
        Menu emptyMenu = persistMenu("빈 메뉴", 10);
        Recipe basicRecipe = persistRecipe(targetMenu, RecipeType.BASIC);
        Recipe userRecipe = persistRecipe(targetMenu, RecipeType.USER);
        persistRecipeStep(basicRecipe, 1L);
        persistRecipeStep(basicRecipe, 2L);
        persistRecipeStep(userRecipe, 1L);
        FoodIngredient sharedIngredient = persistFoodIngredient("재료 1");
        persistRecipeFoodIngredient(basicRecipe, sharedIngredient);
        persistRecipeFoodIngredient(userRecipe, sharedIngredient);
        persistRecipeFoodIngredient(basicRecipe, persistFoodIngredient("재료 2"));
        entityManager.flush();
        entityManager.clear();

        // when
        List<MenuDifficultyCalculationDto> results =
                menuRepository.findAllForDifficultyLevelInitialization();

        // then
        MenuDifficultyCalculationDto targetResult = findByMenuId(results, targetMenu.getId());
        MenuDifficultyCalculationDto emptyResult = findByMenuId(results, emptyMenu.getId());
        assertThat(targetResult.recipeStepCount()).isEqualTo(2L);
        assertThat(targetResult.ingredientCount()).isEqualTo(2L);
        assertThat(emptyResult.recipeStepCount()).isZero();
        assertThat(emptyResult.ingredientCount()).isZero();
    }

    private Menu persistMenu(String name, int timeRequired) {
        return entityManager.persist(Menu.builder()
                .name(name)
                .type(MenuType.KOREAN)
                .recommendationType(RecommendationType.DEFAULT)
                .calory(100.0)
                .ingredient_info_original("재료 정보")
                .timeRequired(timeRequired)
                .difficultyLevel(DifficultyLevel.LEVEL_1)
                .build());
    }

    private Recipe persistRecipe(Menu menu, RecipeType recipeType) {
        return entityManager.persist(Recipe.builder()
                .menu(menu)
                .type(recipeType)
                .build());
    }

    private void persistRecipeStep(Recipe recipe, long level) {
        entityManager.persist(RecipeStep.builder()
                .recipe(recipe)
                .type(RecipeStepType.NORMAL)
                .level(level)
                .contents("조리 단계")
                .build());
    }

    private FoodIngredient persistFoodIngredient(String name) {
        return entityManager.persist(FoodIngredient.builder()
                .name(name)
                .type(FoodIngredientType.VEGETABLE)
                .pricePerHundred(1000L)
                .primaryUnit(PrimaryUnit.G)
                .secondaryUnit(SecondaryUnit.GAE)
                .allergy(false)
                .dislike(false)
                .build());
    }

    private void persistRecipeFoodIngredient(Recipe recipe, FoodIngredient foodIngredient) {
        entityManager.persist(RecipeFoodIngredient.create(
                recipe,
                foodIngredient,
                100.0,
                null
        ));
    }

    private MenuDifficultyCalculationDto findByMenuId(
            List<MenuDifficultyCalculationDto> results,
            Long menuId
    ) {
        return results.stream()
                .filter(result -> result.menu().getId().equals(menuId))
                .findFirst()
                .orElseThrow();
    }
}
