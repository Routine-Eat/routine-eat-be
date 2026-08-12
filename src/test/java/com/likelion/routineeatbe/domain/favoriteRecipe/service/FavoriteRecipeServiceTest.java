package com.likelion.routineeatbe.domain.favoriteRecipe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.likelion.routineeatbe.domain.favoriteRecipe.entity.FavoriteRecipe;
import com.likelion.routineeatbe.domain.favoriteRecipe.exception.FavoriteRecipeErrorCode;
import com.likelion.routineeatbe.domain.favoriteRecipe.repository.FavoriteRecipeRepository;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class FavoriteRecipeServiceTest {

    @InjectMocks
    private FavoriteRecipeService favoriteRecipeService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private FavoriteRecipeRepository favoriteRecipeRepository;

    @Test
    @DisplayName("사용자와 레시피를 조회하여 레시피 찜 등록 성공")
    void 사용자와_레시피를_조회하여_레시피_찜_등록_성공() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        Recipe recipe = Recipe.builder().id(10L).build();
        FavoriteRecipe savedFavoriteRecipe = FavoriteRecipe.builder()
                .id(100L)
                .user(user)
                .recipe(recipe)
                .build();
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(recipeRepository.findById(10L)).willReturn(Optional.of(recipe));
        given(favoriteRecipeRepository.existsByUserIdAndRecipeId(1L, 10L))
                .willReturn(false);
        given(favoriteRecipeRepository.saveAndFlush(any(FavoriteRecipe.class)))
                .willReturn(savedFavoriteRecipe);

        // when
        favoriteRecipeService.addFavorite(10L, 1234);

        // then
        ArgumentCaptor<FavoriteRecipe> captor = ArgumentCaptor.forClass(FavoriteRecipe.class);
        then(favoriteRecipeRepository).should().saveAndFlush(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getRecipe()).isSameAs(recipe);
    }

    @Test
    @DisplayName("레시피 찜 등록 실패 - 존재하지 않는 사용자")
    void 레시피_찜_등록_실패_존재하지_않는_사용자() {
        // given
        given(userRepository.findByLoginNumber("9999")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> favoriteRecipeService.addFavorite(10L, 9999))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(FavoriteRecipeErrorCode.USER_NOT_FOUND);
        then(recipeRepository).shouldHaveNoInteractions();
        then(favoriteRecipeRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("레시피 찜 등록 실패 - 존재하지 않는 레시피")
    void 레시피_찜_등록_실패_존재하지_않는_레시피() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(recipeRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> favoriteRecipeService.addFavorite(999L, 1234))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(FavoriteRecipeErrorCode.RECIPE_NOT_FOUND);
        then(favoriteRecipeRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("레시피 찜 등록 실패 - 이미 찜한 레시피")
    void 레시피_찜_등록_실패_이미_찜한_레시피() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        Recipe recipe = Recipe.builder().id(10L).build();
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(recipeRepository.findById(10L)).willReturn(Optional.of(recipe));
        given(favoriteRecipeRepository.existsByUserIdAndRecipeId(1L, 10L))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> favoriteRecipeService.addFavorite(10L, 1234))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(FavoriteRecipeErrorCode.FAVORITE_RECIPE_ALREADY_EXISTS);
        then(favoriteRecipeRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("레시피 찜 등록 실패 - 동시 요청으로 중복 저장")
    void 레시피_찜_등록_실패_동시_요청으로_중복_저장() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        Recipe recipe = Recipe.builder().id(10L).build();
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(recipeRepository.findById(10L)).willReturn(Optional.of(recipe));
        given(favoriteRecipeRepository.existsByUserIdAndRecipeId(1L, 10L))
                .willReturn(false);
        willThrow(new DataIntegrityViolationException("duplicate"))
                .given(favoriteRecipeRepository)
                .saveAndFlush(any(FavoriteRecipe.class));

        // when & then
        assertThatThrownBy(() -> favoriteRecipeService.addFavorite(10L, 1234))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(FavoriteRecipeErrorCode.FAVORITE_RECIPE_ALREADY_EXISTS);
    }
}
