package com.likelion.routineeatbe.domain.favoriteRecipe.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.likelion.routineeatbe.domain.favoriteRecipe.entity.FavoriteRecipe;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeType;
import com.likelion.routineeatbe.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:favorite-recipe-test;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FavoriteRecipeRepositoryTest {

    @Autowired
    private FavoriteRecipeRepository favoriteRecipeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("사용자와 레시피 조합의 찜 저장 및 존재 여부 조회 성공")
    void 사용자와_레시피_조합의_찜_저장_및_존재_여부_조회_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("1234").build());
        Recipe recipe = persistRecipe("찜 대상 레시피");

        // when
        FavoriteRecipe result = favoriteRecipeRepository.saveAndFlush(
                FavoriteRecipe.create(user, recipe)
        );

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(favoriteRecipeRepository.existsByUserIdAndRecipeId(
                user.getId(), recipe.getId()
        )).isTrue();
    }

    @Test
    @DisplayName("동일한 사용자와 레시피 조합의 중복 찜 저장 실패")
    void 동일한_사용자와_레시피_조합의_중복_찜_저장_실패() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("5678").build());
        Recipe recipe = persistRecipe("중복 찜 대상 레시피");
        favoriteRecipeRepository.saveAndFlush(FavoriteRecipe.create(user, recipe));
        entityManager.clear();

        // when & then
        assertThatThrownBy(() -> favoriteRecipeRepository.saveAndFlush(
                FavoriteRecipe.create(user, recipe)
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("사용자와 레시피 조합으로 찜 조회 및 삭제 성공")
    void 사용자와_레시피_조합으로_찜_조회_및_삭제_성공() {
        // given
        User targetUser = entityManager.persist(User.builder().loginNumber("1111").build());
        User otherUser = entityManager.persist(User.builder().loginNumber("2222").build());
        Recipe recipe = persistRecipe("찜 해제 대상 레시피");
        favoriteRecipeRepository.saveAndFlush(FavoriteRecipe.create(targetUser, recipe));
        favoriteRecipeRepository.saveAndFlush(FavoriteRecipe.create(otherUser, recipe));
        entityManager.clear();

        // when
        FavoriteRecipe favoriteRecipe = favoriteRecipeRepository.findByUserIdAndRecipeId(
                        targetUser.getId(),
                        recipe.getId()
                )
                .orElseThrow();
        favoriteRecipeRepository.delete(favoriteRecipe);
        favoriteRecipeRepository.flush();

        // then
        assertThat(favoriteRecipeRepository.findByUserIdAndRecipeId(
                targetUser.getId(), recipe.getId()
        )).isEmpty();
        assertThat(favoriteRecipeRepository.findByUserIdAndRecipeId(
                otherUser.getId(), recipe.getId()
        )).isPresent();
    }

    private Recipe persistRecipe(String menuName) {
        Menu menu = entityManager.persist(Menu.builder()
                .name(menuName)
                .type(MenuType.KOREAN)
                .recommendationType(RecommendationType.DEFAULT)
                .calory(100.0)
                .ingredient_info_original("재료 정보")
                .timeRequired(20)
                .difficultyLevel(DifficultyLevel.LEVEL_1)
                .build());
        return entityManager.persist(Recipe.builder()
                .type(RecipeType.BASIC)
                .menu(menu)
                .build());
    }
}
