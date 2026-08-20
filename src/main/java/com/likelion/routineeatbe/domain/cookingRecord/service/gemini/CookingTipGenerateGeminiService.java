package com.likelion.routineeatbe.domain.cookingRecord.service.gemini;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingTipGenerateGeminiFunctionDeclarationDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingTipGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.global.config.GeminiProperties;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import com.likelion.routineeatbe.global.util.GeminiRetryDelayStrategy;
import com.likelion.routineeatbe.global.util.GeminiUtil;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CookingTipGenerateGeminiService {

    private static final int COOKING_TIP_MAX_LENGTH = 500;

    private final GeminiUtil geminiUtil;
    private final GeminiRetryDelayStrategy retryDelayStrategy;
    private final GeminiProperties properties;

    /**
     * 완료된 실제 요리 단계를 바탕으로 한 줄 요리 팁을 생성합니다.
     *
     * @param cookingSteps 완료된 요리 세션의 실제 요리 단계 목록
     * @return Gemini가 생성하고 검증한 한 줄 요리 팁
     */
    public String generate(List<CookingStep> cookingSteps) {
        log.info(
                "[CookingTipGenerateGeminiService] 요리 팁 생성 시작 | generate() - START | cookingStepCount: {}",
                cookingSteps == null ? 0 : cookingSteps.size()
        );

        String prompt = createPrompt(cookingSteps);
        int maxAttempts = properties.retry().maxAttempts();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                CookingTipGenerateGeminiResponseDto response = geminiUtil.callFunction(
                        properties.cookingStepGenerateModel(),
                        prompt,
                        CookingTipGenerateGeminiFunctionDeclarationDto.create(),
                        CookingTipGenerateGeminiResponseDto.class
                );
                String result = validateCookingTip(response);
                log.info(
                        "[CookingTipGenerateGeminiService] 요리 팁 생성 종료 | generate() - END | cookingTipLength: {}",
                        result.length()
                );
                return result;
            } catch (CustomException exception) {
                if (!isRetryable(exception) || attempt == maxAttempts) {
                    throw exception;
                }
                log.warn(
                        "[CookingTipGenerateGeminiService] 요리 팁 생성 재시도 | generate() | attempt: {}, errorCode: {}",
                        attempt,
                        exception.getErrorCode().getCode()
                );
                retryDelayStrategy.waitBeforeRetry(attempt);
            }
        }
        throw new CustomException(GeminiErrorCode.INVALID_RESPONSE);
    }

    /**
     * Gemini에 전달할 요리 단계 기반 팁 생성 프롬프트를 만듭니다.
     *
     * @param cookingSteps 완료된 요리 세션의 실제 요리 단계 목록
     * @return Gemini 입력 프롬프트
     */
    private String createPrompt(List<CookingStep> cookingSteps) {
        log.debug("[CookingTipGenerateGeminiService] 프롬프트 생성 시작 | createPrompt() - START");
        if (cookingSteps == null || cookingSteps.isEmpty()) {
            throw new CustomException(GeminiErrorCode.INVALID_RESPONSE);
        }

        StringBuilder prompt = new StringBuilder("""
                아래 실제 요리 단계를 보고, 다음 요리를 할 때 도움이 되는 한 줄 팁을 생성하세요.
                제공된 단계에 있는 정보만 사용하고 수치, 재료, 도구 또는 조리법을 추측하지 마세요.
                쉽고 자연스러운 해요체 한 문장으로 작성하고, 500자를 넘기지 마세요.
                팁만 작성하세요.

                [실제 요리 단계]
                """);
        cookingSteps.stream()
                .filter(cookingStep -> cookingStep.getLevel() != null && cookingStep.getLevel() >= 1)
                .sorted(Comparator.comparing(CookingStep::getLevel))
                .forEach(cookingStep -> prompt.append("- level=")
                        .append(cookingStep.getLevel())
                        .append(", title=")
                        .append(cookingStep.getTitle())
                        .append(", content=")
                        .append(cookingStep.getContent())
                        .append(", subContent=")
                        .append(cookingStep.getSubContent() == null ? "없음" : cookingStep.getSubContent())
                        .append(System.lineSeparator()));
        String result = prompt.toString();
        log.debug(
                "[CookingTipGenerateGeminiService] 프롬프트 생성 종료 | createPrompt() - END | promptLength: {}",
                result.length()
        );
        return result;
    }

    /**
     * Gemini 응답의 요리 팁을 검증합니다.
     *
     * @param response Gemini Function Calling 응답
     * @return 검증된 한 줄 요리 팁
     */
    private String validateCookingTip(CookingTipGenerateGeminiResponseDto response) {
        log.debug("[CookingTipGenerateGeminiService] 요리 팁 검증 시작 | validateCookingTip() - START");
        if (response == null
                || response.cookingTip() == null
                || response.cookingTip().isBlank()
                || response.cookingTip().length() > COOKING_TIP_MAX_LENGTH) {
            throw new CustomException(GeminiErrorCode.INVALID_RESPONSE);
        }
        String result = response.cookingTip().trim();
        log.debug("[CookingTipGenerateGeminiService] 요리 팁 검증 종료 | validateCookingTip() - END");
        return result;
    }

    /**
     * Gemini 예외가 재시도 가능한 오류인지 확인합니다.
     *
     * @param exception Gemini 호출 또는 응답 검증 예외
     * @return 재시도 가능 여부
     */
    private boolean isRetryable(CustomException exception) {
        log.debug("[CookingTipGenerateGeminiService] 재시도 여부 확인 시작 | isRetryable() - START");
        boolean result = exception.getErrorCode() == GeminiErrorCode.RATE_LIMIT_EXCEEDED
                || exception.getErrorCode() == GeminiErrorCode.API_TIMEOUT
                || exception.getErrorCode() == GeminiErrorCode.INVALID_RESPONSE;
        log.debug(
                "[CookingTipGenerateGeminiService] 재시도 여부 확인 종료 | isRetryable() - END | result: {}",
                result
        );
        return result;
    }
}
