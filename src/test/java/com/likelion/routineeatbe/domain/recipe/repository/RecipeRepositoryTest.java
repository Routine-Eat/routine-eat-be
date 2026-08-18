package com.likelion.routineeatbe.domain.recipe.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import com.likelion.routineeatbe.domain.favoriteRecipe.entity.FavoriteRecipe;
import com.likelion.routineeatbe.domain.favoriteRecipe.repository.FavoriteRecipeRepository;
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
import java.util.List;
import java.util.Set;
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
    private FavoriteRecipeRepository favoriteRecipeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("재료 추가 제거 교체 기준 차이별 레시피 후보 조회 성공")
    void 재료_추가_제거_교체_기준_차이별_레시피_후보_조회_성공() {
        // given
        FoodIngredient ingredientA = persistFoodIngredient("재료A", 100L);
        FoodIngredient ingredientB = persistFoodIngredient("재료B", 100L);
        FoodIngredient ingredientC = persistFoodIngredient("재료C", 100L);
        FoodIngredient ingredientD = persistFoodIngredient("재료D", 100L);
        Recipe targetRecipe = persistRecipe("대상 요리", 0L, RecommendationType.DEFAULT);
        Recipe exactRecipe = persistRecipe("동일 재료 요리", 30L, RecommendationType.DEFAULT);
        Recipe oneRemovedRecipe = persistRecipe("한 개 제거 요리", 30L, RecommendationType.DEFAULT);
        Recipe oneReplacedRecipe = persistRecipe("한 개 교체 요리", 20L, RecommendationType.DEFAULT);
        Recipe twoReplacedRecipe = persistRecipe(
                "두 개 교체 요리", 10L, RecommendationType.DEFAULT
        );

        persistRequiredIngredient(targetRecipe, ingredientA, 100.0);
        persistRequiredIngredient(targetRecipe, ingredientB, 100.0);
        persistRequiredIngredient(exactRecipe, ingredientA, 100.0);
        persistRequiredIngredient(exactRecipe, ingredientB, 100.0);
        persistRequiredIngredient(oneRemovedRecipe, ingredientA, 100.0);
        persistRequiredIngredient(oneReplacedRecipe, ingredientA, 100.0);
        persistRequiredIngredient(oneReplacedRecipe, ingredientC, 100.0);
        persistRequiredIngredient(twoReplacedRecipe, ingredientC, 100.0);
        persistRequiredIngredient(twoReplacedRecipe, ingredientD, 100.0);
        entityManager.flush();
        entityManager.clear();

        Set<Long> targetFoodIngredientIds = Set.of(ingredientA.getId(), ingredientB.getId());

        // when
        List<Recipe> differenceZero =
                recipeRepository.findRecipeCandidatesByExactIngredientDifference(
                        targetRecipe.getId(),
                        targetFoodIngredientIds,
                        0,
                        3
                );
        List<Recipe> differenceOne =
                recipeRepository.findRecipeCandidatesByExactIngredientDifference(
                        targetRecipe.getId(),
                        targetFoodIngredientIds,
                        1,
                        3
                );
        List<Recipe> differenceTwo =
                recipeRepository.findRecipeCandidatesByExactIngredientDifference(
                        targetRecipe.getId(),
                        targetFoodIngredientIds,
                        2,
                        3
                );

        // then
        assertThat(differenceZero).extracting(Recipe::getId)
                .containsExactly(exactRecipe.getId());
        assertThat(differenceOne).extracting(Recipe::getId)
                .containsExactly(oneRemovedRecipe.getId(), oneReplacedRecipe.getId());
        assertThat(differenceTwo).extracting(Recipe::getId)
                .containsExactly(twoReplacedRecipe.getId());
    }

    @Test
    @DisplayName("대상 레시피에 재료가 없는 경우 재료 차이별 후보 조회 성공")
    void 대상_레시피에_재료가_없는_경우_재료_차이별_후보_조회_성공() {
        // given
        FoodIngredient ingredient = persistFoodIngredient("단일 재료", 100L);
        Recipe targetRecipe = persistRecipe("재료 없는 대상", 0L, RecommendationType.DEFAULT);
        Recipe emptyRecipe = persistRecipe("재료 없는 후보", 10L, RecommendationType.DEFAULT);
        Recipe oneIngredientRecipe = persistRecipe("재료 한 개 후보", 20L, RecommendationType.DEFAULT);
        persistRequiredIngredient(oneIngredientRecipe, ingredient, 100.0);
        entityManager.flush();
        entityManager.clear();

        // when
        List<Recipe> differenceZero =
                recipeRepository.findRecipeCandidatesByExactIngredientDifference(
                        targetRecipe.getId(),
                        Set.of(),
                        0,
                        3
                );
        List<Recipe> differenceOne =
                recipeRepository.findRecipeCandidatesByExactIngredientDifference(
                        targetRecipe.getId(),
                        Set.of(),
                        1,
                        3
                );

        // then
        assertThat(differenceZero).extracting(Recipe::getId)
                .containsExactly(emptyRecipe.getId());
        assertThat(differenceOne).extracting(Recipe::getId)
                .containsExactly(oneIngredientRecipe.getId());
    }

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
        User user = entityManager.persist(User.builder().loginNumber("7777").build());
        FoodIngredient potato = persistFoodIngredient("검색 감자", 1000L);
        Recipe exactMatch = persistRecipe("감자", 1L, RecommendationType.DEFAULT);
        Recipe shortPrefixMatch = persistRecipe("감자국", 1L, RecommendationType.DEFAULT);
        Recipe longPrefixMatch = persistRecipe("감자볶음", 100L, RecommendationType.DEFAULT);
        Recipe containsMatch = persistRecipe("매운감자국", 200L, RecommendationType.DEFAULT);
        persistRecipe("고구마국", 300L, RecommendationType.DEFAULT);
        persistRequiredIngredient(exactMatch, potato, 100.0);
        entityManager.persist(UserFoodIngredient.builder()
                .user(user)
                .foodIngredient(potato)
                .relationType(UserFoodIngredientType.OWN)
                .primaryAmountValue(100.0)
                .build());
        entityManager.flush();
        entityManager.clear();

        // when
        Slice<RecipeSearchResult> firstPage = recipeRepository.searchRecipesByMenuName(
                user.getId(), "감자", 1L, 2
        );
        Slice<RecipeSearchResult> secondPage = recipeRepository.searchRecipesByMenuName(
                user.getId(), "감자", 3L, 2
        );

        // then
        assertThat(firstPage.getContent()).extracting(RecipeSearchResult::recipeId)
                .containsExactly(exactMatch.getId(), shortPrefixMatch.getId());
        assertThat(firstPage.getContent().getFirst().matchedIngredientCount()).isEqualTo(1L);
        assertThat(firstPage.getContent().getFirst().requiredIngredientCount()).isEqualTo(1L);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(secondPage.getContent()).extracting(RecipeSearchResult::recipeId)
                .containsExactly(longPrefixMatch.getId(), containsMatch.getId());
        assertThat(secondPage.hasNext()).isFalse();
    }

    @Test
    @DisplayName("LIKE 특수문자를 일반 문자로 처리한 메뉴명 검색 성공")
    void LIKE_특수문자를_일반_문자로_처리한_메뉴명_검색_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("8888").build());
        Recipe percentRecipe = persistRecipe("100% 감자", 1L, RecommendationType.DEFAULT);
        persistRecipe("감자국", 2L, RecommendationType.DEFAULT);
        entityManager.flush();
        entityManager.clear();

        // when
        Slice<RecipeSearchResult> result = recipeRepository.searchRecipesByMenuName(
                user.getId(), "%", 1L, 10
        );

        // then
        assertThat(result.getContent()).extracting(RecipeSearchResult::recipeId)
                .containsExactly(percentRecipe.getId());
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("찜한 레시피를 최신 찜순으로 조회하고 부족 재료 통계 계산 성공")
    void 찜한_레시피를_최신_찜순으로_조회하고_부족_재료_통계_계산_성공() {
        // given
        User targetUser = entityManager.persist(User.builder().loginNumber("3333").build());
        User otherUser = entityManager.persist(User.builder().loginNumber("4444").build());
        FoodIngredient potato = persistFoodIngredient("찜 조회 감자", 1000L);
        FoodIngredient carrot = persistFoodIngredient("찜 조회 당근", 2000L);
        Recipe firstRecipe = persistRecipe("첫 번째 찜 레시피", 10L, RecommendationType.DEFAULT);
        Recipe latestRecipe = persistRecipe("최근 찜 레시피", 20L, RecommendationType.DEFAULT);
        persistRequiredIngredient(latestRecipe, potato, 100.0);
        persistRequiredIngredient(latestRecipe, carrot, 100.0);
        entityManager.persist(UserFoodIngredient.builder()
                .user(targetUser)
                .foodIngredient(potato)
                .relationType(UserFoodIngredientType.OWN)
                .primaryAmountValue(50.0)
                .build());
        entityManager.persist(UserFoodIngredient.builder()
                .user(targetUser)
                .foodIngredient(carrot)
                .relationType(UserFoodIngredientType.OWN)
                .primaryAmountValue(100.0)
                .build());
        favoriteRecipeRepository.saveAndFlush(FavoriteRecipe.create(targetUser, firstRecipe));
        favoriteRecipeRepository.saveAndFlush(FavoriteRecipe.create(targetUser, latestRecipe));
        favoriteRecipeRepository.saveAndFlush(FavoriteRecipe.create(otherUser, latestRecipe));
        entityManager.clear();

        // when
        Slice<RecipeSearchResult> firstPage = recipeRepository.searchFavoriteRecipes(
                targetUser.getId(), 1L, 1
        );
        Slice<RecipeSearchResult> secondPage = recipeRepository.searchFavoriteRecipes(
                targetUser.getId(), 2L, 1
        );

        // then
        assertThat(firstPage.getContent()).hasSize(1);
        assertThat(firstPage.getContent().getFirst().recipeId()).isEqualTo(latestRecipe.getId());
        assertThat(firstPage.getContent().getFirst().matchedIngredientCount()).isEqualTo(2L);
        assertThat(firstPage.getContent().getFirst().requiredIngredientCount()).isEqualTo(1L);
        assertThat(firstPage.getContent().getFirst().requiredIngredientCost()).isEqualTo(500L);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(secondPage.getContent()).extracting(RecipeSearchResult::recipeId)
                .containsExactly(firstRecipe.getId());
        assertThat(secondPage.hasNext()).isFalse();
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
                .exception(false)
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
