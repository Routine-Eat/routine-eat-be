package com.likelion.routineeatbe.domain.cookingRecord.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto.GeneratedCookingStep;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordFoodIngredientsResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingResultSaveResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecordFoodIngredient;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingStepStage;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CookingRecordMapperTest {

    private final CookingRecordMapper cookingRecordMapper = new CookingRecordMapper();

    @Test
    @DisplayName("현재 사용자 보유량에서 요리 사용량을 차감한 예상량을 응답으로 변환한다")
    void 사용자_보유량과_요리_후_예상량_응답_변환_성공() {
        // given
        FoodIngredient egg = FoodIngredient.builder()
                .id(20L)
                .name("계란")
                .primaryUnit(PrimaryUnit.G)
                .secondaryUnit(SecondaryUnit.AL)
                .build();
        Recipe recipe = Recipe.builder().id(30L).build();
        RecipeFoodIngredient recipeFoodIngredient = RecipeFoodIngredient.builder()
                .id(40L)
                .recipe(recipe)
                .foodIngredient(egg)
                .primaryNeedAmountValue(80.0)
                .secondaryNeedAmountValue(3.0)
                .build();
        CookingRecord cookingRecord = CookingRecord.builder().id(10L).build();
        CookingRecordFoodIngredient.create(cookingRecord, egg, 160.0, 6.0);
        UserFoodIngredient firstOwnedIngredient = UserFoodIngredient.builder()
                .id(50L)
                .foodIngredient(egg)
                .primaryAmountValue(200.0)
                .secondaryAmountValue(3.0)
                .build();
        UserFoodIngredient secondOwnedIngredient = UserFoodIngredient.builder()
                .id(51L)
                .foodIngredient(egg)
                .primaryAmountValue(100.0)
                .secondaryAmountValue(5.0)
                .build();

        // when
        CookingRecordFoodIngredientsResDto result = cookingRecordMapper
                .toCookingRecordFoodIngredientsResDto(
                        cookingRecord,
                        List.of(recipeFoodIngredient),
                        List.of(firstOwnedIngredient, secondOwnedIngredient)
                );

        // then
        assertThat(result.recipeFoodIngredients()).singleElement().satisfies(ingredient -> {
            assertThat(ingredient.id()).isEqualTo(40L);
            assertThat(ingredient.name()).isEqualTo("계란");
            assertThat(ingredient.prevPrimaryAmountValue()).isEqualTo(300.0);
            assertThat(ingredient.currentPrimaryAmountValue()).isEqualTo(140.0);
            assertThat(ingredient.primaryUnit()).isEqualTo(PrimaryUnit.G);
            assertThat(ingredient.prevSecondaryAmountValue()).isEqualTo(8.0);
            assertThat(ingredient.currentSecondaryAmountValue()).isEqualTo(2.0);
            assertThat(ingredient.secondaryUnit()).isEqualTo(SecondaryUnit.AL);
        });
    }

    @Test
    @DisplayName("보조 단위 필요량이 없으면 보조 단위 응답을 null로 변환한다")
    void 보조_단위_필요량_없음_응답_변환_성공() {
        // given
        FoodIngredient salt = FoodIngredient.builder()
                .id(21L)
                .name("소금")
                .primaryUnit(PrimaryUnit.G)
                .secondaryUnit(SecondaryUnit.PINCH)
                .build();
        RecipeFoodIngredient recipeFoodIngredient = RecipeFoodIngredient.builder()
                .id(41L)
                .foodIngredient(salt)
                .primaryNeedAmountValue(5.0)
                .secondaryNeedAmountValue(null)
                .build();
        CookingRecord cookingRecord = CookingRecord.builder().id(11L).build();
        CookingRecordFoodIngredient.create(cookingRecord, salt, 20.0, null);
        UserFoodIngredient ownedIngredient = UserFoodIngredient.builder()
                .id(52L)
                .foodIngredient(salt)
                .primaryAmountValue(15.0)
                .secondaryAmountValue(2.0)
                .build();

        // when
        CookingRecordFoodIngredientsResDto result = cookingRecordMapper
                .toCookingRecordFoodIngredientsResDto(
                        cookingRecord,
                        List.of(recipeFoodIngredient),
                        List.of(ownedIngredient)
                );

        // then
        assertThat(result.recipeFoodIngredients()).singleElement().satisfies(ingredient -> {
            assertThat(ingredient.prevPrimaryAmountValue()).isEqualTo(15.0);
            assertThat(ingredient.currentPrimaryAmountValue()).isZero();
            assertThat(ingredient.prevSecondaryAmountValue()).isNull();
            assertThat(ingredient.currentSecondaryAmountValue()).isNull();
            assertThat(ingredient.secondaryUnit()).isNull();
        });
    }

    @Test
    @DisplayName("저장된 요리 기록 PK를 요리 결과 저장 응답으로 변환한다")
    void toCookingResultSaveResDto_success() {
        // given
        CookingRecord cookingRecord = CookingRecord.builder().id(10L).build();

        // when
        CookingResultSaveResDto result = cookingRecordMapper
                .toCookingResultSaveResDto(cookingRecord);

        // then
        assertThat(result.savedCookingRecordId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("요리 시작 응답에 첫 번째 요리 단계 상세 정보를 포함한다")
    void 요리_시작_응답_첫_단계_상세_변환_성공() {
        // given
        CookingRecord cookingRecord = CookingRecord.builder().id(10L).build();
        CookingSession cookingSession = CookingSession.builder()
                .id(100L)
                .status(CookingSessionStatus.IN_PROGRESS)
                .cookingStepCount(3)
                .currentCookingStepLevel(1)
                .cookingRecord(cookingRecord)
                .build();
        cookingRecord.assignCookingSession(cookingSession);
        CookingStep firstCookingStep = CookingStep.builder()
                .id(19L)
                .level(1L)
                .title("재료 준비")
                .content("대파를 잘라주세요.")
                .subContent("가위를 사용해도 괜찮아요.")
                .cookingSession(cookingSession)
                .build();
        Recipe recipe = Recipe.builder()
                .menu(Menu.builder()
                        .name("계란 대파 볶음밥")
                        .thumbnailUrl("https://example.com/recipe.jpg")
                        .timeRequired(8)
                        .build())
                .build();
        CookingStepGenerateGeminiResponseDto generated =
                CookingStepGenerateGeminiResponseDto.create(
                        List.of("손을 씻으세요."),
                        List.of(
                                GeneratedCookingStep.create(
                                        1,
                                        CookingStepStage.PREPARATION,
                                        "재료 준비",
                                        "대파를 잘라주세요.",
                                        "가위를 사용해도 괜찮아요."
                                ),
                                GeneratedCookingStep.create(
                                        2,
                                        CookingStepStage.COOKING,
                                        "조리",
                                        "볶아주세요.",
                                        null
                                ),
                                GeneratedCookingStep.create(
                                        3,
                                        CookingStepStage.FINISH,
                                        "완료",
                                        "불을 꺼주세요.",
                                        null
                                )
                        )
                );

        // when
        CookingStartResDto result = cookingRecordMapper.toCookingStartResDto(
                cookingRecord,
                recipe,
                generated,
                firstCookingStep
        );

        // then
        assertThat(result.cookingStepCount()).isEqualTo(3);
        assertThat(result.prevCookingStepLevel()).isZero();
        assertThat(result.nextCookingStepLevel()).isEqualTo(2);
        assertThat(result.currentCookingStep().cookingStepId()).isEqualTo(19L);
        assertThat(result.currentCookingStep().level()).isEqualTo(1L);
        assertThat(result.currentCookingStep().stepTips()).isEmpty();
        assertThat(result.cookingStepTitles()).hasSize(3);
    }

    @Test
    @DisplayName("현재 요리 단계와 앞뒤 단계 정보를 단계 이동 응답으로 변환한다")
    void 요리_단계_이동_응답_변환_성공() {
        // given
        CookingSession cookingSession = CookingSession.builder()
                .id(100L)
                .status(CookingSessionStatus.IN_PROGRESS)
                .cookingStepCount(3)
                .currentCookingStepLevel(1)
                .build();
        CookingStep cookingStep = CookingStep.builder()
                .id(19L)
                .level(1L)
                .title("재료 준비")
                .thumbnailUrl("https://example.com/step.jpg")
                .content("대파를 잘라주세요.")
                .subContent("가위를 사용해도 괜찮아요.")
                .cookingSession(cookingSession)
                .build();

        // when
        CookingStepNavigationResDto result = cookingRecordMapper
                .toCookingStepNavigationResDto(cookingSession, cookingStep);

        // then
        assertThat(result.cookingStepCount()).isEqualTo(3);
        assertThat(result.prevCookingStepLevel()).isZero();
        assertThat(result.nextCookingStepLevel()).isEqualTo(2);
        assertThat(result.currentCookingStep().cookingStepId()).isEqualTo(19L);
        assertThat(result.currentCookingStep().level()).isEqualTo(1L);
        assertThat(result.currentCookingStep().stepTips()).isEmpty();
    }
}
