package com.likelion.routineeatbe.domain.recipe.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeSortType;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeType;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Slice;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:recipe-test;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RecipeRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("추천 유형별 레시피 조회 및 부족 재료비 계산 성공")
    void 추천_유형별_레시피_조회_및_부족_재료비_계산_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("1234").build());
        FoodIngredient potato = persistFoodIngredient("감자", 1000L);
        FoodIngredient carrot = persistFoodIngredient("당근", 2000L);
        Recipe simpleRecipe = persistRecipe("감자 요리", 10L, RecommendationType.SIMPLE);
        persistRecipe("당근 요리", 20L, RecommendationType.DIET);
        persistRequiredIngredient(simpleRecipe, potato, 100.0);
        persistRequiredIngredient(simpleRecipe, carrot, 100.0);
        entityManager.persist(UserFoodIngredient.builder()
                .user(user)
                .foodIngredient(potato)
                .relationType(UserFoodIngredientType.OWN)
                .primaryAmountValue(50.0)
                .build());
        entityManager.flush();
        entityManager.clear();

        RecipeSearchRequestDto request = createRequest(1L, 10, RecipeSortType.FOOD_INTEGRATION);

        // when
        Slice<RecipeSearchResult> result = recipeRepository.searchRecipes(
                user.getId(), request, RecommendationType.SIMPLE
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().recipeId()).isEqualTo(simpleRecipe.getId());
        assertThat(result.getContent().getFirst().matchedIngredientCount()).isEqualTo(1L);
        assertThat(result.getContent().getFirst().requiredIngredientCount()).isEqualTo(2L);
        assertThat(result.getContent().getFirst().requiredIngredientCost()).isEqualTo(2500L);
    }

    @Test
    @DisplayName("1부터 시작하는 위치 커서 레시피 조회 성공")
    void 위치_커서_레시피_조회_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("5678").build());
        Recipe firstRecipe = persistRecipe("인기 요리", 30L, RecommendationType.DEFAULT);
        Recipe secondRecipe = persistRecipe("보통 요리", 20L, RecommendationType.SIMPLE);
        Recipe thirdRecipe = persistRecipe("마지막 요리", 10L, RecommendationType.DIET);
        entityManager.flush();
        entityManager.clear();
        RecipeSearchRequestDto request = createRequest(2L, 2, RecipeSortType.DEFAULT);

        // when
        Slice<RecipeSearchResult> result = recipeRepository.searchRecipes(
                user.getId(), request, RecommendationType.DEFAULT
        );

        // then
        assertThat(firstRecipe.getId()).isNotEqualTo(secondRecipe.getId());
        assertThat(result.getContent()).extracting(RecipeSearchResult::recipeId)
                .containsExactly(secondRecipe.getId(), thirdRecipe.getId());
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("메뉴명 검색어 일치도 및 위치 커서 레시피 조회 성공")
    void 메뉴명_검색어_일치도_및_위치_커서_레시피_조회_성공() {
        // given
        Recipe exactMatch = persistRecipe("감자", 1L, RecommendationType.DEFAULT);
        Recipe shortPrefixMatch = persistRecipe("감자국", 1L, RecommendationType.DEFAULT);
        Recipe longPrefixMatch = persistRecipe("감자볶음", 100L, RecommendationType.DEFAULT);
        Recipe containsMatch = persistRecipe("매운감자국", 200L, RecommendationType.DEFAULT);
        persistRecipe("고구마국", 300L, RecommendationType.DEFAULT);
        entityManager.flush();
        entityManager.clear();

        // when
        Slice<Recipe> firstPage = recipeRepository.searchRecipesByMenuName("감자", 1L, 2);
        Slice<Recipe> secondPage = recipeRepository.searchRecipesByMenuName("감자", 3L, 2);

        // then
        assertThat(firstPage.getContent()).extracting(Recipe::getId)
                .containsExactly(exactMatch.getId(), shortPrefixMatch.getId());
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(secondPage.getContent()).extracting(Recipe::getId)
                .containsExactly(longPrefixMatch.getId(), containsMatch.getId());
        assertThat(secondPage.hasNext()).isFalse();
    }

    @Test
    @DisplayName("LIKE 특수문자를 일반 문자로 처리한 메뉴명 검색 성공")
    void LIKE_특수문자를_일반_문자로_처리한_메뉴명_검색_성공() {
        // given
        Recipe percentRecipe = persistRecipe("100% 감자", 1L, RecommendationType.DEFAULT);
        persistRecipe("감자국", 2L, RecommendationType.DEFAULT);
        entityManager.flush();
        entityManager.clear();

        // when
        Slice<Recipe> result = recipeRepository.searchRecipesByMenuName("%", 1L, 10);

        // then
        assertThat(result.getContent()).extracting(Recipe::getId)
                .containsExactly(percentRecipe.getId());
        assertThat(result.hasNext()).isFalse();
    }

    private RecipeSearchRequestDto createRequest(Long cursor, int size, RecipeSortType sortType) {
        return new RecipeSearchRequestDto(
                "1234", cursor, size, null, null, MenuType.KOREAN, sortType
        );
    }

    private Recipe persistRecipe(
            String name,
            Long cookingCount,
            RecommendationType recommendationType
    ) {
        Menu menu = entityManager.persist(Menu.builder()
                .name(name)
                .type(MenuType.KOREAN)
                .recommendationType(recommendationType)
                .calory(100.0)
                .ingredient_info_original("재료 정보")
                .timeRequired(20)
                .difficultyLevel(DifficultyLevel.LEVEL_1)
                .build());
        return entityManager.persist(Recipe.builder()
                .type(RecipeType.BASIC)
                .menu(menu)
                .cookingCount(cookingCount)
                .build());
    }

    private FoodIngredient persistFoodIngredient(String name, Long pricePerHundred) {
        return entityManager.persist(FoodIngredient.builder()
                .name(name)
                .type(FoodIngredientType.VEGETABLE)
                .pricePerHundred(pricePerHundred)
                .primaryUnit(PrimaryUnit.G)
                .secondaryUnit(SecondaryUnit.GAE)
                .allergy(false)
                .dislike(false)
                .build());
    }

    private void persistRequiredIngredient(
            Recipe recipe,
            FoodIngredient foodIngredient,
            Double requiredAmount
    ) {
        entityManager.persist(RecipeFoodIngredient.create(
                recipe,
                foodIngredient,
                requiredAmount,
                null
        ));
    }
}
