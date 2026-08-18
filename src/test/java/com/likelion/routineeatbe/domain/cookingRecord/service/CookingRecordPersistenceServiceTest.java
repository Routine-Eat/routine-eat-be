package com.likelion.routineeatbe.domain.cookingRecord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto.GeneratedCookingStep;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.ModifiedCookingRecordFoodIngredientReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecordFoodIngredient;
import com.likelion.routineeatbe.domain.cookingRecord.enums.TasteRating;
import com.likelion.routineeatbe.domain.cookingRecord.exception.CookingRecordErrorCode;
import com.likelion.routineeatbe.domain.cookingRecord.repository.CookingRecordRepository;
import com.likelion.routineeatbe.domain.cookingTip.entity.CookingTip;
import com.likelion.routineeatbe.domain.cookingTip.repository.CookingTipRepository;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingStepStage;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.repository.RecipeFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import com.likelion.routineeatbe.domain.user.repository.UserFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CookingRecordPersistenceServiceTest {

    @InjectMocks
    private CookingRecordPersistenceService persistenceService;

    @Mock private UserRepository userRepository;
    @Mock private RecipeRepository recipeRepository;
    @Mock private RecipeFoodIngredientRepository recipeFoodIngredientRepository;
    @Mock private CookingRecordRepository cookingRecordRepository;
    @Mock private UserFoodIngredientRepository userFoodIngredientRepository;
    @Mock private CookingTipRepository cookingTipRepository;

    @Test
    @DisplayName("수정 목록이 비어 있으면 초기 사용량으로 재고를 차감한다")
    void 수정_목록_비어있음_초기_사용량_차감_성공() {
        // given
        CookingRecord cookingRecord = CookingRecord.builder().id(10L).build();
        FoodIngredient foodIngredient = FoodIngredient.builder().id(20L).build();
        CookingRecordFoodIngredient.create(
                cookingRecord,
                foodIngredient,
                100.0,
                1.0
        );
        CookingSession cookingSession = CookingSession.builder()
                .id(100L)
                .status(CookingSessionStatus.COMPLETED)
                .cookingStepCount(3)
                .currentCookingStepLevel(3)
                .cookingRecord(cookingRecord)
                .build();
        cookingRecord.assignCookingSession(cookingSession);
        given(cookingRecordRepository.findByIdAndUserIdForUpdate(10L, 1L))
                .willReturn(Optional.of(cookingRecord));
        UserFoodIngredient ownedFoodIngredient = UserFoodIngredient.builder()
                .id(30L)
                .relationType(UserFoodIngredientType.OWN)
                .primaryAmountValue(300.0)
                .secondaryAmountValue(3.0)
                .foodIngredient(foodIngredient)
                .build();
        given(userFoodIngredientRepository
                .findAllForUpdateByUserIdAndRelationTypeAndFoodIngredientIds(
                        1L,
                        UserFoodIngredientType.OWN,
                        List.of(20L)
                ))
                .willReturn(List.of(ownedFoodIngredient));

        // when
        CookingRecord result = persistenceService.saveCookingResult(
                1L,
                10L,
                TasteRating.LEVEL_3,
                DifficultyLevel.LEVEL_2,
                "참기름을 조금 더 넣으면 맛있습니다.",
                List.of(),
                "https://api-img.nahjjun.cloud/1/10/result.jpg"
        );

        // then
        assertThat(result.getTasteRating()).isEqualTo(TasteRating.LEVEL_3);
        assertThat(result.getDifficultyLevel()).isEqualTo(DifficultyLevel.LEVEL_2);
        assertThat(result.getCookingTip()).isEqualTo("참기름을 조금 더 넣으면 맛있습니다.");
        assertThat(result.getPhotoUrl())
                .isEqualTo("https://api-img.nahjjun.cloud/1/10/result.jpg");
        assertThat(result.getCookingSession().getStatus())
                .isEqualTo(CookingSessionStatus.TERMINATED);
        assertThat(ownedFoodIngredient.getPrimaryAmountValue()).isEqualTo(200.0);
        assertThat(ownedFoodIngredient.getSecondaryAmountValue()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("수정된 주 단위 사용량과 기존 보조 단위 사용량으로 재고를 차감한다")
    void 수정된_사용량_기준_재고_차감_성공() {
        // given
        CookingRecord cookingRecord = CookingRecord.builder().id(10L).build();
        FoodIngredient foodIngredient = FoodIngredient.builder().id(20L).build();
        CookingRecordFoodIngredient usedFoodIngredient =
                CookingRecordFoodIngredient.builder()
                        .id(40L)
                        .primaryUsedAmountValue(100.0)
                        .secondaryUsedAmountValue(1.0)
                        .cookingRecord(cookingRecord)
                        .foodIngredient(foodIngredient)
                        .build();
        cookingRecord.addFoodIngredient(usedFoodIngredient);
        CookingSession cookingSession = CookingSession.builder()
                .id(100L)
                .status(CookingSessionStatus.COMPLETED)
                .cookingRecord(cookingRecord)
                .build();
        cookingRecord.assignCookingSession(cookingSession);
        UserFoodIngredient ownedFoodIngredient = UserFoodIngredient.builder()
                .id(30L)
                .relationType(UserFoodIngredientType.OWN)
                .primaryAmountValue(300.0)
                .secondaryAmountValue(3.0)
                .foodIngredient(foodIngredient)
                .build();
        given(cookingRecordRepository.findByIdAndUserIdForUpdate(10L, 1L))
                .willReturn(Optional.of(cookingRecord));
        given(userFoodIngredientRepository
                .findAllForUpdateByUserIdAndRelationTypeAndFoodIngredientIds(
                        1L,
                        UserFoodIngredientType.OWN,
                        List.of(20L)
                ))
                .willReturn(List.of(ownedFoodIngredient));

        // when
        persistenceService.saveCookingResult(
                1L,
                10L,
                TasteRating.LEVEL_3,
                DifficultyLevel.LEVEL_2,
                null,
                List.of(new ModifiedCookingRecordFoodIngredientReqDto(
                        40L,
                        120.0,
                        null
                )),
                null
        );

        // then
        assertThat(usedFoodIngredient.getPrimaryUsedAmountValue()).isEqualTo(120.0);
        assertThat(usedFoodIngredient.getSecondaryUsedAmountValue()).isEqualTo(1.0);
        assertThat(ownedFoodIngredient.getPrimaryAmountValue()).isEqualTo(180.0);
        assertThat(ownedFoodIngredient.getSecondaryAmountValue()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("현재 요리 기록에 속하지 않은 음식 재료 수정은 실패한다")
    void 음식_재료_사용량_수정_실패_현재_요리_기록에_속하지_않음() {
        // given
        CookingRecord cookingRecord = CookingRecord.builder().id(10L).build();
        FoodIngredient foodIngredient = FoodIngredient.builder().id(20L).build();
        CookingRecordFoodIngredient usedFoodIngredient =
                CookingRecordFoodIngredient.builder()
                        .id(40L)
                        .primaryUsedAmountValue(100.0)
                        .cookingRecord(cookingRecord)
                        .foodIngredient(foodIngredient)
                        .build();
        cookingRecord.addFoodIngredient(usedFoodIngredient);
        CookingSession cookingSession = CookingSession.builder()
                .id(100L)
                .status(CookingSessionStatus.COMPLETED)
                .cookingRecord(cookingRecord)
                .build();
        cookingRecord.assignCookingSession(cookingSession);
        given(cookingRecordRepository.findByIdAndUserIdForUpdate(10L, 1L))
                .willReturn(Optional.of(cookingRecord));

        // when & then
        assertThatThrownBy(() -> persistenceService.saveCookingResult(
                1L,
                10L,
                TasteRating.LEVEL_3,
                DifficultyLevel.LEVEL_2,
                null,
                List.of(new ModifiedCookingRecordFoodIngredientReqDto(
                        99L,
                        120.0,
                        null
                )),
                null
        )).isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingRecordErrorCode
                                .COOKING_RECORD_FOOD_INGREDIENT_NOT_FOUND));
        assertThat(usedFoodIngredient.getPrimaryUsedAmountValue()).isEqualTo(100.0);
        then(userFoodIngredientRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("동일한 요리 기록 음식 재료가 중복되면 수정에 실패한다")
    void 음식_재료_사용량_수정_실패_중복_ID() {
        // given
        CookingRecord cookingRecord = CookingRecord.builder().id(10L).build();
        FoodIngredient foodIngredient = FoodIngredient.builder().id(20L).build();
        CookingRecordFoodIngredient usedFoodIngredient =
                CookingRecordFoodIngredient.builder()
                        .id(40L)
                        .primaryUsedAmountValue(100.0)
                        .cookingRecord(cookingRecord)
                        .foodIngredient(foodIngredient)
                        .build();
        cookingRecord.addFoodIngredient(usedFoodIngredient);
        CookingSession cookingSession = CookingSession.builder()
                .id(100L)
                .status(CookingSessionStatus.COMPLETED)
                .cookingRecord(cookingRecord)
                .build();
        cookingRecord.assignCookingSession(cookingSession);
        given(cookingRecordRepository.findByIdAndUserIdForUpdate(10L, 1L))
                .willReturn(Optional.of(cookingRecord));
        ModifiedCookingRecordFoodIngredientReqDto modifiedFoodIngredient =
                new ModifiedCookingRecordFoodIngredientReqDto(40L, 120.0, null);

        // when & then
        assertThatThrownBy(() -> persistenceService.saveCookingResult(
                1L,
                10L,
                TasteRating.LEVEL_3,
                DifficultyLevel.LEVEL_2,
                null,
                List.of(modifiedFoodIngredient, modifiedFoodIngredient),
                null
        )).isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingRecordErrorCode
                                .DUPLICATE_COOKING_RECORD_FOOD_INGREDIENT));
        assertThat(usedFoodIngredient.getPrimaryUsedAmountValue()).isEqualTo(100.0);
        then(userFoodIngredientRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이미지 없는 회고 재저장은 기존 이미지 URL을 유지한다")
    void 이미지_없는_회고_재저장_기존_URL_유지_성공() {
        // given
        CookingRecord cookingRecord = CookingRecord.builder()
                .id(10L)
                .photoUrl("https://api-img.nahjjun.cloud/1/10/result.jpg")
                .build();
        CookingSession cookingSession = CookingSession.builder()
                .id(100L)
                .status(CookingSessionStatus.COMPLETED)
                .cookingStepCount(3)
                .currentCookingStepLevel(3)
                .cookingRecord(cookingRecord)
                .build();
        cookingRecord.assignCookingSession(cookingSession);
        given(cookingRecordRepository.findByIdAndUserIdForUpdate(10L, 1L))
                .willReturn(Optional.of(cookingRecord));

        // when
        CookingRecord result = persistenceService.saveCookingResult(
                1L,
                10L,
                TasteRating.LEVEL_2,
                DifficultyLevel.LEVEL_1,
                null,
                List.of(),
                null
        );

        // then
        assertThat(result.getPhotoUrl())
                .isEqualTo("https://api-img.nahjjun.cloud/1/10/result.jpg");
        assertThat(result.getCookingSession().getStatus())
                .isEqualTo(CookingSessionStatus.TERMINATED);
    }

    @Test
    @DisplayName("진행 중인 요리 기록은 회고 저장에 실패한다")
    void 진행_중_요리_기록_회고_저장_실패() {
        // given
        CookingRecord cookingRecord = CookingRecord.builder().id(10L).build();
        CookingSession cookingSession = CookingSession.builder()
                .id(100L)
                .status(CookingSessionStatus.IN_PROGRESS)
                .cookingStepCount(3)
                .currentCookingStepLevel(2)
                .cookingRecord(cookingRecord)
                .build();
        cookingRecord.assignCookingSession(cookingSession);
        given(cookingRecordRepository.findByIdAndUserIdForUpdate(10L, 1L))
                .willReturn(Optional.of(cookingRecord));

        // when & then
        assertThatThrownBy(() -> persistenceService.saveCookingResult(
                1L,
                10L,
                TasteRating.LEVEL_1,
                DifficultyLevel.LEVEL_5,
                null,
                List.of(),
                null
        )).isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingRecordErrorCode.COOKING_SESSION_NOT_COMPLETED));
    }

    @Test
    @DisplayName("체크리스트를 level 0으로 저장하고 실제 단계 수만 세션에 기록한다")
    void 체크리스트_level_0_저장_성공() {
        // given
        User user = User.builder().id(1L).build();
        Recipe recipe = Recipe.builder().id(2L).build();
        FoodIngredient greenOnion = FoodIngredient.builder().id(10L).build();
        FoodIngredient egg = FoodIngredient.builder().id(11L).build();
        List<RecipeFoodIngredient> recipeFoodIngredients = List.of(
                RecipeFoodIngredient.builder()
                        .recipe(recipe)
                        .foodIngredient(greenOnion)
                        .primaryNeedAmountValue(50.0)
                        .secondaryNeedAmountValue(0.5)
                        .build(),
                RecipeFoodIngredient.builder()
                        .recipe(recipe)
                        .foodIngredient(egg)
                        .primaryNeedAmountValue(60.0)
                        .secondaryNeedAmountValue(null)
                        .build()
        );
        CookingStepGenerateGeminiResponseDto generated = CookingStepGenerateGeminiResponseDto.create(
                List.of("손을 씻으세요.", "도구를 확인하세요."),
                List.of(
                        GeneratedCookingStep.create(
                                1,
                                CookingStepStage.PREPARATION,
                                "준비",
                                "준비",
                                null,
                                List.of(100L),
                                List.of(10L)
                        ),
                        GeneratedCookingStep.create(
                                2,
                                CookingStepStage.COOKING,
                                "조리",
                                "조리",
                                null,
                                List.of(),
                                List.of(11L)
                        ),
                        GeneratedCookingStep.create(
                                3,
                                CookingStepStage.FINISH,
                                "완료",
                                "완료",
                                null,
                                List.of(),
                                List.of()
                        )
                )
        );
        CookingTip cookingTip = CookingTip.builder()
                .id(100L)
                .title("대파 써는 법")
                .build();
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(recipeRepository.findById(2L)).willReturn(Optional.of(recipe));
        given(cookingRecordRepository.existsBlockingSession(eq(1L), eq(2L), anyCollection()))
                .willReturn(false);
        given(recipeFoodIngredientRepository.findAllByRecipeIdInWithFoodIngredient(List.of(2L)))
                .willReturn(recipeFoodIngredients);
        given(cookingTipRepository.findAllById(anyCollection())).willReturn(List.of(cookingTip));
        given(cookingRecordRepository.saveAndFlush(any(CookingRecord.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        CookingRecord result = persistenceService.save(1L, 2L, 2, generated);

        // then
        assertThat(result.getCookingSession().getStatus())
                .isEqualTo(CookingSessionStatus.IN_PROGRESS);
        assertThat(result.getCookingSession().getCookingStepCount()).isEqualTo(3);
        assertThat(result.getCookingSession().getCookingSteps()).hasSize(5);
        assertThat(result.getCookingSession().getCookingSteps())
                .filteredOn(cookingStep -> cookingStep.getLevel() == 0L)
                .extracting(CookingStep::getContent)
                .containsExactly("손을 씻으세요.", "도구를 확인하세요.");
        assertThat(result.getCookingSession().getCookingSteps())
                .filteredOn(cookingStep -> cookingStep.getLevel() == 1L)
                .singleElement()
                .satisfies(cookingStep -> {
                    assertThat(cookingStep.getCookingStepTips())
                            .singleElement()
                            .satisfies(cookingStepTip ->
                                    assertThat(cookingStepTip.getCookingTip())
                                            .isSameAs(cookingTip));
                    assertThat(cookingStep.getCookingStepFoodIngredients())
                            .singleElement()
                            .satisfies(cookingStepFoodIngredient -> assertThat(
                                    cookingStepFoodIngredient
                                            .getCookingRecordFoodIngredient()
                                            .getFoodIngredient()
                                            .getId()
                            ).isEqualTo(10L));
                });
        assertThat(result.getFoodIngredients()).hasSize(2);
        assertThat(result.getFoodIngredients())
                .extracting(
                        CookingRecordFoodIngredient::getPrimaryUsedAmountValue,
                        CookingRecordFoodIngredient::getSecondaryUsedAmountValue
                )
                .containsExactlyInAnyOrder(
                        tuple(100.0, 1.0),
                        tuple(120.0, null)
                );
        assertThat(result.getFoodIngredients())
                .extracting(foodIngredient -> foodIngredient.getFoodIngredient().getId())
                .containsExactlyInAnyOrder(10L, 11L);
    }

    @Test
    @DisplayName("Gemini가 선택한 요리 팁이 삭제되었으면 저장에 실패한다")
    void 요리_팁_조회_실패() {
        // given
        User user = User.builder().id(1L).build();
        Recipe recipe = Recipe.builder().id(2L).build();
        FoodIngredient foodIngredient = FoodIngredient.builder().id(10L).build();
        RecipeFoodIngredient recipeFoodIngredient = RecipeFoodIngredient.builder()
                .recipe(recipe)
                .foodIngredient(foodIngredient)
                .primaryNeedAmountValue(50.0)
                .build();
        CookingStepGenerateGeminiResponseDto generated =
                CookingStepGenerateGeminiResponseDto.create(
                        List.of("손을 씻으세요."),
                        List.of(GeneratedCookingStep.create(
                                1,
                                CookingStepStage.PREPARATION,
                                "준비",
                                "준비",
                                null,
                                List.of(999L),
                                List.of(10L)
                        ))
                );
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(recipeRepository.findById(2L)).willReturn(Optional.of(recipe));
        given(cookingRecordRepository.existsBlockingSession(eq(1L), eq(2L), anyCollection()))
                .willReturn(false);
        given(recipeFoodIngredientRepository.findAllByRecipeIdInWithFoodIngredient(List.of(2L)))
                .willReturn(List.of(recipeFoodIngredient));
        given(cookingTipRepository.findAllById(anyCollection())).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> persistenceService.save(1L, 2L, 1, generated))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingRecordErrorCode.COOKING_TIP_NOT_FOUND));
        then(cookingRecordRepository).should(never()).saveAndFlush(any(CookingRecord.class));
    }

    @Test
    @DisplayName("Gemini가 레시피에 없는 음식 재료를 선택하면 저장에 실패한다")
    void 단계별_음식_재료_조회_실패_레시피에_없는_재료() {
        // given
        User user = User.builder().id(1L).build();
        Recipe recipe = Recipe.builder().id(2L).build();
        FoodIngredient foodIngredient = FoodIngredient.builder().id(10L).build();
        RecipeFoodIngredient recipeFoodIngredient = RecipeFoodIngredient.builder()
                .recipe(recipe)
                .foodIngredient(foodIngredient)
                .primaryNeedAmountValue(50.0)
                .build();
        CookingStepGenerateGeminiResponseDto generated =
                CookingStepGenerateGeminiResponseDto.create(
                        List.of("손을 씻으세요."),
                        List.of(GeneratedCookingStep.create(
                                1,
                                CookingStepStage.PREPARATION,
                                "준비",
                                "준비",
                                null,
                                List.of(),
                                List.of(999L)
                        ))
                );
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(recipeRepository.findById(2L)).willReturn(Optional.of(recipe));
        given(cookingRecordRepository.existsBlockingSession(eq(1L), eq(2L), anyCollection()))
                .willReturn(false);
        given(recipeFoodIngredientRepository.findAllByRecipeIdInWithFoodIngredient(List.of(2L)))
                .willReturn(List.of(recipeFoodIngredient));

        // when & then
        assertThatThrownBy(() -> persistenceService.save(1L, 2L, 1, generated))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(
                                CookingRecordErrorCode
                                        .COOKING_RECORD_FOOD_INGREDIENT_NOT_FOUND
                        ));
        then(cookingRecordRepository).should(never()).saveAndFlush(any(CookingRecord.class));
    }

    @Test
    @DisplayName("잠금 획득 후 활성 세션이 확인되면 저장을 차단한다")
    void 잠금_후_활성_세션_확인_실패() {
        // given
        User user = User.builder().id(1L).build();
        Recipe recipe = Recipe.builder().id(2L).build();
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(recipeRepository.findById(2L)).willReturn(Optional.of(recipe));
        given(cookingRecordRepository.existsBlockingSession(eq(1L), eq(2L), anyCollection()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> persistenceService.save(
                1L,
                2L,
                1,
                CookingStepGenerateGeminiResponseDto.create(List.of("확인"), List.of())
        )).isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingRecordErrorCode.COOKING_ALREADY_STARTED));
        then(cookingRecordRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("레시피 음식 재료가 없으면 요리 시작 데이터 저장에 실패한다")
    void 레시피_음식_재료_없음_저장_실패() {
        // given
        User user = User.builder().id(1L).build();
        Recipe recipe = Recipe.builder().id(2L).build();
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(recipeRepository.findById(2L)).willReturn(Optional.of(recipe));
        given(cookingRecordRepository.existsBlockingSession(eq(1L), eq(2L), anyCollection()))
                .willReturn(false);
        given(recipeFoodIngredientRepository.findAllByRecipeIdInWithFoodIngredient(List.of(2L)))
                .willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> persistenceService.save(
                1L,
                2L,
                2,
                CookingStepGenerateGeminiResponseDto.create(List.of("확인"), List.of())
        )).isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingRecordErrorCode.RECIPE_FOOD_INGREDIENT_EMPTY));
        then(cookingRecordRepository).should(never()).saveAndFlush(any(CookingRecord.class));
    }
}
