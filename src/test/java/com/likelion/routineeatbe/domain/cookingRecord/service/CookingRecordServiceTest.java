package com.likelion.routineeatbe.domain.cookingRecord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto.GeneratedCookingStep;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingResultSaveReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingStartReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordFoodIngredientsResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingResultSaveResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepDetailResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecordFoodIngredient;
import com.likelion.routineeatbe.domain.cookingRecord.enums.TasteRating;
import com.likelion.routineeatbe.domain.cookingRecord.exception.CookingRecordErrorCode;
import com.likelion.routineeatbe.domain.cookingRecord.mapper.CookingRecordMapper;
import com.likelion.routineeatbe.domain.cookingRecord.repository.CookingRecordRepository;
import com.likelion.routineeatbe.domain.cookingRecord.service.gemini.CookingStepGenerateGeminiService;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingStepStage;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import com.likelion.routineeatbe.domain.cookingSession.repository.CookingStepRepository;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.entity.RecipeStep;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeStepRepository;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.repository.RecipeFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class CookingRecordServiceTest {

    @InjectMocks
    private CookingRecordService cookingRecordService;

    @Mock private UserRepository userRepository;
    @Mock private RecipeRepository recipeRepository;
    @Mock private RecipeStepRepository recipeStepRepository;
    @Mock private RecipeFoodIngredientRepository recipeFoodIngredientRepository;
    @Mock private CookingRecordRepository cookingRecordRepository;
    @Mock private CookingStepRepository cookingStepRepository;
    @Mock private CookingStepGenerateGeminiService geminiService;
    @Mock private CookingRecordPersistenceService persistenceService;
    @Mock private CookingRecordImageStorageService imageStorageService;
    @Mock private CookingRecordMapper cookingRecordMapper;
    @Mock private UserFoodIngredientRepository userFoodIngredientRepository;

    @Test
    @DisplayName("레시피 기본 필요량과 요리 기록 사용량을 조회한다")
    void 요리_사용_음식_재료_양_조회_성공() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        Recipe recipe = Recipe.builder().id(2L).build();
        FoodIngredient foodIngredient = FoodIngredient.builder().id(3L).build();
        CookingRecord cookingRecord = CookingRecord.builder()
                .id(10L)
                .user(user)
                .recipe(recipe)
                .build();
        CookingRecordFoodIngredient.create(cookingRecord, foodIngredient, 100.0, null);
        CookingSession cookingSession = CookingSession.builder()
                .id(30L)
                .status(CookingSessionStatus.COMPLETED)
                .cookingRecord(cookingRecord)
                .build();
        cookingRecord.assignCookingSession(cookingSession);
        RecipeFoodIngredient recipeFoodIngredient = RecipeFoodIngredient.builder()
                .id(20L)
                .recipe(recipe)
                .foodIngredient(foodIngredient)
                .build();
        CookingRecordFoodIngredientsResDto expected =
                CookingRecordFoodIngredientsResDto.create(List.of());
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(cookingRecordRepository.findByIdAndUserIdWithFoodIngredients(10L, 1L))
                .willReturn(Optional.of(cookingRecord));
        given(recipeFoodIngredientRepository.findAllByRecipeIdInWithFoodIngredient(List.of(2L)))
                .willReturn(List.of(recipeFoodIngredient));
        given(userFoodIngredientRepository
                .findAllWithFoodIngredientByUserIdAndRelationTypeAndFoodIngredientIds(
                        1L,
                        UserFoodIngredientType.OWN,
                        List.of(3L)
                ))
                .willReturn(List.of());
        given(cookingRecordMapper.toCookingRecordFoodIngredientsResDto(
                cookingRecord,
                List.of(recipeFoodIngredient),
                List.of()
        )).willReturn(expected);

        // when
        CookingRecordFoodIngredientsResDto result = cookingRecordService.getFoodIngredients(
                10L,
                "1234"
        );

        // then
        assertThat(result).isSameAs(expected);
        then(cookingRecordMapper).should().toCookingRecordFoodIngredientsResDto(
                cookingRecord,
                List.of(recipeFoodIngredient),
                List.of()
        );
    }

    @Test
    @DisplayName("완료되지 않은 요리 세션이면 음식 재료 양 조회에 실패한다")
    void 요리_사용_음식_재료_양_조회_실패_완료되지_않은_세션() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        CookingRecord cookingRecord = CookingRecord.builder().id(10L).user(user).build();
        CookingSession cookingSession = CookingSession.builder()
                .id(30L)
                .status(CookingSessionStatus.IN_PROGRESS)
                .cookingRecord(cookingRecord)
                .build();
        cookingRecord.assignCookingSession(cookingSession);
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(cookingRecordRepository.findByIdAndUserIdWithFoodIngredients(10L, 1L))
                .willReturn(Optional.of(cookingRecord));

        // when & then
        assertThatThrownBy(() -> cookingRecordService.getFoodIngredients(10L, "1234"))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingRecordErrorCode.COOKING_SESSION_NOT_COMPLETED));
        then(recipeFoodIngredientRepository).shouldHaveNoInteractions();
        then(userFoodIngredientRepository).shouldHaveNoInteractions();
        then(cookingRecordMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("다른 사용자의 요리 기록이면 음식 재료 양 조회에 실패한다")
    void 요리_사용_음식_재료_양_조회_실패_다른_사용자_기록() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(cookingRecordRepository.findByIdAndUserIdWithFoodIngredients(10L, 1L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cookingRecordService.getFoodIngredients(10L, "1234"))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingRecordErrorCode.COOKING_RECORD_NOT_FOUND));
        then(recipeFoodIngredientRepository).shouldHaveNoInteractions();
        then(cookingRecordMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("최근 완료 요리 기록에 이미지 없이 회고를 저장한다")
    void 최근_완료_요리_기록_이미지_없이_회고_저장_성공() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        CookingRecord cookingRecord = createCookingRecord(10L, user, 3, 3);
        cookingRecord.getCookingSession().complete();
        CookingResultSaveReqDto request = new CookingResultSaveReqDto(
                TasteRating.LEVEL_3,
                DifficultyLevel.LEVEL_2
        );
        CookingResultSaveResDto expected = CookingResultSaveResDto.create(10L);
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(cookingRecordRepository
                .findFirstByUser_IdAndCookingSession_StatusOrderByCreatedAtDescIdDesc(
                        1L,
                        CookingSessionStatus.COMPLETED
                ))
                .willReturn(Optional.of(cookingRecord));
        given(persistenceService.saveCookingResult(
                1L,
                10L,
                TasteRating.LEVEL_3,
                DifficultyLevel.LEVEL_2,
                null
        )).willReturn(cookingRecord);
        given(cookingRecordMapper.toCookingResultSaveResDto(cookingRecord))
                .willReturn(expected);

        // when
        CookingResultSaveResDto result = cookingRecordService.saveCookingResult(
                "1234",
                request,
                null
        );

        // then
        assertThat(result).isSameAs(expected);
        then(imageStorageService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("요리 결과 이미지를 업로드하고 회고를 저장한다")
    void 요리_결과_이미지_업로드_회고_저장_성공() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        CookingRecord cookingRecord = createCookingRecord(10L, user, 3, 3);
        cookingRecord.getCookingSession().complete();
        CookingResultSaveReqDto request = new CookingResultSaveReqDto(
                TasteRating.LEVEL_2,
                DifficultyLevel.LEVEL_3
        );
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "result.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image-data".getBytes()
        );
        String photoUrl = "https://api-img.nahjjun.cloud/1/10/result.jpg";
        CookingResultSaveResDto expected = CookingResultSaveResDto.create(10L);
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(cookingRecordRepository
                .findFirstByUser_IdAndCookingSession_StatusOrderByCreatedAtDescIdDesc(
                        1L,
                        CookingSessionStatus.COMPLETED
                ))
                .willReturn(Optional.of(cookingRecord));
        given(imageStorageService.upload(1L, 10L, image)).willReturn(photoUrl);
        given(persistenceService.saveCookingResult(
                1L,
                10L,
                TasteRating.LEVEL_2,
                DifficultyLevel.LEVEL_3,
                photoUrl
        )).willReturn(cookingRecord);
        given(cookingRecordMapper.toCookingResultSaveResDto(cookingRecord))
                .willReturn(expected);

        // when
        CookingResultSaveResDto result = cookingRecordService.saveCookingResult(
                "1234",
                request,
                image
        );

        // then
        assertThat(result).isSameAs(expected);
        then(imageStorageService).should().upload(1L, 10L, image);
        then(imageStorageService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("완료된 요리 기록이 없으면 회고 저장에 실패한다")
    void 완료된_요리_기록_없음_회고_저장_실패() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        CookingResultSaveReqDto request = new CookingResultSaveReqDto(
                TasteRating.LEVEL_2,
                DifficultyLevel.LEVEL_2
        );
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(cookingRecordRepository
                .findFirstByUser_IdAndCookingSession_StatusOrderByCreatedAtDescIdDesc(
                        1L,
                        CookingSessionStatus.COMPLETED
                ))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cookingRecordService.saveCookingResult(
                "1234",
                request,
                null
        )).isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(
                                CookingRecordErrorCode.COMPLETED_COOKING_RECORD_NOT_FOUND
                        ));
        then(imageStorageService).shouldHaveNoInteractions();
        then(persistenceService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이미지 업로드 후 DB 저장 실패 시 S3 객체를 보상 삭제한다")
    void 이미지_업로드_후_DB_저장_실패_S3_보상_삭제() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        CookingRecord cookingRecord = createCookingRecord(10L, user, 3, 3);
        cookingRecord.getCookingSession().complete();
        CookingResultSaveReqDto request = new CookingResultSaveReqDto(
                TasteRating.LEVEL_1,
                DifficultyLevel.LEVEL_4
        );
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "result.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image-data".getBytes()
        );
        String photoUrl = "https://api-img.nahjjun.cloud/1/10/result.jpg";
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(cookingRecordRepository
                .findFirstByUser_IdAndCookingSession_StatusOrderByCreatedAtDescIdDesc(
                        1L,
                        CookingSessionStatus.COMPLETED
                ))
                .willReturn(Optional.of(cookingRecord));
        given(imageStorageService.upload(1L, 10L, image)).willReturn(photoUrl);
        given(persistenceService.saveCookingResult(
                1L,
                10L,
                TasteRating.LEVEL_1,
                DifficultyLevel.LEVEL_4,
                photoUrl
        )).willThrow(new CustomException(CookingRecordErrorCode.COOKING_RECORD_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> cookingRecordService.saveCookingResult(
                "1234",
                request,
                image
        )).isInstanceOf(CustomException.class);
        then(imageStorageService).should().delete(1L, 10L, "result.jpg");
    }

    @Test
    @DisplayName("사용자 맞춤 요리 단계를 생성하고 요리 기록을 저장한다")
    void 사용자_맞춤_요리_단계_생성_요리_기록_저장_성공() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        Recipe recipe = Recipe.builder().id(2L).menu(Menu.builder().name("볶음밥").build()).build();
        RecipeFoodIngredient ingredient = RecipeFoodIngredient.builder().build();
        RecipeStep recipeStep = RecipeStep.builder().level(1L).build();
        CookingStartReqDto request = new CookingStartReqDto(2L, 2);
        CookingStepGenerateGeminiResponseDto generated = generatedResponse();
        CookingRecord saved = createCookingRecord(3L, user, 1, 3);
        CookingStep firstCookingStep = CookingStep.builder()
                .id(20L)
                .level(1L)
                .title("준비")
                .content("재료를 준비하세요.")
                .cookingSession(saved.getCookingSession())
                .build();
        CookingStartResDto expected = CookingStartResDto.create(
                3L,
                "볶음밥",
                null,
                10,
                List.of("손을 씻으세요."),
                3,
                0,
                2,
                CookingStepDetailResDto.create(
                        20L,
                        1L,
                        "준비",
                        null,
                        "재료를 준비하세요.",
                        null,
                        List.of()
                ),
                List.of()
        );

        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(recipeRepository.findByIdWithMenu(2L)).willReturn(Optional.of(recipe));
        given(cookingRecordRepository.existsBlockingSession(eq(1L), eq(2L), anyCollection()))
                .willReturn(false);
        given(recipeFoodIngredientRepository.findAllByRecipeIdInWithFoodIngredient(List.of(2L)))
                .willReturn(List.of(ingredient));
        given(recipeStepRepository.findAllByRecipeIdOrderByLevelAsc(2L))
                .willReturn(List.of(recipeStep));
        given(geminiService.generate(user, recipe, List.of(ingredient), List.of(recipeStep), 2))
                .willReturn(generated);
        given(persistenceService.save(1L, 2L, 2, generated)).willReturn(saved);
        given(cookingStepRepository.findByCookingSessionIdAndLevel(100L, 1L))
                .willReturn(Optional.of(firstCookingStep));
        given(cookingRecordMapper.toCookingStartResDto(
                saved,
                recipe,
                generated,
                firstCookingStep
        ))
                .willReturn(expected);

        // when
        CookingStartResDto result = cookingRecordService.startCooking("1234", request);

        // then
        assertThat(result).isSameAs(expected);
        then(persistenceService).should().save(1L, 2L, 2, generated);
    }

    @Test
    @DisplayName("진행 중이거나 완료된 동일 요리가 있으면 Gemini 호출 전에 차단한다")
    void 동일_요리_활성_세션_존재_실패() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        Recipe recipe = Recipe.builder().id(2L).build();
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(recipeRepository.findByIdWithMenu(2L)).willReturn(Optional.of(recipe));
        given(cookingRecordRepository.existsBlockingSession(eq(1L), eq(2L), anyCollection()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> cookingRecordService.startCooking(
                "1234",
                new CookingStartReqDto(2L, 1)
        )).isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingRecordErrorCode.COOKING_ALREADY_STARTED));
        then(geminiService).shouldHaveNoInteractions();
        then(persistenceService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("진행 중인 요리 세션을 다음 단계로 이동한다")
    void 진행_중_요리_세션_다음_단계_이동_성공() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        CookingRecord cookingRecord = createCookingRecord(10L, user, 1, 3);
        CookingSession cookingSession = cookingRecord.getCookingSession();
        CookingStep cookingStep = CookingStep.builder()
                .id(20L)
                .level(2L)
                .title("조리")
                .content("볶아주세요.")
                .cookingSession(cookingSession)
                .build();
        CookingStepNavigationResDto expected = CookingStepNavigationResDto.create(
                3,
                1,
                3,
                CookingStepDetailResDto.create(
                        20L, 2L, "조리", null, "볶아주세요.", null, List.of()
                )
        );
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(cookingRecordRepository.findByIdAndUserIdForUpdate(10L, 1L))
                .willReturn(Optional.of(cookingRecord));
        given(cookingStepRepository.findByCookingSessionIdAndLevel(100L, 2L))
                .willReturn(Optional.of(cookingStep));
        given(cookingRecordMapper.toCookingStepNavigationResDto(cookingSession, cookingStep))
                .willReturn(expected);

        // when
        CookingStepNavigationResDto result = cookingRecordService.moveToNextCookingStep(
                10L,
                "1234"
        );

        // then
        assertThat(result).isSameAs(expected);
        assertThat(cookingSession.getCurrentCookingStepLevel()).isEqualTo(2);
        assertThat(cookingSession.getStatus()).isEqualTo(CookingSessionStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("마지막 요리 단계에서 다음 이동 시 세션을 완료한다")
    void 마지막_요리_단계_다음_이동_세션_완료_성공() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        CookingRecord cookingRecord = createCookingRecord(10L, user, 3, 3);
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(cookingRecordRepository.findByIdAndUserIdForUpdate(10L, 1L))
                .willReturn(Optional.of(cookingRecord));

        // when
        CookingStepNavigationResDto result = cookingRecordService.moveToNextCookingStep(
                10L,
                "1234"
        );

        // then
        assertThat(result).isNull();
        assertThat(cookingRecord.getCookingSession().getStatus())
                .isEqualTo(CookingSessionStatus.COMPLETED);
        then(cookingStepRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("완료된 요리 세션은 다음 단계 이동에 실패한다")
    void 완료된_요리_세션_다음_단계_이동_실패() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        CookingRecord cookingRecord = createCookingRecord(10L, user, 3, 3);
        cookingRecord.getCookingSession().complete();
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(cookingRecordRepository.findByIdAndUserIdForUpdate(10L, 1L))
                .willReturn(Optional.of(cookingRecord));

        // when & then
        assertThatThrownBy(() -> cookingRecordService.moveToNextCookingStep(10L, "1234"))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingRecordErrorCode.COOKING_SESSION_NOT_IN_PROGRESS));
    }

    @Test
    @DisplayName("진행 중인 요리 세션을 이전 단계로 이동한다")
    void 진행_중_요리_세션_이전_단계_이동_성공() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        CookingRecord cookingRecord = createCookingRecord(10L, user, 2, 3);
        CookingSession cookingSession = cookingRecord.getCookingSession();
        CookingStep cookingStep = CookingStep.builder()
                .id(19L)
                .level(1L)
                .title("재료 준비")
                .content("대파를 잘라주세요.")
                .cookingSession(cookingSession)
                .build();
        CookingStepNavigationResDto expected = CookingStepNavigationResDto.create(
                3,
                0,
                2,
                CookingStepDetailResDto.create(
                        19L,
                        1L,
                        "재료 준비",
                        null,
                        "대파를 잘라주세요.",
                        null,
                        List.of()
                )
        );
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(cookingRecordRepository.findByIdAndUserIdForUpdate(10L, 1L))
                .willReturn(Optional.of(cookingRecord));
        given(cookingStepRepository.findByCookingSessionIdAndLevel(100L, 1L))
                .willReturn(Optional.of(cookingStep));
        given(cookingRecordMapper.toCookingStepNavigationResDto(cookingSession, cookingStep))
                .willReturn(expected);

        // when
        CookingStepNavigationResDto result = cookingRecordService.moveToPreviousCookingStep(
                10L,
                "1234"
        );

        // then
        assertThat(result).isSameAs(expected);
        assertThat(cookingSession.getCurrentCookingStepLevel()).isEqualTo(1);
        assertThat(cookingSession.getStatus()).isEqualTo(CookingSessionStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("첫 요리 단계에서는 이전 단계로 이동하지 않는다")
    void 첫_요리_단계_이전_단계_이동_차단() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        CookingRecord cookingRecord = createCookingRecord(10L, user, 1, 3);
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(cookingRecordRepository.findByIdAndUserIdForUpdate(10L, 1L))
                .willReturn(Optional.of(cookingRecord));

        // when
        CookingStepNavigationResDto result = cookingRecordService
                .moveToPreviousCookingStep(10L, "1234");

        // then
        assertThat(result).isNull();
        assertThat(cookingRecord.getCookingSession().getCurrentCookingStepLevel()).isEqualTo(1);
        then(cookingStepRepository).shouldHaveNoInteractions();
        then(cookingRecordMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("완료된 요리 세션은 이전 단계 이동에 실패한다")
    void 완료된_요리_세션_이전_단계_이동_실패() {
        // given
        User user = User.builder().id(1L).loginNumber("1234").build();
        CookingRecord cookingRecord = createCookingRecord(10L, user, 3, 3);
        cookingRecord.getCookingSession().complete();
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(cookingRecordRepository.findByIdAndUserIdForUpdate(10L, 1L))
                .willReturn(Optional.of(cookingRecord));

        // when & then
        assertThatThrownBy(() -> cookingRecordService.moveToPreviousCookingStep(10L, "1234"))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingRecordErrorCode.COOKING_SESSION_NOT_IN_PROGRESS));
    }

    private CookingRecord createCookingRecord(
            Long cookingRecordId,
            User user,
            Integer currentLevel,
            Integer cookingStepCount
    ) {
        CookingRecord cookingRecord = CookingRecord.builder()
                .id(cookingRecordId)
                .user(user)
                .build();
        CookingSession cookingSession = CookingSession.builder()
                .id(100L)
                .status(CookingSessionStatus.IN_PROGRESS)
                .currentCookingStepLevel(currentLevel)
                .cookingStepCount(cookingStepCount)
                .cookingRecord(cookingRecord)
                .build();
        cookingRecord.assignCookingSession(cookingSession);
        return cookingRecord;
    }

    private CookingStepGenerateGeminiResponseDto generatedResponse() {
        return CookingStepGenerateGeminiResponseDto.create(
                List.of("손을 씻으세요."),
                List.of(
                        GeneratedCookingStep.create(1, CookingStepStage.PREPARATION, "준비", "준비", null),
                        GeneratedCookingStep.create(2, CookingStepStage.COOKING, "조리", "조리", null),
                        GeneratedCookingStep.create(3, CookingStepStage.FINISH, "완료", "완료", null)
                )
        );
    }
}
