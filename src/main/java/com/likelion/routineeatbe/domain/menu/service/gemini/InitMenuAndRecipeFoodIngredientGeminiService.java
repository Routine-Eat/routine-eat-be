package com.likelion.routineeatbe.domain.menu.service.gemini;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeFoodIngredientGeminiResponseDto;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeFoodIngredientGeminiResponseDto.FoodIngredientNeedAmount;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeFoodIngredientGeminiResponseDto.MenuFoodIngredients;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.global.config.GeminiProperties;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import com.likelion.routineeatbe.global.util.GeminiRetryDelayStrategy;
import com.likelion.routineeatbe.global.util.GeminiUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Gemini를 이용하여 메뉴별 음식 재료 필요량을 1인분 기준으로 생성하는 Service입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InitMenuAndRecipeFoodIngredientGeminiService {

    private final GeminiUtil geminiUtil;
    private final GeminiRetryDelayStrategy retryDelayStrategy;
    private final GeminiProperties properties;

    /**
     * (1) 작업 목적
     * 메뉴 정보와 타입별 음식 재료 목록을 Gemini에 전달하여 메뉴별 1인분 필요량을 생성합니다.
     *
     * (2) 세부 작업 내용
     * - 메뉴를 설정된 배치 크기로 분할합니다.
     * - 모든 배치에 동일한 음식 재료 카탈로그와 단위 정보를 제공합니다.
     * - Gemini 응답의 순번, 식재료 식별자, 사용량을 검증한 후 메뉴 ID 기준으로 반환합니다.
     *
     * @param menus 음식 재료 필요량을 생성할 메뉴 목록
     * @param foodIngredientsByType 음식 재료 타입별 음식 재료 목록
     * @return 메뉴 ID별 1인분 음식 재료 필요량 목록
     */
    public Map<Long, List<FoodIngredientNeedAmount>> generateNeedAmounts(
            List<Menu> menus,
            Map<FoodIngredientType, List<FoodIngredient>> foodIngredientsByType
    ) {
        log.info(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 메뉴별 음식 재료 필요량 생성 시작 | generateNeedAmounts() - START | menuCount: {}",
                menus.size()
        );

        if (menus.isEmpty()) {
            log.debug(
                    "[InitMenuAndRecipeFoodIngredientGeminiService] 초기화 대상 메뉴 없음 | generateNeedAmounts() | menuCount: 0"
            );
            log.info(
                    "[InitMenuAndRecipeFoodIngredientGeminiService] 메뉴별 음식 재료 필요량 생성 종료 | generateNeedAmounts() - END | resultSize: 0"
            );
            return Map.of();
        }

        Set<Long> availableFoodIngredientIds = extractFoodIngredientIds(foodIngredientsByType);
        if (availableFoodIngredientIds.isEmpty()) {
            log.warn(
                    "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 입력 음식 재료 없음 | generateNeedAmounts() | typeCount: {}",
                    Objects.isNull(foodIngredientsByType) ? null : foodIngredientsByType.size()
            );
            throw new CustomException(GeminiErrorCode.INVALID_METADATA);
        }
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 입력 음식 재료 확인 | generateNeedAmounts() | typeCount: {}, ingredientCount: {}",
                foodIngredientsByType.size(),
                availableFoodIngredientIds.size()
        );

        Map<Long, List<FoodIngredientNeedAmount>> result = new LinkedHashMap<>();
        List<List<Menu>> batches = partition(menus);

        for (int index = 0; index < batches.size(); index++) {
            List<Menu> batch = batches.get(index);
            log.debug(
                    "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 메뉴 배치 처리 시작 | generateNeedAmounts() | batchNumber: {}, batchSize: {}, firstMenuId: {}, lastMenuId: {}",
                    index + 1,
                    batch.size(),
                    batch.getFirst().getId(),
                    batch.getLast().getId()
            );
            Map<Integer, MenuFoodIngredients> responseBySequence = callBatchWithRetry(
                    batch,
                    foodIngredientsByType,
                    availableFoodIngredientIds,
                    index + 1
            );
            mergeBatchResult(batch, responseBySequence, result);
            log.debug(
                    "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 메뉴 배치 처리 종료 | generateNeedAmounts() | batchNumber: {}, accumulatedMenuCount: {}, accumulatedIngredientCount: {}",
                    index + 1,
                    result.size(),
                    result.values().stream().mapToLong(List::size).sum()
            );
        }

        long totalIngredientCount = result.values().stream().mapToLong(List::size).sum();
        log.info(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 메뉴별 음식 재료 필요량 생성 종료 | generateNeedAmounts() - END | resultSize: {}, batchCount: {}, ingredientCount: {}",
                result.size(),
                batches.size(),
                totalIngredientCount
        );
        return result;
    }

    /**
     * 메뉴 목록을 Gemini 설정의 배치 크기로 분할합니다.
     *
     * @param menus 분할할 메뉴 목록
     * @return 배치 단위 메뉴 목록
     */
    private List<List<Menu>> partition(List<Menu> menus) {
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 메뉴 배치 분할 시작 | partition() - START | menuCount: {}, batchSize: {}",
                menus.size(),
                properties.batchSize()
        );

        List<List<Menu>> result = new ArrayList<>();
        for (int start = 0; start < menus.size(); start += properties.batchSize()) {
            int end = Math.min(start + properties.batchSize(), menus.size());
            List<Menu> batch = List.copyOf(menus.subList(start, end));
            result.add(batch);
            log.debug(
                    "[InitMenuAndRecipeFoodIngredientGeminiService] 메뉴 배치 생성 | partition() | batchNumber: {}, startIndex: {}, endIndexExclusive: {}, batchSize: {}",
                    result.size(),
                    start,
                    end,
                    batch.size()
            );
        }

        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 메뉴 배치 분할 종료 | partition() - END | batchCount: {}",
                result.size()
        );
        return result;
    }

    /**
     * 단일 메뉴 배치를 Gemini로 호출하고 호출 한도 초과, 응답 시간 초과 또는 응답 검증 실패 시 재시도합니다.
     *
     * @param batch Gemini에 전달할 메뉴 배치
     * @param foodIngredientsByType 타입별 음식 재료 목록
     * @param availableFoodIngredientIds Gemini에 제공한 음식 재료 식별자 집합
     * @param batchNumber 로그에 사용할 배치 번호
     * @return 검증 및 정규화를 완료한 메뉴 순번별 음식 재료 필요량
     */
    private Map<Integer, MenuFoodIngredients> callBatchWithRetry(
            List<Menu> batch,
            Map<FoodIngredientType, List<FoodIngredient>> foodIngredientsByType,
            Set<Long> availableFoodIngredientIds,
            int batchNumber
    ) {
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 배치 호출 시작 | callBatchWithRetry() - START | batchNumber: {}, batchSize: {}",
                batchNumber,
                batch.size()
        );

        int maxAttempts = properties.retry().maxAttempts();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.debug(
                        "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 배치 호출 시도 | callBatchWithRetry() | batchNumber: {}, attempt: {}, maxAttempts: {}",
                        batchNumber,
                        attempt,
                        maxAttempts
                );
                InitMenuAndRecipeFoodIngredientGeminiResponseDto response = geminiUtil.callFunction(
                        properties.foodIngredientAnalyzeModel(),
                        createBatchPrompt(batch, foodIngredientsByType),
                        InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto.create(batch.size()),
                        InitMenuAndRecipeFoodIngredientGeminiResponseDto.class
                );
                Map<Integer, MenuFoodIngredients> result = validateBatchResponse(
                        batch,
                        response,
                        availableFoodIngredientIds
                );

                log.debug(
                        "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 배치 호출 및 검증 종료 | callBatchWithRetry() - END | batchNumber: {}, attempt: {}, resultSize: {}",
                        batchNumber,
                        attempt,
                        result.size()
                );
                return result;
            } catch (CustomException exception) {
                boolean rateLimited = exception.getErrorCode() == GeminiErrorCode.RATE_LIMIT_EXCEEDED;
                boolean apiTimedOut = exception.getErrorCode() == GeminiErrorCode.API_TIMEOUT;
                boolean invalidMetadata = exception.getErrorCode() == GeminiErrorCode.INVALID_METADATA;
                boolean retryable = rateLimited || apiTimedOut || invalidMetadata;
                if (!retryable || attempt == maxAttempts) {
                    log.warn(
                            "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 배치 호출 실패 | callBatchWithRetry() | batchNumber: {}, attempt: {}, maxAttempts: {}, errorCode: {}",
                            batchNumber,
                            attempt,
                            maxAttempts,
                            exception.getErrorCode().getCode()
                    );
                    throw exception;
                }
                log.warn(
                        "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 배치 재시도 | callBatchWithRetry() | batchNumber: {}, attempt: {}, nextAttempt: {}, errorCode: {}",
                        batchNumber,
                        attempt,
                        attempt + 1,
                        exception.getErrorCode().getCode()
                );
                retryDelayStrategy.waitBeforeRetry(attempt);
            }
        }
        throw new CustomException(GeminiErrorCode.RATE_LIMIT_EXCEEDED);
    }

    /**
     * 메뉴 배치와 타입별 음식 재료를 하나의 Gemini 프롬프트로 생성합니다.
     *
     * @param batch 프롬프트에 포함할 메뉴 배치
     * @param foodIngredientsByType 프롬프트에 포함할 타입별 음식 재료 목록
     * @return Gemini 입력 프롬프트
     */
    private String createBatchPrompt(
            List<Menu> batch,
            Map<FoodIngredientType, List<FoodIngredient>> foodIngredientsByType
    ) {
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 프롬프트 생성 시작 | createBatchPrompt() - START | batchSize: {}",
                batch.size()
        );

        StringBuilder prompt = new StringBuilder("""
                다음 메뉴 정보와 사용 가능한 음식 재료 목록을 기준으로 메뉴별 1인분 필요량을 생성하세요.
                반드시 제공된 foodIngredientId만 사용하고, 실제 조리에 필요한 재료만 선택하세요.
                모든 메뉴의 foodIngredients에는 음식 재료를 최소 1개 이상 포함하세요.
                같은 메뉴에서 동일한 foodIngredientId를 중복 반환하지 마세요.
                하나의 재료가 여러 조리 단계에 사용되면 사용량을 합산하여 하나의 항목으로 반환하세요.
                primaryNeedAmountValue는 primaryUnit 기준, secondaryNeedAmountValue는 secondaryUnit 기준입니다.
                보조 사용량을 신뢰성 있게 추정할 수 없으면 secondaryNeedAmountValue는 생략하세요.
                각 메뉴 결과의 sequence는 입력 메뉴 앞에 표시된 순번을 그대로 사용하세요.

                [사용 가능한 음식 재료]
                """);

        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 음식 재료 카탈로그 추가 | createBatchPrompt() | typeCount: {}, ingredientCount: {}",
                foodIngredientsByType.size(),
                foodIngredientsByType.values().stream().mapToLong(List::size).sum()
        );
        foodIngredientsByType.forEach((type, foodIngredients) -> {
            log.debug(
                    "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 음식 재료 타입 추가 | createBatchPrompt() | type: {}, ingredientCount: {}",
                    type,
                    foodIngredients.size()
            );
            prompt.append("\n- type: ").append(type.name()).append(System.lineSeparator());
            foodIngredients.forEach(foodIngredient -> prompt.append("  ")
                    .append("foodIngredientId=").append(foodIngredient.getId())
                    .append(", name=").append(foodIngredient.getName())
                    .append(", primaryUnit=").append(foodIngredient.getPrimaryUnit().name())
                    .append(", secondaryUnit=").append(foodIngredient.getSecondaryUnit().name())
                    .append(System.lineSeparator()));
        });

        prompt.append("\n[메뉴 목록]\n");
        IntStream.range(0, batch.size())
                .forEach(index -> prompt.append(createMenuPrompt(batch.get(index), index + 1)));

        String result = prompt.toString();
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 프롬프트 생성 종료 | createBatchPrompt() - END | promptLength: {}",
                result.length()
        );
        return result;
    }

    /**
     * 단일 메뉴의 이름, 타입, 원본 재료 정보를 프롬프트 구간으로 생성합니다.
     *
     * @param menu 프롬프트에 포함할 메뉴
     * @param sequence 배치 내부 메뉴 순번
     * @return 단일 메뉴 프롬프트 구간
     */
    private String createMenuPrompt(Menu menu, int sequence) {
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 단일 메뉴 프롬프트 생성 시작 | createMenuPrompt() - START | sequence: {}, menuId: {}, menuType: {}",
                sequence,
                menu.getId(),
                menu.getType()
        );

        String result = """
                --- 메뉴 %d ---
                name: %s
                type: %s
                ingredientInfoOriginal: %s

                """.formatted(
                sequence,
                menu.getName(),
                menu.getType().name(),
                menu.getIngredient_info_original()
        );

        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 단일 메뉴 프롬프트 생성 종료 | createMenuPrompt() - END | sequence: {}, menuId: {}",
                sequence,
                menu.getId()
        );
        return result;
    }

    /**
     * Gemini 배치 응답을 검증하고 메뉴 ID 기준 결과에 병합합니다.
     *
     * @param batch 원본 메뉴 배치
     * @param responseBySequence 검증 및 정규화를 완료한 메뉴 순번별 Gemini 응답
     * @param result 전체 생성 결과
     */
    private void mergeBatchResult(
            List<Menu> batch,
            Map<Integer, MenuFoodIngredients> responseBySequence,
            Map<Long, List<FoodIngredientNeedAmount>> result
    ) {
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 응답 병합 시작 | mergeBatchResult() - START | batchSize: {}",
                batch.size()
        );

        for (int index = 0; index < batch.size(); index++) {
            Menu menu = batch.get(index);
            MenuFoodIngredients menuFoodIngredients = responseBySequence.get(index + 1);
            result.put(
                    menu.getId(),
                    List.copyOf(menuFoodIngredients.foodIngredients())
            );
            log.debug(
                    "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 메뉴 응답 연결 | mergeBatchResult() | sequence: {}, menuId: {}, ingredientCount: {}",
                    index + 1,
                    menu.getId(),
                    menuFoodIngredients.foodIngredients().size()
            );
        }

        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 응답 병합 종료 | mergeBatchResult() - END | resultSize: {}",
                result.size()
        );
    }

    /**
     * Gemini 배치 응답의 개수와 메뉴 순번 및 음식 재료 데이터를 검증합니다.
     *
     * @param batch 원본 메뉴 배치
     * @param response Gemini 배치 응답
     * @param availableFoodIngredientIds Gemini에 제공한 음식 재료 식별자 집합
     * @return 메뉴 순번별 검증 완료 응답
     */
    private Map<Integer, MenuFoodIngredients> validateBatchResponse(
            List<Menu> batch,
            InitMenuAndRecipeFoodIngredientGeminiResponseDto response,
            Set<Long> availableFoodIngredientIds
    ) {
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 응답 검증 시작 | validateBatchResponse() - START | batchSize: {}",
                batch.size()
        );

        if (Objects.isNull(response)
                || Objects.isNull(response.menus())
                || response.menus().size() != batch.size()) {
            Integer responseMenuCount = Objects.isNull(response) || Objects.isNull(response.menus())
                    ? null
                    : response.menus().size();
            log.warn(
                    "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 배치 응답 개수 불일치 | validateBatchResponse() | expectedMenuCount: {}, actualMenuCount: {}, responseNull: {}, menusNull: {}",
                    batch.size(),
                    responseMenuCount,
                    Objects.isNull(response),
                    Objects.nonNull(response) && Objects.isNull(response.menus())
            );
            throw new CustomException(GeminiErrorCode.INVALID_METADATA);
        }

        Map<Integer, MenuFoodIngredients> result = new LinkedHashMap<>();
        for (MenuFoodIngredients menuFoodIngredients : response.menus()) {
            MenuFoodIngredients normalizedMenuFoodIngredients = validateAndNormalizeMenuFoodIngredients(
                    menuFoodIngredients,
                    batch.size(),
                    availableFoodIngredientIds
            );
            if (result.putIfAbsent(
                    normalizedMenuFoodIngredients.sequence(),
                    normalizedMenuFoodIngredients
            ) != null) {
                log.warn(
                        "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 메뉴 순번 중복 | validateBatchResponse() | sequence: {}",
                        normalizedMenuFoodIngredients.sequence()
                );
                throw new CustomException(GeminiErrorCode.INVALID_METADATA);
            }
        }

        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 응답 검증 종료 | validateBatchResponse() - END | resultSize: {}",
                result.size()
        );
        return result;
    }

    /**
     * 단일 메뉴의 Gemini 음식 재료 응답을 검증하고 중복 재료의 사용량을 합산합니다.
     *
     * @param menuFoodIngredients 검증할 메뉴 음식 재료 응답
     * @param batchSize 허용되는 메뉴 순번 최댓값
     * @param availableFoodIngredientIds Gemini에 제공한 음식 재료 식별자 집합
     * @return 음식 재료 식별자 중복을 제거한 메뉴별 필요량
     */
    private MenuFoodIngredients validateAndNormalizeMenuFoodIngredients(
            MenuFoodIngredients menuFoodIngredients,
            int batchSize,
            Set<Long> availableFoodIngredientIds
    ) {
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 메뉴별 음식 재료 응답 검증 및 정규화 시작 | validateAndNormalizeMenuFoodIngredients() - START"
        );

        if (Objects.isNull(menuFoodIngredients)
                || Objects.isNull(menuFoodIngredients.sequence())
                || menuFoodIngredients.sequence() < 1
                || menuFoodIngredients.sequence() > batchSize
                || Objects.isNull(menuFoodIngredients.foodIngredients())
                || menuFoodIngredients.foodIngredients().isEmpty()) {
            log.warn(
                    "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 메뉴 음식 재료 응답 형식 오류 | validateAndNormalizeMenuFoodIngredients() | sequence: {}, batchSize: {}, ingredientCount: {}",
                    Objects.isNull(menuFoodIngredients) ? null : menuFoodIngredients.sequence(),
                    batchSize,
                    Objects.isNull(menuFoodIngredients) || Objects.isNull(menuFoodIngredients.foodIngredients())
                            ? null
                            : menuFoodIngredients.foodIngredients().size()
            );
            throw new CustomException(GeminiErrorCode.INVALID_METADATA);
        }

        Map<Long, FoodIngredientNeedAmount> normalizedNeedAmounts = new LinkedHashMap<>();
        for (int index = 0; index < menuFoodIngredients.foodIngredients().size(); index++) {
            FoodIngredientNeedAmount needAmount = menuFoodIngredients.foodIngredients().get(index);
            Long foodIngredientId = Objects.isNull(needAmount) ? null : needAmount.foodIngredientId();
            Double primaryNeedAmountValue = Objects.isNull(needAmount)
                    ? null
                    : needAmount.primaryNeedAmountValue();
            Double secondaryNeedAmountValue = Objects.isNull(needAmount)
                    ? null
                    : needAmount.secondaryNeedAmountValue();
            boolean availableId = Objects.nonNull(foodIngredientId)
                    && availableFoodIngredientIds.contains(foodIngredientId);
            boolean validPrimaryNeedAmount = isPositiveFiniteAmount(primaryNeedAmountValue);
            boolean validSecondaryNeedAmount = Objects.isNull(secondaryNeedAmountValue)
                    || isPositiveFiniteAmount(secondaryNeedAmountValue);

            if (Objects.isNull(needAmount)
                    || Objects.isNull(foodIngredientId)
                    || !availableId
                    || !validPrimaryNeedAmount
                    || !validSecondaryNeedAmount) {
                log.warn(
                        "[InitMenuAndRecipeFoodIngredientGeminiService] Gemini 음식 재료 필요량 검증 실패 | validateAndNormalizeMenuFoodIngredients() | sequence: {}, ingredientIndex: {}, foodIngredientId: {}, primaryNeedAmountValue: {}, secondaryNeedAmountValue: {}, nullNeedAmount: {}, availableId: {}, validPrimaryNeedAmount: {}, validSecondaryNeedAmount: {}",
                        menuFoodIngredients.sequence(),
                        index,
                        foodIngredientId,
                        primaryNeedAmountValue,
                        secondaryNeedAmountValue,
                        Objects.isNull(needAmount),
                        availableId,
                        validPrimaryNeedAmount,
                        validSecondaryNeedAmount
                );
                throw new CustomException(GeminiErrorCode.INVALID_METADATA);
            }

            normalizedNeedAmounts.merge(
                    foodIngredientId,
                    needAmount,
                    this::mergeNeedAmounts
            );
        }

        MenuFoodIngredients result = MenuFoodIngredients.create(
                menuFoodIngredients.sequence(),
                List.copyOf(normalizedNeedAmounts.values())
        );
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 메뉴별 음식 재료 응답 검증 및 정규화 종료 | validateAndNormalizeMenuFoodIngredients() - END | sequence: {}, originalIngredientCount: {}, normalizedIngredientCount: {}",
                menuFoodIngredients.sequence(),
                menuFoodIngredients.foodIngredients().size(),
                result.foodIngredients().size()
        );
        return result;
    }

    /**
     * 동일한 음식 재료의 주 사용량과 보조 사용량을 각각 합산합니다.
     *
     * @param first 먼저 반환된 음식 재료 필요량
     * @param second 중복 반환된 음식 재료 필요량
     * @return 합산된 음식 재료 필요량
     */
    private FoodIngredientNeedAmount mergeNeedAmounts(
            FoodIngredientNeedAmount first,
            FoodIngredientNeedAmount second
    ) {
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 중복 음식 재료 사용량 합산 시작 | mergeNeedAmounts() - START | foodIngredientId: {}",
                first.foodIngredientId()
        );

        double primaryNeedAmountValue = first.primaryNeedAmountValue()
                + second.primaryNeedAmountValue();
        Double secondaryNeedAmountValue = mergeNullableAmounts(
                first.secondaryNeedAmountValue(),
                second.secondaryNeedAmountValue()
        );
        if (!isPositiveFiniteAmount(primaryNeedAmountValue)
                || Objects.nonNull(secondaryNeedAmountValue)
                && !isPositiveFiniteAmount(secondaryNeedAmountValue)) {
            log.warn(
                    "[InitMenuAndRecipeFoodIngredientGeminiService] 중복 음식 재료 사용량 합산 실패 | mergeNeedAmounts() | foodIngredientId: {}, primaryNeedAmountValue: {}, secondaryNeedAmountValue: {}",
                    first.foodIngredientId(),
                    primaryNeedAmountValue,
                    secondaryNeedAmountValue
            );
            throw new CustomException(GeminiErrorCode.INVALID_METADATA);
        }

        FoodIngredientNeedAmount result = FoodIngredientNeedAmount.create(
                first.foodIngredientId(),
                primaryNeedAmountValue,
                secondaryNeedAmountValue
        );
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 중복 음식 재료 사용량 합산 종료 | mergeNeedAmounts() - END | foodIngredientId: {}, primaryNeedAmountValue: {}, secondaryNeedAmountValue: {}",
                result.foodIngredientId(),
                result.primaryNeedAmountValue(),
                result.secondaryNeedAmountValue()
        );
        return result;
    }

    /**
     * 선택 값인 보조 사용량을 null 상태를 유지하면서 합산합니다.
     *
     * @param first 첫 번째 보조 사용량
     * @param second 두 번째 보조 사용량
     * @return 두 값이 모두 null이면 null, 아니면 존재하는 값의 합계
     */
    private Double mergeNullableAmounts(Double first, Double second) {
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 보조 사용량 합산 시작 | mergeNullableAmounts() - START | first: {}, second: {}",
                first,
                second
        );
        if (Objects.isNull(first) && Objects.isNull(second)) {
            log.debug(
                    "[InitMenuAndRecipeFoodIngredientGeminiService] 보조 사용량 합산 종료 | mergeNullableAmounts() - END | result: null"
            );
            return null;
        }
        double result = Objects.requireNonNullElse(first, 0.0)
                + Objects.requireNonNullElse(second, 0.0);
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 보조 사용량 합산 종료 | mergeNullableAmounts() - END | result: {}",
                result
        );
        return result;
    }

    /**
     * 사용량이 양수이면서 유한한 값인지 확인합니다.
     *
     * @param value 검증할 사용량
     * @return 양의 유한값 여부
     */
    private boolean isPositiveFiniteAmount(Double value) {
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 음식 재료 사용량 검증 시작 | isPositiveFiniteAmount() - START | value: {}",
                value
        );
        boolean result = Objects.nonNull(value) && Double.isFinite(value) && value > 0;
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 음식 재료 사용량 검증 종료 | isPositiveFiniteAmount() - END | result: {}",
                result
        );
        return result;
    }

    /**
     * 타입별 음식 재료 목록에서 유효한 음식 재료 식별자를 추출합니다.
     *
     * @param foodIngredientsByType 타입별 음식 재료 목록
     * @return 음식 재료 식별자 집합
     */
    private Set<Long> extractFoodIngredientIds(
            Map<FoodIngredientType, List<FoodIngredient>> foodIngredientsByType
    ) {
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 음식 재료 식별자 추출 시작 | extractFoodIngredientIds() - START"
        );
        if (Objects.isNull(foodIngredientsByType)) {
            log.debug(
                    "[InitMenuAndRecipeFoodIngredientGeminiService] 음식 재료 식별자 추출 종료 | extractFoodIngredientIds() - END | resultSize: 0"
            );
            return Set.of();
        }
        Set<Long> result = foodIngredientsByType.values().stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .map(FoodIngredient::getId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        log.debug(
                "[InitMenuAndRecipeFoodIngredientGeminiService] 음식 재료 식별자 추출 종료 | extractFoodIngredientIds() - END | resultSize: {}, typeCount: {}",
                result.size(),
                foodIngredientsByType.size()
        );
        return result;
    }
}
