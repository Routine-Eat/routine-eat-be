package com.likelion.routineeatbe.domain.recipe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeWithSimilarRecipes;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeDetailReqDto;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeKeywordSearchReqDto;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeDetailResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeIngredientResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeIngredientUsageListResponseDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeKeywordSearchResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeSearchResponseDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.SimilarRecipeResDto;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeSortType;
import com.likelion.routineeatbe.domain.recipe.mapper.RecipeMapper;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.repository.RecipeFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import com.likelion.routineeatbe.domain.user.repository.UserFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.domain.userSearchHistory.entity.UserSearchHistory;
import com.likelion.routineeatbe.domain.userSearchHistory.repository.UserSearchHistoryRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.response.CursorSliceResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @InjectMocks
    private RecipeService recipeService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserFoodIngredientRepository userFoodIngredientRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeFoodIngredientRepository recipeFoodIngredientRepository;

    @Mock
    private UserSearchHistoryRepository userSearchHistoryRepository;

    @Mock
    private RecipeMapper recipeMapper;

    @Mock
    private FindSimilarRecipeService findSimilarRecipeService;

    @Test
    @DisplayName("인분과 사용자 보유량을 반영한 레시피 상세 조회 성공")
    void 인분과_사용자_보유량을_반영한_레시피_상세_조회_성공() {
        // given
        RecipeDetailReqDto request = new RecipeDetailReqDto("1234", 2);
        User user = User.builder().id(1L).loginNumber("1234").build();
        Menu targetMenu = Menu.builder()
                .id(10L)
                .name("볶음밥")
                .thumbnailUrl("thumbnail")
                .timeRequired(15)
                .difficultyLevel(DifficultyLevel.LEVEL_1)
                .build();
        Recipe targetRecipe = Recipe.builder().id(100L).menu(targetMenu).build();
        Menu similarMenu = Menu.builder().id(20L).name("김치 볶음밥").build();
        Recipe similarRecipe = Recipe.builder().id(200L).menu(similarMenu).build();

        FoodIngredient carrot = FoodIngredient.builder()
                .id(1000L)
                .name("당근")
                .type(FoodIngredientType.VEGETABLE)
                .pricePerHundred(1000L)
                .primaryUnit(PrimaryUnit.G)
                .secondaryUnit(SecondaryUnit.GAE)
                .build();
        FoodIngredient egg = FoodIngredient.builder()
                .id(2000L)
                .name("달걀")
                .type(FoodIngredientType.EGG)
                .pricePerHundred(500L)
                .primaryUnit(PrimaryUnit.G)
                .secondaryUnit(SecondaryUnit.AL)
                .build();
        RecipeFoodIngredient targetCarrot = RecipeFoodIngredient.create(
                targetRecipe, carrot, 100.0, 1.0
        );
        RecipeFoodIngredient targetEgg = RecipeFoodIngredient.create(
                targetRecipe, egg, 50.0, 1.0
        );
        RecipeFoodIngredient similarCarrot = RecipeFoodIngredient.create(
                similarRecipe, carrot, 100.0, 1.0
        );
        UserFoodIngredient ownedCarrot = UserFoodIngredient.builder()
                .user(user)
                .foodIngredient(carrot)
                .relationType(UserFoodIngredientType.OWN)
                .primaryAmountValue(50.0)
                .build();
        UserFoodIngredient ownedEgg = UserFoodIngredient.builder()
                .user(user)
                .foodIngredient(egg)
                .relationType(UserFoodIngredientType.OWN)
                .primaryAmountValue(100.0)
                .build();

        RecipeIngredientResDto fullCarrot = RecipeIngredientResDto.builder()
                .id(1000L).primaryNeedAmountValue(200.0).secondaryNeedAmountValue(2.0).build();
        RecipeIngredientResDto additionalCarrot = RecipeIngredientResDto.builder()
                .id(1000L).primaryNeedAmountValue(150.0).secondaryNeedAmountValue(1.5).build();
        RecipeIngredientResDto fullEgg = RecipeIngredientResDto.builder()
                .id(2000L).primaryNeedAmountValue(100.0).secondaryNeedAmountValue(2.0).build();
        SimilarRecipeResDto similarRecipeResDto = SimilarRecipeResDto.builder()
                .id(200L).name("김치 볶음밥").additionalFoodIngredientCount(1L).build();
        RecipeDetailResDto expectedResponse = RecipeDetailResDto.builder()
                .recipeId(100L)
                .foodIngredientUsingPercent(100L)
                .additionalFoodIngredientCost(1500L)
                .servings(2)
                .foodIngredients(List.of(fullCarrot, fullEgg))
                .additionalFoodIngredients(List.of(additionalCarrot))
                .similarRecipes(List.of(similarRecipeResDto))
                .build();

        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(findSimilarRecipeService.findRecipeWithSimilarRecipes(100L))
                .willReturn(RecipeWithSimilarRecipes.create(
                        targetRecipe,
                        List.of(similarRecipe)
                ));
        given(recipeFoodIngredientRepository.findAllByRecipeIdInWithFoodIngredient(
                java.util.Set.of(100L, 200L)
        )).willReturn(List.of(targetCarrot, targetEgg, similarCarrot));
        given(userFoodIngredientRepository.findAllWithFoodIngredientByUserIdAndRelationType(
                1L,
                UserFoodIngredientType.OWN
        )).willReturn(List.of(ownedCarrot, ownedEgg));
        given(recipeMapper.toRecipeIngredientResDto(targetCarrot, 200.0, 2.0))
                .willReturn(fullCarrot);
        given(recipeMapper.toRecipeIngredientResDto(targetCarrot, 150.0, 1.5))
                .willReturn(additionalCarrot);
        given(recipeMapper.toRecipeIngredientResDto(targetEgg, 100.0, 2.0))
                .willReturn(fullEgg);
        given(recipeMapper.toSimilarRecipeResDto(similarRecipe, 1L))
                .willReturn(similarRecipeResDto);
        given(recipeMapper.toRecipeDetailResDto(
                targetRecipe,
                2L,
                2L,
                1500L,
                2,
                List.of(fullCarrot, fullEgg),
                List.of(additionalCarrot),
                List.of(similarRecipeResDto)
        )).willReturn(expectedResponse);

        // when
        RecipeDetailResDto result = recipeService.getRecipeDetail(100L, request);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.foodIngredientUsingPercent()).isEqualTo(100L);
        assertThat(result.additionalFoodIngredientCost()).isEqualTo(1500L);
        assertThat(result.additionalFoodIngredients().getFirst().primaryNeedAmountValue())
                .isEqualTo(150.0);
        assertThat(result.similarRecipes().getFirst().additionalFoodIngredientCount())
                .isEqualTo(1L);
        verify(findSimilarRecipeService).findRecipeWithSimilarRecipes(100L);
    }

    @Test
    @DisplayName("남은 재료 및 추천 유형별 레시피 목록 조회 성공")
    void 남은_재료_및_추천_유형별_레시피_목록_조회_성공() {
        // given
        RecipeSearchRequestDto request = createRequest("1234");
        User user = User.builder().id(1L).loginNumber("1234").build();
        FoodIngredient prioritizedFoodIngredient = FoodIngredient.builder()
                .id(100L)
                .name("감자")
                .build();
        RecipeSearchResult searchResult = new RecipeSearchResult(
                10L, 20L, "감자 요리", null, 100.0, 20,
                null, null, 3L, 1L, 2L, 2500L, true
        );
        RecipeIngredientUsageListResponseDto usageResponseDto =
                RecipeIngredientUsageListResponseDto.builder()
                        .recipeId(10L)
                        .menuName("감자 요리")
                        .foodIngredientUsingPercent(50L)
                        .requiredIngredientCost(2500L)
                        .isFavoriteRecipe(true)
                        .build();
        Slice<RecipeSearchResult> slice = new SliceImpl<>(
                List.of(searchResult), PageRequest.of(0, 10), true
        );

        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(userFoodIngredientRepository.findFoodIngredientsByTotalPrimaryAmountDesc(
                user.getId(),
                UserFoodIngredientType.OWN,
                PageRequest.of(0, 1)
        )).willReturn(List.of(prioritizedFoodIngredient));
        given(recipeRepository.searchRecipesByFoodIngredient(user.getId(), 100L, request))
                .willReturn(slice);
        for (RecommendationType type : List.of(
                RecommendationType.SIMPLE,
                RecommendationType.DIET,
                RecommendationType.GLUTEN_FREE
        )) {
            given(recipeRepository.searchRecipes(user.getId(), request, type)).willReturn(slice);
        }
        given(recipeMapper.toRecipeIngredientUsageListResponseDto(searchResult))
                .willReturn(usageResponseDto);

        // when
        RecipeSearchResponseDto result = recipeService.getRecipes(request);

        // then
        assertThat(result.remainFoodIngredientName()).isEqualTo("감자");
        assertThat(result.remainFoodIngredient().content()).containsExactly(usageResponseDto);
        assertThat(result.remainFoodIngredient().content().getFirst().foodIngredientUsingPercent())
                .isEqualTo(50L);
        assertThat(result.simpleRecipe().nextCursor()).isEqualTo(11L);
        assertThat(result.dietRecipe().content()).containsExactly(usageResponseDto);
        assertThat(result.dietRecipe().content().getFirst().foodIngredientUsingPercent())
                .isEqualTo(50L);
        assertThat(result.dietRecipe().nextCursor()).isEqualTo(11L);
        assertThat(result.glutenFreeRecipe().nextCursor()).isEqualTo(11L);
        verify(recipeRepository).searchRecipesByFoodIngredient(user.getId(), 100L, request);
        for (RecommendationType type : List.of(
                RecommendationType.SIMPLE,
                RecommendationType.DIET,
                RecommendationType.GLUTEN_FREE
        )) {
            verify(recipeRepository).searchRecipes(user.getId(), request, type);
        }
    }

    @Test
    @DisplayName("보유량이 남은 OWN 재료가 없는 경우 남은 재료 우선 목록 빈 응답 성공")
    void 보유량이_남은_OWN_재료가_없는_경우_남은_재료_우선_목록_빈_응답_성공() {
        // given
        RecipeSearchRequestDto request = createRequest("1234");
        User user = User.builder().id(1L).loginNumber("1234").build();
        Slice<RecipeSearchResult> emptySlice = new SliceImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                false
        );

        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(userFoodIngredientRepository.findFoodIngredientsByTotalPrimaryAmountDesc(
                user.getId(),
                UserFoodIngredientType.OWN,
                PageRequest.of(0, 1)
        )).willReturn(List.of());
        for (RecommendationType type : List.of(
                RecommendationType.SIMPLE,
                RecommendationType.DIET,
                RecommendationType.GLUTEN_FREE
        )) {
            given(recipeRepository.searchRecipes(user.getId(), request, type))
                    .willReturn(emptySlice);
        }

        // when
        RecipeSearchResponseDto result = recipeService.getRecipes(request);

        // then
        assertThat(result.remainFoodIngredient().content()).isEmpty();
        assertThat(result.remainFoodIngredientName()).isNull();
        assertThat(result.remainFoodIngredient().size()).isEqualTo(10);
        assertThat(result.remainFoodIngredient().hasNext()).isFalse();
        assertThat(result.remainFoodIngredient().nextCursor()).isNull();
        verify(recipeRepository, never()).searchRecipesByFoodIngredient(
                anyLong(),
                anyLong(),
                any(RecipeSearchRequestDto.class)
        );
    }

    @Test
    @DisplayName("존재하지 않는 사용자 전체 레시피 조회 실패")
    void 존재하지_않는_사용자_전체_레시피_조회_실패() {
        // given
        RecipeSearchRequestDto request = createRequest("9999");
        given(userRepository.findByLoginNumber("9999")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> recipeService.getRecipes(request))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("검색어 기반 레시피 검색 성공")
    void 검색어_기반_레시피_검색_성공() {
        // given
        RecipeKeywordSearchReqDto request = new RecipeKeywordSearchReqDto(
                "1234", " 감자 ", 1L, 10, null, null, null, RecipeSortType.DEFAULT
        );
        User user = User.builder().id(1L).loginNumber("1234").build();
        RecipeSearchResult searchResult = new RecipeSearchResult(
                10L, 20L, "감자미역국", "thumbnail", 35.4, 20,
                DifficultyLevel.LEVEL_2, MenuType.KOREAN, 0L, 2L, 2L, 0L, true
        );
        RecipeKeywordSearchResDto responseDto = RecipeKeywordSearchResDto.builder()
                .recipeId(10L)
                .menuName("감자미역국")
                .foodIngredientUsingPercent(100L)
                .isFavoriteRecipe(true)
                .build();
        Slice<RecipeSearchResult> slice = new SliceImpl<>(
                List.of(searchResult), PageRequest.of(0, 10), true
        );

        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(recipeRepository.searchRecipesByMenuName(1L, "감자", request)).willReturn(slice);
        given(recipeMapper.toRecipeKeywordSearchResDto(searchResult)).willReturn(responseDto);

        // when
        CursorSliceResponse<RecipeKeywordSearchResDto> result =
                recipeService.searchRecipesByMenuName(request);

        // then
        assertThat(result.content()).containsExactly(responseDto);
        assertThat(result.content().getFirst().foodIngredientUsingPercent()).isEqualTo(100L);
        assertThat(result.content().getFirst().isFavoriteRecipe()).isTrue();
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(11L);
        ArgumentCaptor<UserSearchHistory> historyCaptor =
                ArgumentCaptor.forClass(UserSearchHistory.class);
        verify(userSearchHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getContent()).isEqualTo("감자");
        assertThat(historyCaptor.getValue().getUser()).isEqualTo(user);
        verify(recipeRepository).searchRecipesByMenuName(1L, "감자", request);
        verify(recipeMapper).toRecipeKeywordSearchResDto(searchResult);
    }

    @Test
    @DisplayName("검색어 기반 레시피 검색 마지막 페이지 성공")
    void 검색어_기반_레시피_검색_마지막_페이지_성공() {
        // given
        RecipeKeywordSearchReqDto request = new RecipeKeywordSearchReqDto(
                "1234", "감자", 11L, 10, null, null, null, RecipeSortType.DEFAULT
        );
        User user = User.builder().id(1L).loginNumber("1234").build();
        Slice<RecipeSearchResult> slice = new SliceImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                false
        );

        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(recipeRepository.searchRecipesByMenuName(1L, "감자", request)).willReturn(slice);

        // when
        CursorSliceResponse<RecipeKeywordSearchResDto> result =
                recipeService.searchRecipesByMenuName(request);

        // then
        assertThat(result.content()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        verify(userSearchHistoryRepository, never()).save(any(UserSearchHistory.class));
    }

    @Test
    @DisplayName("검색어 기반 레시피 검색 실패 - 존재하지 않는 사용자")
    void 검색어_기반_레시피_검색_실패_존재하지_않는_사용자() {
        // given
        RecipeKeywordSearchReqDto request = new RecipeKeywordSearchReqDto(
                "9999", "감자", 1L, 10, null, null, null, RecipeSortType.DEFAULT
        );
        given(userRepository.findByLoginNumber("9999")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> recipeService.searchRecipesByMenuName(request))
                .isInstanceOf(CustomException.class);
        verify(userRepository).findByLoginNumber("9999");
        verify(userSearchHistoryRepository, never()).save(any(UserSearchHistory.class));
    }

    private RecipeSearchRequestDto createRequest(String userNumber) {
        return new RecipeSearchRequestDto(
                userNumber, 1L, 10, null, null, null, RecipeSortType.DEFAULT
        );
    }
}
