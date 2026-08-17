package com.likelion.routineeatbe.domain.cookingRecord.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingRecordSearchResult;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecordFoodIngredient;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import com.likelion.routineeatbe.domain.cookingSession.repository.CookingStepRepository;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import com.likelion.routineeatbe.domain.favoriteRecipe.entity.FavoriteRecipe;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeType;
import com.likelion.routineeatbe.domain.user.entity.User;
import java.util.EnumSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Slice;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cooking-record-test;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CookingRecordRepositoryTest {

    @Autowired
    private CookingRecordRepository cookingRecordRepository;

    @Autowired
    private CookingStepRepository cookingStepRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("사용자의 회고 저장까지 종료된 요리 기록을 집계하여 조회한다")
    void 종료_요리_기록_목록_집계_조회_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("1234").build());
        User otherUser = entityManager.persist(User.builder().loginNumber("5678").build());
        Recipe recipe = persistRecipe();
        FoodIngredient potato = persistFoodIngredient("감자");
        FoodIngredient seaweed = persistFoodIngredient("미역");

        CookingRecord target = CookingRecord.builder()
                .user(user)
                .recipe(recipe)
                .servings(1)
                .difficultyLevel(DifficultyLevel.LEVEL_3)
                .build();
        CookingSession targetSession = CookingSession.create(target, 1);
        targetSession.terminate();
        CookingRecordFoodIngredient.create(target, potato, 100.0, null);
        CookingRecordFoodIngredient.create(target, seaweed, 20.0, null);
        cookingRecordRepository.saveAndFlush(target);
        entityManager.persistAndFlush(FavoriteRecipe.create(user, recipe));

        CookingRecord completedButNotTerminated = CookingRecord.builder()
                .user(user)
                .recipe(recipe)
                .servings(1)
                .difficultyLevel(DifficultyLevel.LEVEL_2)
                .build();
        CookingSession completedSession = CookingSession.create(completedButNotTerminated, 1);
        completedSession.complete();
        cookingRecordRepository.saveAndFlush(completedButNotTerminated);

        CookingRecord inProgress = CookingRecord.builder()
                .user(user)
                .recipe(recipe)
                .servings(1)
                .difficultyLevel(DifficultyLevel.LEVEL_2)
                .build();
        CookingSession.create(inProgress, 1);
        cookingRecordRepository.saveAndFlush(inProgress);

        CookingRecord otherUserRecord = CookingRecord.builder()
                .user(otherUser)
                .recipe(recipe)
                .servings(1)
                .difficultyLevel(DifficultyLevel.LEVEL_4)
                .build();
        CookingSession otherUserSession = CookingSession.create(otherUserRecord, 1);
        otherUserSession.terminate();
        cookingRecordRepository.saveAndFlush(otherUserRecord);
        entityManager.clear();

        // when
        Slice<CookingRecordSearchResult> result = cookingRecordRepository
                .searchTerminatedCookingRecords(user.getId(), 1, 10);

        // then
        assertThat(result.hasNext()).isFalse();
        assertThat(result.getContent()).singleElement().satisfies(record -> {
            assertThat(record.recipeId()).isEqualTo(recipe.getId());
            assertThat(record.menuName()).isEqualTo("요리 시작 테스트 메뉴");
            assertThat(record.favoriteRecipe()).isTrue();
            assertThat(record.createdAt()).isNotNull();
            assertThat(record.userDifficultyLevel()).isEqualTo(DifficultyLevel.LEVEL_3);
            assertThat(record.usedFoodIngredientCount()).isEqualTo(2L);
        });
    }

    @Test
    @DisplayName("종료 요리 기록을 최신순으로 위치 커서 조회한다")
    void 종료_요리_기록_커서_조회_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("2468").build());
        Recipe firstRecipe = persistRecipe();
        Recipe secondRecipe = persistRecipe("두 번째 요리 테스트 메뉴");
        persistTerminatedCookingRecord(user, firstRecipe, DifficultyLevel.LEVEL_1);
        persistTerminatedCookingRecord(user, secondRecipe, DifficultyLevel.LEVEL_5);
        entityManager.clear();

        // when
        Slice<CookingRecordSearchResult> firstPage = cookingRecordRepository
                .searchTerminatedCookingRecords(user.getId(), 1, 1);
        Slice<CookingRecordSearchResult> secondPage = cookingRecordRepository
                .searchTerminatedCookingRecords(user.getId(), 2, 1);

        // then
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.getContent()).singleElement()
                .extracting(CookingRecordSearchResult::recipeId)
                .isEqualTo(secondRecipe.getId());
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(secondPage.getContent()).singleElement()
                .extracting(CookingRecordSearchResult::recipeId)
                .isEqualTo(firstRecipe.getId());
    }

    @Test
    @DisplayName("사용자의 가장 최근 완료 요리 기록을 조회한다")
    void findLatestCompletedCookingRecord_success() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("2468").build());
        Recipe recipe = persistRecipe();

        CookingRecord firstCompleted = CookingRecord.create(user, recipe, 1);
        CookingSession firstSession = CookingSession.create(firstCompleted, 1);
        firstSession.complete();
        cookingRecordRepository.saveAndFlush(firstCompleted);

        CookingRecord latestCompleted = CookingRecord.create(user, recipe, 2);
        CookingSession latestSession = CookingSession.create(latestCompleted, 1);
        latestSession.complete();
        cookingRecordRepository.saveAndFlush(latestCompleted);

        CookingRecord inProgress = CookingRecord.create(user, recipe, 3);
        CookingSession.create(inProgress, 1);
        cookingRecordRepository.saveAndFlush(inProgress);
        entityManager.clear();

        // when
        CookingRecord result = cookingRecordRepository
                .findFirstByUser_IdAndCookingSession_StatusOrderByCreatedAtDescIdDesc(
                        user.getId(),
                        CookingSessionStatus.COMPLETED
                )
                .orElseThrow();

        // then
        assertThat(result.getId()).isEqualTo(latestCompleted.getId());
    }

    @Test
    @DisplayName("요리 기록 저장 시 세션, 체크리스트와 사용 음식 재료를 함께 저장한다")
    void 요리_기록_연관_데이터_Cascade_저장_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("1234").build());
        Recipe recipe = persistRecipe();
        FoodIngredient foodIngredient = entityManager.persist(FoodIngredient.builder()
                .name("대파")
                .type(FoodIngredientType.VEGETABLE)
                .pricePerHundred(1000L)
                .primaryUnit(PrimaryUnit.G)
                .secondaryUnit(SecondaryUnit.JULGI)
                .exception(false)
                .build());
        CookingRecord cookingRecord = CookingRecord.create(user, recipe, 2);
        CookingRecordFoodIngredient.create(
                cookingRecord,
                foodIngredient,
                100.0,
                2.0
        );
        CookingSession cookingSession = CookingSession.create(cookingRecord, 1);
        CookingStep.create(cookingSession, 0L, "요리 시작 전 체크리스트", "손을 씻으세요.", null);
        CookingStep.create(cookingSession, 1L, "재료 준비", "재료를 준비하세요.", null);

        // when
        CookingRecord saved = cookingRecordRepository.saveAndFlush(cookingRecord);
        entityManager.clear();

        // then
        CookingRecord result = cookingRecordRepository
                .findByIdAndUserIdWithFoodIngredients(saved.getId(), user.getId())
                .orElseThrow();
        assertThat(result.getCookingSession().getCookingStepCount()).isEqualTo(1);
        assertThat(result.getCookingSession().getCookingSteps())
                .extracting(CookingStep::getLevel)
                .containsExactlyInAnyOrder(0L, 1L);
        assertThat(result.getFoodIngredients()).singleElement().satisfies(usedIngredient -> {
            assertThat(usedIngredient.getPrimaryUsedAmountValue()).isEqualTo(100.0);
            assertThat(usedIngredient.getSecondaryUsedAmountValue()).isEqualTo(2.0);
            assertThat(usedIngredient.getFoodIngredient().getId())
                    .isEqualTo(foodIngredient.getId());
        });
        assertThat(cookingRecordRepository.existsBlockingSession(
                user.getId(),
                recipe.getId(),
                EnumSet.of(CookingSessionStatus.IN_PROGRESS, CookingSessionStatus.COMPLETED)
        )).isTrue();
    }

    @Test
    @DisplayName("잠금 조회한 요리 기록 음식 재료의 사용량 변경을 저장한다")
    void 요리_기록_음식_재료_사용량_변경_저장_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("1357").build());
        Recipe recipe = persistRecipe();
        FoodIngredient foodIngredient = entityManager.persist(FoodIngredient.builder()
                .name("계란")
                .type(FoodIngredientType.VEGETABLE)
                .pricePerHundred(1000L)
                .primaryUnit(PrimaryUnit.G)
                .secondaryUnit(SecondaryUnit.AL)
                .exception(false)
                .build());
        CookingRecord cookingRecord = CookingRecord.create(user, recipe, 1);
        CookingRecordFoodIngredient.create(cookingRecord, foodIngredient, 80.0, 2.0);
        CookingSession.create(cookingRecord, 1);
        CookingRecord saved = cookingRecordRepository.saveAndFlush(cookingRecord);
        entityManager.clear();

        // when
        CookingRecord lockedCookingRecord = cookingRecordRepository
                .findByIdAndUserIdForUpdate(saved.getId(), user.getId())
                .orElseThrow();
        lockedCookingRecord.getFoodIngredients().getFirst()
                .updateUsedAmountValues(60.0, null);
        entityManager.flush();
        entityManager.clear();

        // then
        CookingRecord result = cookingRecordRepository
                .findByIdAndUserIdWithFoodIngredients(saved.getId(), user.getId())
                .orElseThrow();
        assertThat(result.getFoodIngredients()).singleElement().satisfies(usedIngredient -> {
            assertThat(usedIngredient.getPrimaryUsedAmountValue()).isEqualTo(60.0);
            assertThat(usedIngredient.getSecondaryUsedAmountValue()).isEqualTo(2.0);
        });
    }

    @Test
    @DisplayName("완료된 동일 요리 세션은 새로운 요리 시작을 차단한다")
    void 완료된_동일_요리_세션_차단_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("5678").build());
        Recipe recipe = persistRecipe();
        CookingRecord cookingRecord = CookingRecord.create(user, recipe, 1);
        CookingSession cookingSession = CookingSession.builder()
                .status(CookingSessionStatus.COMPLETED)
                .cookingStepCount(1)
                .currentCookingStepLevel(1)
                .cookingRecord(cookingRecord)
                .build();
        cookingRecord.assignCookingSession(cookingSession);
        CookingStep.create(cookingSession, 1L, "요리 완료", "요리를 완료합니다.", null);
        cookingRecordRepository.saveAndFlush(cookingRecord);

        // when
        boolean result = cookingRecordRepository.existsBlockingSession(
                user.getId(),
                recipe.getId(),
                EnumSet.of(CookingSessionStatus.IN_PROGRESS, CookingSessionStatus.COMPLETED)
        );

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("종료된 동일 요리 세션은 새로운 요리 시작을 허용한다")
    void 종료된_동일_요리_세션_허용_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("9012").build());
        Recipe recipe = persistRecipe();
        CookingRecord cookingRecord = CookingRecord.create(user, recipe, 1);
        CookingSession cookingSession = CookingSession.builder()
                .status(CookingSessionStatus.TERMINATED)
                .cookingStepCount(1)
                .currentCookingStepLevel(1)
                .cookingRecord(cookingRecord)
                .build();
        cookingRecord.assignCookingSession(cookingSession);
        CookingStep.create(cookingSession, 1L, "요리 종료", "요리를 종료합니다.", null);
        cookingRecordRepository.saveAndFlush(cookingRecord);

        // when
        boolean result = cookingRecordRepository.existsBlockingSession(
                user.getId(),
                recipe.getId(),
                EnumSet.of(CookingSessionStatus.IN_PROGRESS, CookingSessionStatus.COMPLETED)
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자 소유 요리 기록을 레시피와 메뉴까지 함께 조회한다")
    void 사용자_소유_요리_기록_레시피_메뉴_조회_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("3456").build());
        User otherUser = entityManager.persist(User.builder().loginNumber("7890").build());
        Recipe recipe = persistRecipe();
        CookingRecord saved = cookingRecordRepository.saveAndFlush(
                CookingRecord.create(user, recipe, 1)
        );
        entityManager.clear();

        // when
        CookingRecord result = cookingRecordRepository
                .findByIdAndUserIdWithRecipeAndMenu(saved.getId(), user.getId())
                .orElseThrow();

        // then
        assertThat(result.getRecipe().getMenu().getName()).isEqualTo("요리 시작 테스트 메뉴");
        assertThat(cookingRecordRepository.findByIdAndUserIdWithRecipeAndMenu(
                saved.getId(),
                otherUser.getId()
        )).isEmpty();
    }

    @Test
    @DisplayName("사용자 소유 요리 기록과 세션을 조회하고 양수 단계만 조회한다")
    void 사용자_소유_요리_기록_세션_단계_조회_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("3456").build());
        User otherUser = entityManager.persist(User.builder().loginNumber("7890").build());
        Recipe recipe = persistRecipe();
        CookingRecord cookingRecord = CookingRecord.create(user, recipe, 1);
        CookingSession cookingSession = CookingSession.create(cookingRecord, 2);
        CookingStep.create(cookingSession, 0L, "체크리스트", "손을 씻으세요.", null);
        CookingStep expected = CookingStep.create(
                cookingSession,
                2L,
                "두 번째 단계",
                "두 번째 단계입니다.",
                null
        );
        CookingRecord saved = cookingRecordRepository.saveAndFlush(cookingRecord);
        entityManager.clear();

        // when
        CookingRecord result = cookingRecordRepository
                .findByIdAndUserIdForUpdate(saved.getId(), user.getId())
                .orElseThrow();

        // then
        assertThat(result.getCookingSession()).isNotNull();
        assertThat(cookingStepRepository.findByCookingSessionIdAndLevel(
                result.getCookingSession().getId(),
                2L
        )).get().extracting(CookingStep::getId).isEqualTo(expected.getId());
        assertThat(cookingRecordRepository.findByIdAndUserIdForUpdate(
                saved.getId(),
                otherUser.getId()
        )).isEmpty();
    }

    private Recipe persistRecipe() {
        return persistRecipe("요리 시작 테스트 메뉴");
    }

    private Recipe persistRecipe(String menuName) {
        Menu menu = entityManager.persist(Menu.builder()
                .name(menuName)
                .type(MenuType.KOREAN)
                .recommendationType(RecommendationType.DEFAULT)
                .calory(100.0)
                .ingredient_info_original("계란 1개")
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

    private CookingRecord persistTerminatedCookingRecord(
            User user,
            Recipe recipe,
            DifficultyLevel difficultyLevel
    ) {
        CookingRecord cookingRecord = CookingRecord.builder()
                .user(user)
                .recipe(recipe)
                .servings(1)
                .difficultyLevel(difficultyLevel)
                .build();
        CookingSession cookingSession = CookingSession.create(cookingRecord, 1);
        cookingSession.terminate();
        return cookingRecordRepository.saveAndFlush(cookingRecord);
    }
}
