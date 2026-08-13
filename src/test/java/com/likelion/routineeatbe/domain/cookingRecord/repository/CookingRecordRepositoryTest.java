package com.likelion.routineeatbe.domain.cookingRecord.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
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
    private TestEntityManager entityManager;

    @Test
    @DisplayName("요리 기록 저장 시 세션과 level 0 체크리스트를 함께 저장한다")
    void 요리_기록_세션_체크리스트_Cascade_저장_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("1234").build());
        Recipe recipe = persistRecipe();
        CookingRecord cookingRecord = CookingRecord.create(user, recipe, 2);
        CookingSession cookingSession = CookingSession.create(cookingRecord, 1);
        CookingStep.create(cookingSession, 0L, "요리 시작 전 체크리스트", "손을 씻으세요.", null);
        CookingStep.create(cookingSession, 1L, "재료 준비", "재료를 준비하세요.", null);

        // when
        CookingRecord saved = cookingRecordRepository.saveAndFlush(cookingRecord);
        entityManager.clear();

        // then
        CookingRecord result = cookingRecordRepository.findById(saved.getId()).orElseThrow();
        assertThat(result.getCookingSession().getCookingStepCount()).isEqualTo(1);
        assertThat(result.getCookingSession().getCookingSteps())
                .extracting(CookingStep::getLevel)
                .containsExactlyInAnyOrder(0L, 1L);
        assertThat(cookingRecordRepository.existsBlockingSession(
                user.getId(),
                recipe.getId(),
                EnumSet.of(CookingSessionStatus.IN_PROGRESS, CookingSessionStatus.COMPLETED)
        )).isTrue();
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

    private Recipe persistRecipe() {
        Menu menu = entityManager.persist(Menu.builder()
                .name("요리 시작 테스트 메뉴")
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
}
