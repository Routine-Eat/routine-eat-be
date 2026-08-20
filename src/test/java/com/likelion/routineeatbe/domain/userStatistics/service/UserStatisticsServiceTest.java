package com.likelion.routineeatbe.domain.userStatistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.likelion.routineeatbe.domain.cookingRecord.repository.CookingRecordRepository;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.domain.userStatistics.dto.response.UserStatisticsResDto;
import com.likelion.routineeatbe.domain.userStatistics.dto.response.UserStatisticsRecipeReportResDto;
import com.likelion.routineeatbe.domain.userStatistics.entity.UserStatisticsFoodIngredient;
import com.likelion.routineeatbe.domain.userStatistics.entity.UserStatisticsRecipe;
import com.likelion.routineeatbe.domain.userStatistics.entity.UserStatistics;
import com.likelion.routineeatbe.domain.userStatistics.exception.UserStatisticsErrorCode;
import com.likelion.routineeatbe.domain.userStatistics.mapper.UserStatisticsMapper;
import com.likelion.routineeatbe.domain.userStatistics.repository.UserStatisticsFoodIngredientRepository;
import com.likelion.routineeatbe.domain.userStatistics.repository.UserStatisticsRecipeRepository;
import com.likelion.routineeatbe.domain.userStatistics.repository.UserStatisticsRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserStatisticsServiceTest {

    @InjectMocks
    private UserStatisticsService userStatisticsService;

    @Mock
    private CookingRecordRepository cookingRecordRepository;

    @Mock
    private UserStatisticsRepository userStatisticsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStatisticsRecipeRepository userStatisticsRecipeRepository;

    @Mock
    private UserStatisticsFoodIngredientRepository userStatisticsFoodIngredientRepository;

    @Mock
    private UserStatisticsMapper userStatisticsMapper;

    @Test
    @DisplayName("사용자 요리 통계를 생성하고 재료 상위 5개만 저장한다")
    void 사용자_요리_통계_생성_및_재료_상위_5개_저장_성공() {
        // given
        User user = User.builder().id(1L).build();
        Recipe recipe = createRecipe(10L, DifficultyLevel.LEVEL_3);
        List<FoodIngredient> foodIngredients = List.of(
                createFoodIngredient(1L),
                createFoodIngredient(2L),
                createFoodIngredient(3L),
                createFoodIngredient(4L),
                createFoodIngredient(5L)
        );
        given(cookingRecordRepository.findDistinctRecipesByUserId(1L))
                .willReturn(List.of(recipe));
        given(cookingRecordRepository.findTop5MostCookedFoodIngredientsByUserId(1L))
                .willReturn(foodIngredients);
        given(cookingRecordRepository.findRecent5MenuDifficultyLevelsByUserId(1L))
                .willReturn(List.of(DifficultyLevel.LEVEL_1, DifficultyLevel.LEVEL_5));
        given(userStatisticsRepository.save(any(UserStatistics.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        CompletableFuture<UserStatistics> future = userStatisticsService.saveUserStatistics(user);

        // then
        ArgumentCaptor<UserStatistics> captor = ArgumentCaptor.forClass(UserStatistics.class);
        then(userStatisticsRepository).should().save(captor.capture());
        UserStatistics result = captor.getValue();
        assertThat(result.getUser()).isSameAs(user);
        assertThat(result.getCookedRecordAverageDifficultyLevel())
                .isEqualTo(DifficultyLevel.LEVEL_3);
        assertThat(result.getRecipes()).hasSize(2);
        assertThat(result.getFoodIngredients()).hasSize(5);
        assertThat(future.join()).isSameAs(result);
    }

    @Test
    @DisplayName("사용자 통계 조회 성공")
    void 사용자_통계_조회_성공() {
        // given
        User user = User.builder().id(1L).build();
        UserStatistics statistics = UserStatistics.builder()
                .id(10L)
                .user(user)
                .cookedRecordAverageDifficultyLevel(DifficultyLevel.LEVEL_3)
                .build();
        List<UserStatisticsRecipe> recipes = List.of();
        List<UserStatisticsFoodIngredient> foodIngredients = List.of();
        UserStatisticsResDto expected = UserStatisticsResDto.create(
                UserStatisticsRecipeReportResDto.create(List.of()),
                List.of(),
                DifficultyLevel.LEVEL_3
        );
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userStatisticsRepository.findByIdAndUser_Id(10L, 1L))
                .willReturn(Optional.of(statistics));
        given(userStatisticsRecipeRepository.findAllByUserStatistics_IdOrderByRecipe_IdAsc(10L))
                .willReturn(recipes);
        given(userStatisticsFoodIngredientRepository
                .findAllByUserStatistics_IdOrderByFoodIngredient_IdAsc(10L))
                .willReturn(foodIngredients);
        given(userStatisticsMapper.toUserStatisticsResDto(statistics, recipes, foodIngredients))
                .willReturn(expected);

        // when
        UserStatisticsResDto result = userStatisticsService.getUserStatistics(1L, 10L);

        // then
        assertThat(result).isSameAs(expected);
        then(userStatisticsMapper).should()
                .toUserStatisticsResDto(statistics, recipes, foodIngredients);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 통계 조회에 실패한다")
    void 존재하지_않는_사용자_통계_조회_실패() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userStatisticsService.getUserStatistics(999L, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(UserStatisticsErrorCode.USER_NOT_FOUND));
        then(userStatisticsRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("사용자 소유가 아닌 통계 조회에 실패한다")
    void 사용자_소유가_아닌_통계_조회_실패() {
        // given
        User user = User.builder().id(1L).build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userStatisticsRepository.findByIdAndUser_Id(10L, 1L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userStatisticsService.getUserStatistics(1L, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(UserStatisticsErrorCode.USER_STATISTICS_NOT_FOUND));
        then(userStatisticsRecipeRepository).shouldHaveNoInteractions();
    }

    private Recipe createRecipe(Long recipeId, DifficultyLevel difficultyLevel) {
        Menu menu = Menu.builder()
                .id(recipeId)
                .difficultyLevel(difficultyLevel)
                .build();
        return Recipe.builder()
                .id(recipeId)
                .menu(menu)
                .build();
    }

    private FoodIngredient createFoodIngredient(Long foodIngredientId) {
        return FoodIngredient.builder()
                .id(foodIngredientId)
                .name("재료" + foodIngredientId)
                .build();
    }
}
