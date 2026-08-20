package com.likelion.routineeatbe.domain.menu.service.gemini;

import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeCookingEquipmentGeminiFunctionDeclarationDto;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeCookingEquipmentGeminiResponseDto;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeCookingEquipmentGeminiResponseDto.RecipeCookingEquipments;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.entity.RecipeStep;
import com.likelion.routineeatbe.global.config.GeminiProperties;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import com.likelion.routineeatbe.global.util.GeminiRetryDelayStrategy;
import com.likelion.routineeatbe.global.util.GeminiUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitMenuAndRecipeCookingEquipmentGeminiService {

    private final GeminiUtil geminiUtil;
    private final GeminiRetryDelayStrategy retryDelayStrategy;
    private final GeminiProperties properties;

    /**
     * (1) 작업 목적
     * 레시피 조리 단계와 조리 도구 기준 데이터를 Gemini로 분석하여 필요한 조리 도구를 생성합니다.
     *
     * (2) 세부 작업 내용
     * - 레시피를 설정된 배치 크기로 분할합니다.
     * - 메뉴 정보, 순서별 조리 단계, 전체 조리 도구를 Gemini에 전달합니다.
     * - 응답 순번과 조리 도구 식별자를 검증하여 레시피 ID 기준으로 반환합니다.
     *
     * @param recipes 조리 도구를 생성할 레시피 목록
     * @param cookingEquipments Gemini에 제공할 조리 도구 기준 데이터
     * @return 레시피 ID별 필요한 조리 도구 식별자 목록
     */
    public Map<Long, List<Long>> generateCookingEquipmentIds(
            List<Recipe> recipes,
            List<CookingEquipment> cookingEquipments
    ) {
        log.info(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] 레시피 조리 도구 생성 시작 | generateCookingEquipmentIds() - START | recipeCount: {}, equipmentCount: {}",
                recipes.size(),
                cookingEquipments.size()
        );

        if (recipes.isEmpty()) {
            log.info(
                    "[InitMenuAndRecipeCookingEquipmentGeminiService] 레시피 조리 도구 생성 종료 | generateCookingEquipmentIds() - END | resultSize: 0"
            );
            return Map.of();
        }

        Set<Long> availableCookingEquipmentIds = extractCookingEquipmentIds(cookingEquipments);
        if (availableCookingEquipmentIds.isEmpty()) {
            throw new CustomException(GeminiErrorCode.INVALID_METADATA);
        }

        Map<Long, List<Long>> result = new LinkedHashMap<>();
        List<List<Recipe>> batches = partition(recipes);
        for (int index = 0; index < batches.size(); index++) {
            List<Recipe> batch = batches.get(index);
            Map<Integer, List<Long>> responseBySequence = callBatchWithRetry(
                    batch,
                    cookingEquipments,
                    availableCookingEquipmentIds,
                    index + 1
            );
            mergeBatchResult(batch, responseBySequence, result);
        }

        log.info(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] 레시피 조리 도구 생성 종료 | generateCookingEquipmentIds() - END | resultSize: {}, batchCount: {}",
                result.size(),
                batches.size()
        );
        return result;
    }

    /**
     * 레시피 목록을 Gemini 호출 배치로 분할합니다.
     *
     * @param recipes 분할할 레시피 목록
     * @return 설정된 배치 크기로 분할한 레시피 목록
     */
    private List<List<Recipe>> partition(List<Recipe> recipes) {
        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] 레시피 배치 분할 시작 | partition() - START | recipeCount: {}, batchSize: {}",
                recipes.size(),
                properties.batchSize()
        );

        List<List<Recipe>> result = new ArrayList<>();
        for (int start = 0; start < recipes.size(); start += properties.batchSize()) {
            int end = Math.min(start + properties.batchSize(), recipes.size());
            result.add(List.copyOf(recipes.subList(start, end)));
        }

        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] 레시피 배치 분할 종료 | partition() - END | resultSize: {}",
                result.size()
        );
        return result;
    }

    /**
     * 단일 레시피 배치를 Gemini로 분석하고 일시적 오류 또는 잘못된 응답을 재시도합니다.
     *
     * @param batch Gemini에 전달할 레시피 배치
     * @param cookingEquipments Gemini에 제공할 조리 도구 목록
     * @param availableCookingEquipmentIds 사용 가능한 조리 도구 식별자 집합
     * @param batchNumber 로그에 사용할 배치 번호
     * @return 레시피 순번별 검증된 조리 도구 식별자 목록
     */
    private Map<Integer, List<Long>> callBatchWithRetry(
            List<Recipe> batch,
            List<CookingEquipment> cookingEquipments,
            Set<Long> availableCookingEquipmentIds,
            int batchNumber
    ) {
        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] Gemini 배치 호출 시작 | callBatchWithRetry() - START | batchNumber: {}, batchSize: {}",
                batchNumber,
                batch.size()
        );

        int maxAttempts = properties.retry().maxAttempts();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                InitMenuAndRecipeCookingEquipmentGeminiResponseDto response =
                        geminiUtil.callFunction(
                                properties.cookingEquipmentAnalyzeModel(),
                                createBatchPrompt(batch, cookingEquipments),
                                InitMenuAndRecipeCookingEquipmentGeminiFunctionDeclarationDto.create(
                                        batch.size()
                                ),
                                InitMenuAndRecipeCookingEquipmentGeminiResponseDto.class
                        );
                Map<Integer, List<Long>> result = validateBatchResponse(
                        batch,
                        response,
                        availableCookingEquipmentIds
                );

                log.debug(
                        "[InitMenuAndRecipeCookingEquipmentGeminiService] Gemini 배치 호출 종료 | callBatchWithRetry() - END | batchNumber: {}, attempt: {}, resultSize: {}",
                        batchNumber,
                        attempt,
                        result.size()
                );
                return result;
            } catch (CustomException exception) {
                if (!isRetryable(exception) || attempt == maxAttempts) {
                    log.warn(
                            "[InitMenuAndRecipeCookingEquipmentGeminiService] Gemini 배치 호출 실패 | callBatchWithRetry() | batchNumber: {}, attempt: {}, errorCode: {}",
                            batchNumber,
                            attempt,
                            exception.getErrorCode().getCode()
                    );
                    throw exception;
                }
                log.warn(
                        "[InitMenuAndRecipeCookingEquipmentGeminiService] Gemini 배치 재시도 | callBatchWithRetry() | batchNumber: {}, attempt: {}, nextAttempt: {}, errorCode: {}",
                        batchNumber,
                        attempt,
                        attempt + 1,
                        exception.getErrorCode().getCode()
                );
                retryDelayStrategy.waitBeforeRetry(attempt);
            }
        }
        throw new CustomException(GeminiErrorCode.INVALID_METADATA);
    }

    /**
     * Gemini 호출 예외가 배치 재시도 대상인지 확인합니다.
     *
     * @param exception Gemini 호출 또는 응답 검증 예외
     * @return 호출 한도 초과, 시간 초과, 잘못된 메타데이터 여부
     */
    private boolean isRetryable(CustomException exception) {
        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] 재시도 대상 확인 시작 | isRetryable() - START | errorCode: {}",
                exception.getErrorCode().getCode()
        );
        boolean result = exception.getErrorCode() == GeminiErrorCode.RATE_LIMIT_EXCEEDED
                || exception.getErrorCode() == GeminiErrorCode.API_TIMEOUT
                || exception.getErrorCode() == GeminiErrorCode.INVALID_METADATA;
        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] 재시도 대상 확인 종료 | isRetryable() - END | result: {}",
                result
        );
        return result;
    }

    /**
     * 레시피 배치와 조리 도구 기준 데이터를 Gemini 프롬프트로 생성합니다.
     *
     * @param batch 프롬프트에 포함할 레시피 배치
     * @param cookingEquipments 프롬프트에 포함할 조리 도구 목록
     * @return Gemini 입력 프롬프트
     */
    private String createBatchPrompt(
            List<Recipe> batch,
            List<CookingEquipment> cookingEquipments
    ) {
        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] Gemini 프롬프트 생성 시작 | createBatchPrompt() - START | batchSize: {}, equipmentCount: {}",
                batch.size(),
                cookingEquipments.size()
        );

        StringBuilder prompt = new StringBuilder("""
                다음 레시피 정보와 사용 가능한 조리 도구 목록을 기준으로 레시피별 필요한 조리 도구를 생성하세요.
                반드시 제공된 cookingEquipmentId만 사용하고 실제 조리 단계에 필요한 도구만 선택하세요.
                모든 레시피의 cookingEquipmentIds에는 조리 도구를 최소 1개 이상 포함하세요.
                같은 레시피에서 동일한 cookingEquipmentId를 중복 반환하지 마세요.
                각 레시피 결과의 sequence는 입력 레시피 앞에 표시된 순번을 그대로 사용하세요.

                [사용 가능한 조리 도구]
                """);
        cookingEquipments.forEach(cookingEquipment -> prompt.append("cookingEquipmentId=")
                .append(cookingEquipment.getId())
                .append(", name=")
                .append(cookingEquipment.getName())
                .append(", type=")
                .append(cookingEquipment.getType())
                .append(System.lineSeparator()));

        prompt.append("\n[레시피 목록]\n");
        IntStream.range(0, batch.size())
                .forEach(index -> prompt.append(createRecipePrompt(batch.get(index), index + 1)));

        String result = prompt.toString();
        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] Gemini 프롬프트 생성 종료 | createBatchPrompt() - END | promptLength: {}",
                result.length()
        );
        return result;
    }

    /**
     * 단일 레시피의 메뉴 정보와 순서별 조리 단계를 프롬프트 구간으로 생성합니다.
     *
     * @param recipe 프롬프트에 포함할 레시피
     * @param sequence 배치 내 레시피 순번
     * @return 단일 레시피 프롬프트 구간
     */
    private String createRecipePrompt(Recipe recipe, int sequence) {
        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] 단일 레시피 프롬프트 생성 시작 | createRecipePrompt() - START | sequence: {}, recipeId: {}",
                sequence,
                recipe.getId()
        );

        StringBuilder prompt = new StringBuilder("""
                --- 레시피 %d ---
                recipeType: %s
                menuName: %s
                menuType: %s
                ingredientInfoOriginal: %s
                recipeSteps:
                """.formatted(
                sequence,
                recipe.getType(),
                recipe.getMenu().getName(),
                recipe.getMenu().getType(),
                recipe.getMenu().getIngredient_info_original()
        ));
        recipe.getRecipeSteps().stream()
                .sorted(Comparator.comparing(
                        RecipeStep::getLevel,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .forEach(recipeStep -> prompt.append("- level=")
                        .append(recipeStep.getLevel())
                        .append(", contents=")
                        .append(recipeStep.getContents())
                        .append(System.lineSeparator()));
        prompt.append(System.lineSeparator());

        String result = prompt.toString();
        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] 단일 레시피 프롬프트 생성 종료 | createRecipePrompt() - END | sequence: {}, recipeId: {}",
                sequence,
                recipe.getId()
        );
        return result;
    }

    /**
     * Gemini 배치 응답의 개수, 순번, 조리 도구 식별자를 검증합니다.
     *
     * @param batch 원본 레시피 배치
     * @param response Gemini 응답
     * @param availableCookingEquipmentIds 사용 가능한 조리 도구 식별자 집합
     * @return 레시피 순번별 중복 제거된 조리 도구 식별자 목록
     */
    private Map<Integer, List<Long>> validateBatchResponse(
            List<Recipe> batch,
            InitMenuAndRecipeCookingEquipmentGeminiResponseDto response,
            Set<Long> availableCookingEquipmentIds
    ) {
        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] Gemini 응답 검증 시작 | validateBatchResponse() - START | batchSize: {}",
                batch.size()
        );

        if (Objects.isNull(response)
                || Objects.isNull(response.recipes())
                || response.recipes().size() != batch.size()) {
            throw new CustomException(GeminiErrorCode.INVALID_METADATA);
        }

        Map<Integer, List<Long>> result = new LinkedHashMap<>();
        for (RecipeCookingEquipments recipeCookingEquipments : response.recipes()) {
            List<Long> cookingEquipmentIds = validateAndNormalizeRecipeCookingEquipments(
                    recipeCookingEquipments,
                    batch.size(),
                    availableCookingEquipmentIds
            );
            if (result.putIfAbsent(
                    recipeCookingEquipments.sequence(),
                    cookingEquipmentIds
            ) != null) {
                throw new CustomException(GeminiErrorCode.INVALID_METADATA);
            }
        }

        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] Gemini 응답 검증 종료 | validateBatchResponse() - END | resultSize: {}",
                result.size()
        );
        return result;
    }

    /**
     * 단일 레시피 응답을 검증하고 중복 조리 도구 식별자를 제거합니다.
     *
     * @param recipeCookingEquipments 검증할 단일 레시피 응답
     * @param batchSize 허용되는 레시피 순번 최댓값
     * @param availableCookingEquipmentIds 사용 가능한 조리 도구 식별자 집합
     * @return 입력 순서를 유지하며 중복을 제거한 조리 도구 식별자 목록
     */
    private List<Long> validateAndNormalizeRecipeCookingEquipments(
            RecipeCookingEquipments recipeCookingEquipments,
            int batchSize,
            Set<Long> availableCookingEquipmentIds
    ) {
        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] 레시피 조리 도구 검증 시작 | validateAndNormalizeRecipeCookingEquipments() - START"
        );

        if (Objects.isNull(recipeCookingEquipments)
                || Objects.isNull(recipeCookingEquipments.sequence())
                || recipeCookingEquipments.sequence() < 1
                || recipeCookingEquipments.sequence() > batchSize
                || Objects.isNull(recipeCookingEquipments.cookingEquipmentIds())
                || recipeCookingEquipments.cookingEquipmentIds().isEmpty()) {
            throw new CustomException(GeminiErrorCode.INVALID_METADATA);
        }

        LinkedHashSet<Long> normalizedIds = new LinkedHashSet<>();
        for (Long cookingEquipmentId : recipeCookingEquipments.cookingEquipmentIds()) {
            if (Objects.isNull(cookingEquipmentId)
                    || !availableCookingEquipmentIds.contains(cookingEquipmentId)) {
                throw new CustomException(GeminiErrorCode.INVALID_METADATA);
            }
            normalizedIds.add(cookingEquipmentId);
        }

        List<Long> result = List.copyOf(normalizedIds);
        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] 레시피 조리 도구 검증 종료 | validateAndNormalizeRecipeCookingEquipments() - END | sequence: {}, resultSize: {}",
                recipeCookingEquipments.sequence(),
                result.size()
        );
        return result;
    }

    /**
     * 검증된 배치 응답을 레시피 ID 기준 전체 결과에 병합합니다.
     *
     * @param batch 원본 레시피 배치
     * @param responseBySequence 레시피 순번별 Gemini 응답
     * @param result 전체 생성 결과
     */
    private void mergeBatchResult(
            List<Recipe> batch,
            Map<Integer, List<Long>> responseBySequence,
            Map<Long, List<Long>> result
    ) {
        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] 배치 응답 병합 시작 | mergeBatchResult() - START | batchSize: {}",
                batch.size()
        );

        for (int index = 0; index < batch.size(); index++) {
            result.put(batch.get(index).getId(), responseBySequence.get(index + 1));
        }

        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] 배치 응답 병합 종료 | mergeBatchResult() - END | resultSize: {}",
                result.size()
        );
    }

    /**
     * 조리 도구 기준 데이터에서 유효한 식별자를 추출합니다.
     *
     * @param cookingEquipments 조리 도구 기준 데이터
     * @return null을 제외한 조리 도구 식별자 집합
     */
    private Set<Long> extractCookingEquipmentIds(List<CookingEquipment> cookingEquipments) {
        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] 조리 도구 식별자 추출 시작 | extractCookingEquipmentIds() - START | equipmentCount: {}",
                cookingEquipments.size()
        );

        Set<Long> result = cookingEquipments.stream()
                .filter(Objects::nonNull)
                .map(CookingEquipment::getId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        log.debug(
                "[InitMenuAndRecipeCookingEquipmentGeminiService] 조리 도구 식별자 추출 종료 | extractCookingEquipmentIds() - END | resultSize: {}",
                result.size()
        );
        return result;
    }
}
