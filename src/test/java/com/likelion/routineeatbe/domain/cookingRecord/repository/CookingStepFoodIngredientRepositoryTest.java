package com.likelion.routineeatbe.domain.cookingRecord.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecordFoodIngredient;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingStepFoodIngredient;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeType;
import com.likelion.routineeatbe.domain.user.entity.User;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cooking-step-food-test;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CookingStepFoodIngredientRepositoryTest {

    @Autowired
    private CookingRecordRepository cookingRecordRepository;

    @Autowired
    private CookingStepFoodIngredientRepository cookingStepFoodIngredientRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("요리 단계에 연결된 요리 기록 음식 재료를 PK 순으로 조회한다")
    void 단계별_요리_기록_음식_재료_조회_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("1234").build());
        Recipe recipe = persistRecipe();
        FoodIngredient greenOnion = persistFoodIngredient("대파");
        FoodIngredient egg = persistFoodIngredient("계란");
        CookingRecord cookingRecord = CookingRecord.create(user, recipe, 1);
        CookingRecordFoodIngredient greenOnionRecordFood =
                CookingRecordFoodIngredient.create(
                        cookingRecord,
                        greenOnion,
                        50.0,
                        0.5
                );
        CookingRecordFoodIngredient eggRecordFood = CookingRecordFoodIngredient.create(
                cookingRecord,
                egg,
                60.0,
                1.0
        );
        CookingSession cookingSession = CookingSession.create(cookingRecord, 2);
        CookingStep firstStep = CookingStep.create(
                cookingSession,
                1L,
                "재료 준비",
                "대파와 계란을 준비합니다.",
                null
        );
        CookingStep secondStep = CookingStep.create(
                cookingSession,
                2L,
                "계란 볶기",
                "계란을 볶습니다.",
                null
        );
        CookingStepFoodIngredient.create(firstStep, eggRecordFood);
        CookingStepFoodIngredient.create(firstStep, greenOnionRecordFood);
        CookingStepFoodIngredient.create(secondStep, eggRecordFood);
        cookingRecordRepository.saveAndFlush(cookingRecord);
        Long firstStepId = firstStep.getId();
        Long secondStepId = secondStep.getId();
        entityManager.clear();

        // when
        List<CookingStepFoodIngredient> firstStepFoodIngredients =
                cookingStepFoodIngredientRepository
                        .findAllWithCookingRecordFoodIngredientByCookingStepId(firstStepId);
        List<CookingStepFoodIngredient> secondStepFoodIngredients =
                cookingStepFoodIngredientRepository
                        .findAllWithCookingRecordFoodIngredientByCookingStepId(secondStepId);

        // then
        assertThat(firstStepFoodIngredients)
                .extracting(foodIngredient -> foodIngredient
                        .getCookingRecordFoodIngredient()
                        .getFoodIngredient()
                        .getName())
                .containsExactly("대파", "계란");
        assertThat(secondStepFoodIngredients)
                .singleElement()
                .satisfies(foodIngredient -> assertThat(foodIngredient
                        .getCookingRecordFoodIngredient()
                        .getFoodIngredient()
                        .getName()).isEqualTo("계란"));
    }

    private Recipe persistRecipe() {
        Menu menu = entityManager.persist(Menu.builder()
                .name("계란 대파 볶음밥")
                .type(MenuType.KOREAN)
                .recommendationType(RecommendationType.DEFAULT)
                .calory(300.0)
                .ingredient_info_original("계란, 대파")
                .timeRequired(10)
                .difficultyLevel(DifficultyLevel.LEVEL_1)
                .build());
        return entityManager.persist(Recipe.builder()
                .type(RecipeType.BASIC)
                .menu(menu)
                .build());
    }

    private FoodIngredient persistFoodIngredient(String name) {
        return entityManager.persist(FoodIngredient.builder()
                .name(name)
                .type(FoodIngredientType.VEGETABLE)
                .pricePerHundred(1000L)
                .primaryUnit(PrimaryUnit.G)
                .secondaryUnit(SecondaryUnit.PINCH)
                .exception(false)
                .build());
    }
}
