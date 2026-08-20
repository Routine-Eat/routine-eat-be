package com.likelion.routineeatbe.domain.favoriteRecipe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.likelion.routineeatbe.domain.favoriteRecipe.dto.request.FavoriteRecipeSearchReqDto;
import com.likelion.routineeatbe.domain.favoriteRecipe.dto.response.FavoriteRecipeListResDto;
import com.likelion.routineeatbe.domain.favoriteRecipe.entity.FavoriteRecipe;
import com.likelion.routineeatbe.domain.favoriteRecipe.exception.FavoriteRecipeErrorCode;
import com.likelion.routineeatbe.domain.favoriteRecipe.mapper.FavoriteRecipeMapper;
import com.likelion.routineeatbe.domain.favoriteRecipe.repository.FavoriteRecipeRepository;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

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

    @Mock
    private FavoriteRecipeMapper favoriteRecipeMapper;

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

    @Test
    @DisplayName("사용자와 레시피의 찜 정보를 조회하여 레시피 찜 해제 성공")
    void 사용자와_레시피의_찜_정보를_조회하여_레시피_찜_해제_성공() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        Recipe recipe = Recipe.builder().id(10L).build();
        FavoriteRecipe favoriteRecipe = FavoriteRecipe.builder()
                .id(100L)
                .user(user)
                .recipe(recipe)
                .build();
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(recipeRepository.findById(10L)).willReturn(Optional.of(recipe));
        given(favoriteRecipeRepository.findByUserIdAndRecipeId(1L, 10L))
                .willReturn(Optional.of(favoriteRecipe));

        // when
        favoriteRecipeService.removeFavorite(10L, "1234");

        // then
        then(favoriteRecipeRepository).should().delete(favoriteRecipe);
    }

    @Test
    @DisplayName("레시피 찜 해제 실패 - 존재하지 않는 사용자")
    void 레시피_찜_해제_실패_존재하지_않는_사용자() {
        // given
        given(userRepository.findByLoginNumber("9999")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> favoriteRecipeService.removeFavorite(10L, "9999"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(FavoriteRecipeErrorCode.USER_NOT_FOUND);
        then(recipeRepository).shouldHaveNoInteractions();
        then(favoriteRecipeRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("레시피 찜 해제 실패 - 존재하지 않는 레시피")
    void 레시피_찜_해제_실패_존재하지_않는_레시피() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(recipeRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> favoriteRecipeService.removeFavorite(999L, "1234"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(FavoriteRecipeErrorCode.RECIPE_NOT_FOUND);
        then(favoriteRecipeRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("레시피 찜 해제 실패 - 존재하지 않는 찜 정보")
    void 레시피_찜_해제_실패_존재하지_않는_찜_정보() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        Recipe recipe = Recipe.builder().id(10L).build();
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(recipeRepository.findById(10L)).willReturn(Optional.of(recipe));
        given(favoriteRecipeRepository.findByUserIdAndRecipeId(1L, 10L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> favoriteRecipeService.removeFavorite(10L, "1234"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(FavoriteRecipeErrorCode.FAVORITE_RECIPE_NOT_FOUND);
        then(favoriteRecipeRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("찜한 레시피 조회 성공 - 다음 페이지 존재")
    void 찜한_레시피_조회_성공_다음_페이지_존재() {
        // given
        FavoriteRecipeSearchReqDto request = new FavoriteRecipeSearchReqDto("1234", 1L, 10);
        User user = User.builder().id(1L).loginNumber("1234").build();
        RecipeSearchResult recipeSearchResult = new RecipeSearchResult(
                10L, 100L, "감자미역국", "thumbnail", 35.4, 20,
                null, null, 0L, 1L, 4L, 10_000L
        );
        Slice<RecipeSearchResult> slice = new SliceImpl<>(
                List.of(recipeSearchResult),
                PageRequest.of(0, request.size()),
                true
        );
        FavoriteRecipeListResDto expected = FavoriteRecipeListResDto.create(
                List.of(),
                true,
                11L
        );
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(recipeRepository.searchFavoriteRecipes(1L, 1L, 10)).willReturn(slice);
        given(favoriteRecipeMapper.toFavoriteRecipeListResDto(slice, 11L))
                .willReturn(expected);

        // when
        FavoriteRecipeListResDto result = favoriteRecipeService.getFavoriteRecipes(request);

        // then
        assertThat(result).isSameAs(expected);
        then(recipeRepository).should().searchFavoriteRecipes(1L, 1L, 10);
        then(favoriteRecipeMapper).should().toFavoriteRecipeListResDto(slice, 11L);
    }

    @Test
    @DisplayName("찜한 레시피 조회 성공 - 빈 마지막 페이지")
    void 찜한_레시피_조회_성공_빈_마지막_페이지() {
        // given
        FavoriteRecipeSearchReqDto request = new FavoriteRecipeSearchReqDto("1234", null, null);
        User user = User.builder().id(1L).loginNumber("1234").build();
        Slice<RecipeSearchResult> slice = new SliceImpl<>(
                List.of(),
                PageRequest.of(0, request.size()),
                false
        );
        FavoriteRecipeListResDto expected = FavoriteRecipeListResDto.create(
                List.of(),
                false,
                null
        );
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(recipeRepository.searchFavoriteRecipes(1L, 1L, 10)).willReturn(slice);
        given(favoriteRecipeMapper.toFavoriteRecipeListResDto(slice, null))
                .willReturn(expected);

        // when
        FavoriteRecipeListResDto result = favoriteRecipeService.getFavoriteRecipes(request);

        // then
        assertThat(result).isSameAs(expected);
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    @DisplayName("찜한 레시피 조회 실패 - 존재하지 않는 사용자")
    void 찜한_레시피_조회_실패_존재하지_않는_사용자() {
        // given
        FavoriteRecipeSearchReqDto request = new FavoriteRecipeSearchReqDto("9999", 1L, 10);
        given(userRepository.findByLoginNumber("9999")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> favoriteRecipeService.getFavoriteRecipes(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(FavoriteRecipeErrorCode.USER_NOT_FOUND);
        then(recipeRepository).shouldHaveNoInteractions();
        then(favoriteRecipeMapper).shouldHaveNoInteractions();
    }
}
