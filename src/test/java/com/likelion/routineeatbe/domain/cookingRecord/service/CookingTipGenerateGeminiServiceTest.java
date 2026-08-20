package com.likelion.routineeatbe.domain.cookingRecord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingTipGenerateGeminiFunctionDeclarationDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingTipGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.service.gemini.CookingTipGenerateGeminiService;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.global.config.GeminiProperties;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import com.likelion.routineeatbe.global.util.GeminiRetryDelayStrategy;
import com.likelion.routineeatbe.global.util.GeminiUtil;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CookingTipGenerateGeminiServiceTest {

    @Mock private GeminiUtil geminiUtil;
    @Mock private GeminiRetryDelayStrategy retryDelayStrategy;

    private CookingTipGenerateGeminiService cookingTipGenerateGeminiService;

    @BeforeEach
    void setUp() {
        GeminiProperties properties = new GeminiProperties(
                "https://example.com/interactions",
                "test-key",
                "cooking-generate-model",
                "cooking-translate-model",
                "menu-model",
                "food-ingredient-model",
                "cooking-equipment-model",
                "cooking-step-model",
                10,
                new GeminiProperties.Retry(2, Duration.ofMillis(1), Duration.ofMillis(2), 0.0)
        );
        cookingTipGenerateGeminiService = new CookingTipGenerateGeminiService(
                geminiUtil,
                retryDelayStrategy,
                properties
        );
    }

    @Test
    @DisplayName("완료된 요리 단계를 바탕으로 한 줄 요리 팁을 생성한다")
    void 완료된_요리_단계_기반_한줄_요리_팁_생성_성공() {
        // given
        given(geminiUtil.callFunction(
                eq("cooking-step-model"),
                anyString(),
                any(CookingTipGenerateGeminiFunctionDeclarationDto.class),
                eq(CookingTipGenerateGeminiResponseDto.class)
        )).willReturn(CookingTipGenerateGeminiResponseDto.create("팬이 충분히 달궈진 뒤 재료를 넣어요."));

        // when
        String result = cookingTipGenerateGeminiService.generate(List.of(
                CookingStep.builder().level(2L).title("볶기").content("재료를 볶아요.").build(),
                CookingStep.builder().level(1L).title("준비").content("팬을 달궈요.").build()
        ));

        // then
        assertThat(result).isEqualTo("팬이 충분히 달궈진 뒤 재료를 넣어요.");
    }

    @Test
    @DisplayName("빈 요리 팁 응답은 재시도 후 실패한다")
    void 빈_요리_팁_응답_재시도_후_실패() {
        // given
        given(geminiUtil.callFunction(
                anyString(),
                anyString(),
                any(CookingTipGenerateGeminiFunctionDeclarationDto.class),
                eq(CookingTipGenerateGeminiResponseDto.class)
        )).willReturn(CookingTipGenerateGeminiResponseDto.create(""));

        // when & then
        assertThatThrownBy(() -> cookingTipGenerateGeminiService.generate(List.of(
                CookingStep.builder().level(1L).title("준비").content("팬을 달궈요.").build()
        ))).isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(GeminiErrorCode.INVALID_RESPONSE));
        then(retryDelayStrategy).should().waitBeforeRetry(1);
    }
}
