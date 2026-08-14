package com.likelion.routineeatbe.domain.cookingRecord.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto.GeneratedCookingStep;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingResultSaveResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingStepStage;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CookingRecordMapperTest {

    private final CookingRecordMapper cookingRecordMapper = new CookingRecordMapper();

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
