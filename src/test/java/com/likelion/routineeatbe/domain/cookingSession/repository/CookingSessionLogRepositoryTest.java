package com.likelion.routineeatbe.domain.cookingSession.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSessionLog;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionLogType;
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
import org.springframework.data.domain.Slice;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cookingsessionlogtest;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CookingSessionLogRepositoryTest {

    @Autowired
    private CookingSessionLogRepository cookingSessionLogRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("요리 세션 로그를 생성 순서대로 위치 커서 조회한다")
    void 요리_세션_로그_커서_조회_성공() {
        // given
        CookingSession targetSession = persistCookingSession("1234", "로그 조회 메뉴");
        CookingSession otherSession = persistCookingSession("5678", "다른 로그 조회 메뉴");
        CookingSessionLog firstLog = cookingSessionLogRepository.save(
                CookingSessionLog.create(
                        targetSession,
                        CookingSessionLogType.USER,
                        "굴소스가 부족해요."
                )
        );
        CookingSessionLog secondLog = cookingSessionLogRepository.save(
                CookingSessionLog.create(
                        targetSession,
                        CookingSessionLogType.AI,
                        "간장을 조금 추가해보세요."
                )
        );
        CookingSessionLog thirdLog = cookingSessionLogRepository.save(
                CookingSessionLog.create(
                        targetSession,
                        CookingSessionLogType.SYSTEM,
                        "다음 요리 단계로 이동했습니다."
                )
        );
        cookingSessionLogRepository.save(CookingSessionLog.create(
                otherSession,
                CookingSessionLogType.USER,
                "다른 세션 로그"
        ));
        cookingSessionLogRepository.flush();
        entityManager.clear();

        // when
        Slice<CookingSessionLog> firstPage = cookingSessionLogRepository
                .searchByCookingSessionId(targetSession.getId(), 1, 2);
        Slice<CookingSessionLog> secondPage = cookingSessionLogRepository
                .searchByCookingSessionId(targetSession.getId(), 3, 2);

        // then
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.getContent())
                .extracting(CookingSessionLog::getId)
                .containsExactly(firstLog.getId(), secondLog.getId());
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(secondPage.getContent())
                .extracting(CookingSessionLog::getId)
                .containsExactly(thirdLog.getId());
    }

    @Test
    @DisplayName("로그가 없는 요리 세션을 조회하면 빈 Slice를 반환한다")
    void 요리_세션_로그_조회_성공_빈_결과() {
        // given
        CookingSession cookingSession = persistCookingSession("2468", "빈 로그 조회 메뉴");
        entityManager.clear();

        // when
        Slice<CookingSessionLog> result = cookingSessionLogRepository
                .searchByCookingSessionId(cookingSession.getId(), 1, 10);

        // then
        assertThat(result.hasNext()).isFalse();
        assertThat(result.getContent()).isEmpty();
    }

    private CookingSession persistCookingSession(String loginNumber, String menuName) {
        User user = entityManager.persist(User.builder().loginNumber(loginNumber).build());
        Menu menu = entityManager.persist(Menu.builder()
                .name(menuName)
                .type(MenuType.KOREAN)
                .recommendationType(RecommendationType.DEFAULT)
                .calory(100.0)
                .ingredient_info_original("계란 1개")
                .timeRequired(10)
                .difficultyLevel(DifficultyLevel.LEVEL_1)
                .build());
        Recipe recipe = entityManager.persist(Recipe.builder()
                .type(RecipeType.BASIC)
                .menu(menu)
                .build());
        CookingRecord cookingRecord = CookingRecord.create(user, recipe, 1);
        CookingSession cookingSession = CookingSession.create(cookingRecord, 1);
        entityManager.persist(cookingRecord);
        entityManager.flush();
        return cookingSession;
    }
}
