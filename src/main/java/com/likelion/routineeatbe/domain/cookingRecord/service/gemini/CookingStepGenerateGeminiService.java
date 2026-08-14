package com.likelion.routineeatbe.domain.cookingRecord.service.gemini;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiFunctionDeclarationDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto.GeneratedCookingStep;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingStepStage;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.entity.RecipeStep;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.SkillLevel;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.global.config.GeminiProperties;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import com.likelion.routineeatbe.global.util.GeminiRetryDelayStrategy;
import com.likelion.routineeatbe.global.util.GeminiUtil;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CookingStepGenerateGeminiService {

    private static final int TITLE_MAX_LENGTH = 300;
    private static final int CONTENT_MAX_LENGTH = 500;

    private final GeminiUtil geminiUtil;
    private final GeminiRetryDelayStrategy retryDelayStrategy;
    private final GeminiProperties properties;

    /**
     * (1) 작업 목적
     * 사용자 숙련도와 레시피 정보를 기반으로 요리 시작 전 체크리스트와 요리 단계를 생성합니다.
     *
     * (2) 세부 작업 내용
     * - 메뉴, 인분별 재료량, 기존 레시피 단계를 Gemini 프롬프트에 포함합니다.
     * - Gemini Function Calling 응답을 검증하고 일시적 오류 또는 잘못된 응답을 재시도합니다.
     *
     * @param user 요리를 시작하는 사용자
     * @param recipe 요리할 레시피
     * @param ingredients 레시피 필요 재료 목록
     * @param recipeSteps 기존 레시피 단계 목록
     * @param servings 요청 인분 수
     * @return 검증된 체크리스트와 요리 단계
     */
    public CookingStepGenerateGeminiResponseDto generate(
            User user,
            Recipe recipe,
            List<RecipeFoodIngredient> ingredients,
            List<RecipeStep> recipeSteps,
            int servings
    ) {
        log.info(
                "[CookingStepGenerateGeminiService] 요리 단계 생성 시작 | generate() - START | userId: {}, recipeId: {}, servings: {}",
                user.getId(),
                recipe.getId(),
                servings
        );

        String prompt = createPrompt(user, recipe, ingredients, recipeSteps, servings);
        int maxAttempts = properties.retry().maxAttempts();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                CookingStepGenerateGeminiResponseDto response = geminiUtil.callFunction(
                        properties.cookingStepGenerateModel(),
                        prompt,
                        CookingStepGenerateGeminiFunctionDeclarationDto.create(),
                        CookingStepGenerateGeminiResponseDto.class
                );
                CookingStepGenerateGeminiResponseDto result = validateAndNormalize(
                        response,
                        user.getSkillLevel() == SkillLevel.BEGINNER
                );
                log.info(
                        "[CookingStepGenerateGeminiService] 요리 단계 생성 종료 | generate() - END | cookingStepCount: {}",
                        result.cookingSteps().size()
                );
                return result;
            } catch (CustomException exception) {
                if (!isRetryable(exception) || attempt == maxAttempts) {
                    throw exception;
                }
                log.warn(
                        "[CookingStepGenerateGeminiService] 요리 단계 생성 재시도 | generate() | attempt: {}, errorCode: {}",
                        attempt,
                        exception.getErrorCode().getCode()
                );
                retryDelayStrategy.waitBeforeRetry(attempt);
            }
        }
        throw new CustomException(GeminiErrorCode.INVALID_COOKING_STEP_METADATA);
    }

    /**
     * Gemini에 전달할 사용자 맞춤 요리 단계 생성 프롬프트를 생성합니다.
     *
     * @param user 요리를 시작하는 사용자
     * @param recipe 요리할 레시피
     * @param ingredients 레시피 필요 재료 목록
     * @param recipeSteps 기존 레시피 단계 목록
     * @param servings 요청 인분 수
     * @return Gemini 입력 프롬프트
     */
    private String createPrompt(
            User user,
            Recipe recipe,
            List<RecipeFoodIngredient> ingredients,
            List<RecipeStep> recipeSteps,
            int servings
    ) {
        log.debug(
                "[CookingStepGenerateGeminiService] 프롬프트 생성 시작 | createPrompt() - START | recipeId: {}",
                recipe.getId()
        );

        boolean beginner = user.getSkillLevel() == SkillLevel.BEGINNER;
        StringBuilder prompt = new StringBuilder("""
                아래 사용자와 레시피 정보를 사용하여 실제 조리에 필요한 데이터를 생성하세요.
                checkListBeforeStart에는 요리 시작 전에 확인할 안전 및 준비 항목을 작성하세요.
                cookingSteps는 PREPARATION, COOKING, FINISH 순서로 구성하고 각 구분을 하나 이상 포함하세요.
                cookingSteps의 level은 1부터 시작하여 빈 번호 없이 1씩 증가해야 합니다.
                title은 300자 이하, content와 subContent는 각각 500자 이하로 작성하세요.
                사용자 숙련도가 BEGINNER이면 모든 cookingSteps의 subContent에 구체적인 부연 설명을 작성하세요.
                체크리스트는 cookingSteps에 중복해서 포함하지 마세요.

                [사용자]
                skillLevel: %s
                beginner: %s

                [메뉴 및 레시피]
                recipeType: %s
                menuName: %s
                menuType: %s
                difficultyLevel: %s
                timeRequiredMinutes: %s
                servings: %d

                [인분 수를 반영한 재료]
                """.formatted(
                user.getSkillLevel(),
                beginner,
                recipe.getType(),
                recipe.getMenu().getName(),
                recipe.getMenu().getType(),
                recipe.getMenu().getDifficultyLevel(),
                recipe.getMenu().getTimeRequired(),
                servings
        ));
        ingredients.forEach(ingredient -> prompt.append("- name=")
                .append(ingredient.getFoodIngredient().getName())
                .append(", primaryAmount=")
                .append(ingredient.getPrimaryNeedAmountValue() * servings)
                .append(ingredient.getFoodIngredient().getPrimaryUnit().getDescription())
                .append(", secondaryAmount=")
                .append(ingredient.getSecondaryNeedAmountValue() == null
                        ? "없음"
                        : ingredient.getSecondaryNeedAmountValue() * servings
                                + ingredient.getFoodIngredient().getSecondaryUnit().getDescription())
                .append(System.lineSeparator()));

        prompt.append("\n[기존 레시피 단계]\n");
        recipeSteps.stream()
                .sorted(Comparator.comparing(RecipeStep::getLevel))
                .forEach(recipeStep -> prompt.append("- level=")
                        .append(recipeStep.getLevel())
                        .append(", type=")
                        .append(recipeStep.getType())
                        .append(", contents=")
                        .append(recipeStep.getContents())
                        .append(System.lineSeparator()));

        String result = prompt.toString();
        log.debug(
                "[CookingStepGenerateGeminiService] 프롬프트 생성 종료 | createPrompt() - END | promptLength: {}",
                result.length()
        );
        return result;
    }

    /**
     * Gemini 응답의 필수값, 길이, 단계 순서와 단계 구분을 검증합니다.
     *
     * @param response Gemini 요리 단계 응답
     * @param beginner 초보 사용자 여부
     * @return level 오름차순으로 정규화된 응답
     */
    private CookingStepGenerateGeminiResponseDto validateAndNormalize(
            CookingStepGenerateGeminiResponseDto response,
            boolean beginner
    ) {
        log.debug(
                "[CookingStepGenerateGeminiService] Gemini 응답 검증 시작 | validateAndNormalize() - START | beginner: {}",
                beginner
        );

        if (response == null
                || response.checkListBeforeStart() == null
                || response.checkListBeforeStart().isEmpty()
                || response.cookingSteps() == null
                || response.cookingSteps().isEmpty()) {
            throw new CustomException(GeminiErrorCode.INVALID_COOKING_STEP_METADATA);
        }
        List<String> checkList = response.checkListBeforeStart().stream()
                .map(this::validateCheckListContent)
                .toList();
        List<GeneratedCookingStep> cookingSteps = response.cookingSteps().stream()
                .sorted(Comparator.comparing(
                        GeneratedCookingStep::level,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .toList();

        Set<Integer> levels = new HashSet<>();
        EnumSet<CookingStepStage> stages = EnumSet.noneOf(CookingStepStage.class);
        for (int index = 0; index < cookingSteps.size(); index++) {
            GeneratedCookingStep cookingStep = cookingSteps.get(index);
            if (cookingStep == null
                    || cookingStep.level() == null
                    || cookingStep.level() != index + 1
                    || !levels.add(cookingStep.level())
                    || cookingStep.stage() == null
                    || isBlankOrTooLong(cookingStep.title(), TITLE_MAX_LENGTH)
                    || isBlankOrTooLong(cookingStep.content(), CONTENT_MAX_LENGTH)
                    || (beginner && isBlankOrTooLong(cookingStep.subContent(), CONTENT_MAX_LENGTH))
                    || (!beginner && cookingStep.subContent() != null
                            && cookingStep.subContent().length() > CONTENT_MAX_LENGTH)) {
                throw new CustomException(GeminiErrorCode.INVALID_COOKING_STEP_METADATA);
            }
            stages.add(cookingStep.stage());
        }
        if (!stages.containsAll(EnumSet.allOf(CookingStepStage.class))) {
            throw new CustomException(GeminiErrorCode.INVALID_COOKING_STEP_METADATA);
        }

        CookingStepGenerateGeminiResponseDto result =
                CookingStepGenerateGeminiResponseDto.create(checkList, cookingSteps);
        log.debug(
                "[CookingStepGenerateGeminiService] Gemini 응답 검증 종료 | validateAndNormalize() - END | cookingStepCount: {}",
                cookingSteps.size()
        );
        return result;
    }

    /**
     * 체크리스트 내용을 검증합니다.
     *
     * @param content 체크리스트 내용
     * @return 검증된 체크리스트 내용
     */
    private String validateCheckListContent(String content) {
        log.debug(
                "[CookingStepGenerateGeminiService] 체크리스트 검증 시작 | validateCheckListContent() - START"
        );
        if (isBlankOrTooLong(content, CONTENT_MAX_LENGTH)) {
            throw new CustomException(GeminiErrorCode.INVALID_COOKING_STEP_METADATA);
        }
        log.debug(
                "[CookingStepGenerateGeminiService] 체크리스트 검증 종료 | validateCheckListContent() - END"
        );
        return content;
    }

    /**
     * 문자열이 비어 있거나 허용 길이를 초과하는지 확인합니다.
     *
     * @param value 확인할 문자열
     * @param maxLength 허용 최대 길이
     * @return 잘못된 문자열 여부
     */
    private boolean isBlankOrTooLong(String value, int maxLength) {
        log.debug(
                "[CookingStepGenerateGeminiService] 문자열 검증 시작 | isBlankOrTooLong() - START | maxLength: {}",
                maxLength
        );
        boolean result = value == null || value.isBlank() || value.length() > maxLength;
        log.debug(
                "[CookingStepGenerateGeminiService] 문자열 검증 종료 | isBlankOrTooLong() - END | result: {}",
                result
        );
        return result;
    }

    /**
     * Gemini 예외가 재시도 가능한 오류인지 확인합니다.
     *
     * @param exception Gemini 호출 또는 응답 검증 예외
     * @return 재시도 가능 여부
     */
    private boolean isRetryable(CustomException exception) {
        log.debug(
                "[CookingStepGenerateGeminiService] 재시도 여부 확인 시작 | isRetryable() - START | errorCode: {}",
                exception.getErrorCode().getCode()
        );
        boolean result = exception.getErrorCode() == GeminiErrorCode.RATE_LIMIT_EXCEEDED
                || exception.getErrorCode() == GeminiErrorCode.API_TIMEOUT
                || exception.getErrorCode() == GeminiErrorCode.INVALID_COOKING_STEP_METADATA;
        log.debug(
                "[CookingStepGenerateGeminiService] 재시도 여부 확인 종료 | isRetryable() - END | result: {}",
                result
        );
        return result;
    }
}
