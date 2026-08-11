package com.likelion.routineeatbe.domain.menu.service.gemini;

import com.likelion.routineeatbe.domain.menu.dto.MenuAndRecipeMetaDataBatchDto;
import com.likelion.routineeatbe.domain.menu.dto.MenuAndRecipeMetaDataBatchDto.MenuMetaData;
import com.likelion.routineeatbe.domain.menu.dto.MenuAndRecipeMetaDataDto;
import com.likelion.routineeatbe.domain.menu.dto.response.MenuAndRecipeCrawlingDto;
import com.likelion.routineeatbe.global.config.GeminiProperties;
import com.likelion.routineeatbe.domain.menu.dto.gemini.MenuAndRecipeGeminiFunctionDeclarationDto;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import com.likelion.routineeatbe.global.util.GeminiRetryDelayStrategy;
import com.likelion.routineeatbe.global.util.GeminiUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
public class MenuAndRecipeGeminiService {

    private static final int MIN_TIME_REQUIRED = 1;
    private static final int MAX_TIME_REQUIRED = 1_440;

    private final GeminiUtil geminiUtil;
    private final GeminiRetryDelayStrategy retryDelayStrategy;
    private final GeminiProperties properties;

    /**
     * (1) 작업 목적
     * 크롤링한 메뉴를 설정된 배치 크기로 나누어 종류, 추천 유형, 예상 조리시간을 Gemini로 생성합니다.
     *
     * (2) 세부 작업 내용
     * - 각 배치를 하나의 Gemini Function Call 입력으로 사용합니다.
     * - 429 오류만 지수 백오프 후 제한된 횟수만큼 재시도합니다.
     * - 생성 결과를 원본 메뉴명 및 순서 기준 Map으로 구성하여 저장 단계의 데이터 연결을 보장합니다.
     *
     * @param crawlingDtos 메타데이터를 생성할 크롤링 메뉴 목록
     * @return 메뉴명을 Key로 사용하는 메뉴 메타데이터 Map
     */
    public Map<String, MenuAndRecipeMetaDataDto> generateMetaData(
            List<MenuAndRecipeCrawlingDto> crawlingDtos
    ) {
        log.info(
                "[MenuAndRecipeGeminiService] 메뉴 메타데이터 배치 생성 시작 | generateMetaData() - START | menuCount: {}",
                crawlingDtos.size()
        );

        // 1. 중복된 메뉴명이 있는지 검증한다. 이후 크롤링해온 데이터를 Batch 단위로 분할한다.
        validateUniqueMenuNames(crawlingDtos);
        Map<String, MenuAndRecipeMetaDataDto> result = new LinkedHashMap<>();
        List<List<MenuAndRecipeCrawlingDto>> batches = partition(crawlingDtos);

        // 2. 배치 크롤링 데이터를 한번에 Gemini 요청을 보내고, 그 결과값을 각 메뉴별 sequence 값으로 검증하고 key: 메뉴명 / value: 메뉴별 메타데이터 형식으로 변환한다.
        // 메뉴별 sequence 값은 메뉴명 앞에 붙어있는 메뉴 번호를 사용한다.
        for (int index = 0; index < batches.size(); index++) {
            List<MenuAndRecipeCrawlingDto> batch = batches.get(index);
            MenuAndRecipeMetaDataBatchDto response = callBatchWithRetry(batch, index + 1);
            mergeBatchResult(batch, response, result);
        }

        log.info(
                "[MenuAndRecipeGeminiService] 메뉴 메타데이터 배치 생성 종료 | generateMetaData() - END | resultSize: {}, batchCount: {}",
                result.size(),
                batches.size()
        );
        return result;
    }

    /**
     * 크롤링 메뉴 목록을 Gemini 호출 단위로 분할합니다.
     *
     * @param crawlingDtos 분할할 크롤링 메뉴 목록
     * @return 설정된 크기 이하로 분할된 배치 목록
     */
    private List<List<MenuAndRecipeCrawlingDto>> partition(
            List<MenuAndRecipeCrawlingDto> crawlingDtos
    ) {
        log.debug(
                "[MenuAndRecipeGeminiService] Gemini 배치 분할 시작 | partition() - START | menuCount: {}, batchSize: {}",
                crawlingDtos.size(),
                properties.batchSize()
        );

        List<List<MenuAndRecipeCrawlingDto>> result = new ArrayList<>();
        for (int start = 0; start < crawlingDtos.size(); start += properties.batchSize()) {
            int end = Math.min(start + properties.batchSize(), crawlingDtos.size());
            result.add(List.copyOf(crawlingDtos.subList(start, end)));
        }

        log.debug(
                "[MenuAndRecipeGeminiService] Gemini 배치 분할 종료 | partition() - END | batchCount: {}",
                result.size()
        );
        return result;
    }

    /**
     * 단일 배치를 Gemini로 호출하고 429 오류가 발생한 경우에만 재시도합니다.
     *
     * @param batch Gemini에 전달할 메뉴 배치
     * @param batchNumber 로그에 사용할 배치 순번
     * @return Gemini가 생성한 배치 메타데이터
     */
    private MenuAndRecipeMetaDataBatchDto callBatchWithRetry(
            List<MenuAndRecipeCrawlingDto> batch,
            int batchNumber
    ) {
        log.debug(
                "[MenuAndRecipeGeminiService] Gemini 배치 호출 시작 | callBatchWithRetry() - START | batchNumber: {}, batchSize: {}",
                batchNumber,
                batch.size()
        );

        int maxAttempts = properties.retry().maxAttempts();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                MenuAndRecipeMetaDataBatchDto result = geminiUtil.callFunction(
                        properties.menuAnalyzeModel(),
                        createBatchPrompt(batch),
                        MenuAndRecipeGeminiFunctionDeclarationDto.create(batch.size()),
                        MenuAndRecipeMetaDataBatchDto.class
                );
                log.debug(
                        "[MenuAndRecipeGeminiService] Gemini 배치 호출 종료 | callBatchWithRetry() - END | batchNumber: {}, attempt: {}",
                        batchNumber,
                        attempt
                );
                return result;
            } catch (CustomException exception) {
                boolean rateLimited = exception.getErrorCode() == GeminiErrorCode.RATE_LIMIT_EXCEEDED;
                if (!rateLimited || attempt == maxAttempts) {
                    throw exception;
                }
                log.warn(
                        "[MenuAndRecipeGeminiService] Gemini 배치 재시도 | batchNumber: {}, attempt: {}, maxAttempts: {}",
                        batchNumber,
                        attempt,
                        maxAttempts
                );
                retryDelayStrategy.waitBeforeRetry(attempt);
            }
        }
        throw new CustomException(GeminiErrorCode.RATE_LIMIT_EXCEEDED);
    }

    /**
     * 배치 입력에 포함된 모든 메뉴 정보와 조리 단계를 하나의 Gemini 프롬프트로 생성합니다.
     *
     * @param batch 프롬프트 원본 메뉴 배치
     * @return Gemini에 전달할 배치 프롬프트
     */
    private String createBatchPrompt(List<MenuAndRecipeCrawlingDto> batch) {
        log.debug(
                "[MenuAndRecipeGeminiService] Gemini 배치 프롬프트 생성 시작 | createBatchPrompt() - START | batchSize: {}",
                batch.size()
        );

        StringBuilder prompt = new StringBuilder("""
                다음 메뉴 정보를 각각 분석하여 메뉴 메타데이터를 생성하세요.
                입력 메뉴마다 결과를 정확히 하나씩 생성하세요.
                각 결과의 sequence에는 메뉴 앞에 표시된 원본 순번을 그대로 사용하고, menuName은 응답에 포함하지 마세요.

                메뉴 종류는 KOREAN, WESTERN, JAPANESE, CHINESE 중 하나로 분류하세요.
                조리시간은 재료 준비부터 조리 완료까지 필요한 시간을 1분 이상 1440분 이하의 정수로 계산하세요.
                끓이기, 굽기와 24시간 이내의 불리기·재우기 대기시간은 포함하세요.
                24시간을 초과하는 숙성, 발효, 저장, 보관 등의 장기 대기시간은 제외하세요.

                추천 유형은 다음 우선순위와 기준에 따라 하나만 선택하세요.
                1. GLUTEN_FREE: 밀, 보리, 호밀 및 글루텐 함유 소스가 없다고 명확히 판단할 수 있는 메뉴
                2. DIET: 비교적 낮은 칼로리이며 삶기, 찌기, 굽기 중심인 메뉴
                3. SIMPLE: 조리 단계가 5개 이하이고 장시간 숙성이나 발효가 필요하지 않은 간단한 메뉴
                4. DEFAULT: 위 조건에 해당하지 않거나 판단 근거가 부족한 메뉴
                글루텐 포함 여부가 불확실하면 GLUTEN_FREE로 분류하지 마세요.

                """);

        IntStream.range(0, batch.size())
                .forEach(index -> prompt.append(createMenuPrompt(batch.get(index), index + 1)));
        String result = prompt.toString();

        log.debug(
                "[MenuAndRecipeGeminiService] Gemini 배치 프롬프트 생성 종료 | createBatchPrompt() - END | promptLength: {}",
                result.length()
        );
        return result;
    }

    /**
     * 배치 프롬프트에 포함할 단일 메뉴 구간을 생성합니다.
     *
     * @param crawlingDto 프롬프트 원본 메뉴
     * @param sequence 배치 내부 메뉴 순번
     * @return 단일 메뉴 프롬프트 구간
     */
    private String createMenuPrompt(MenuAndRecipeCrawlingDto crawlingDto, int sequence) {
        log.debug(
                "[MenuAndRecipeGeminiService] 단일 메뉴 프롬프트 생성 시작 | createMenuPrompt() - START | sequence: {}, menuName: {}",
                sequence,
                crawlingDto.menuName()
        );

        String recipeSteps = IntStream.range(0, crawlingDto.recipes().size())
                .mapToObj(index -> String.format(
                        "%d. %s",
                        index + 1,
                        crawlingDto.recipes().get(index).contents()
                ))
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("조리 단계 정보 없음");

        String result = """
                --- 메뉴 %d ---
                menuName: %s
                칼로리: %s kcal
                재료 정보: %s
                조리 단계:
                %s

                """.formatted(
                sequence,
                crawlingDto.menuName(),
                crawlingDto.calories(),
                crawlingDto.ingredientDetails(),
                recipeSteps
        );

        log.debug(
                "[MenuAndRecipeGeminiService] 단일 메뉴 프롬프트 생성 종료 | createMenuPrompt() - END | sequence: {}, menuName: {}",
                sequence,
                crawlingDto.menuName()
        );
        return result;
    }

    /**
     * Gemini 배치 응답을 검증하고 원본 메뉴 순서대로 결과 Map에 병합합니다.
     *
     * @param batch 원본 메뉴 배치
     * @param response Gemini 배치 응답
     * @param result 전체 결과 Map
     */
    private void mergeBatchResult(
            List<MenuAndRecipeCrawlingDto> batch,
            MenuAndRecipeMetaDataBatchDto response,
            Map<String, MenuAndRecipeMetaDataDto> result
    ) {
        log.debug(
                "[MenuAndRecipeGeminiService] Gemini 배치 결과 병합 시작 | mergeBatchResult() - START | batchSize: {}",
                batch.size()
        );

        Map<Integer, MenuMetaData> metaDataBySequence = validateBatchResponse(batch, response);
        for (int index = 0; index < batch.size(); index++) {
            MenuAndRecipeCrawlingDto crawlingDto = batch.get(index);
            MenuMetaData metaData = metaDataBySequence.get(index + 1);
            result.put(
                    crawlingDto.menuName(),
                    MenuAndRecipeMetaDataDto.create(
                            metaData.menuType(),
                            metaData.recommendationType(),
                            metaData.timeRequired(),
                            crawlingDto.mainImageUrl()
                    )
            );
        }

        log.debug(
                "[MenuAndRecipeGeminiService] Gemini 배치 결과 병합 종료 | mergeBatchResult() - END | resultSize: {}",
                result.size()
        );
    }

    /**
     * Gemini 배치 응답의 개수, 메뉴 순번 및 메타데이터 유효성을 검증합니다.
     *
     * @param batch 원본 메뉴 배치
     * @param response Gemini 배치 응답
     * @return 메뉴 순번 기준으로 구성한 검증 완료 메타데이터 Map
     */
    private Map<Integer, MenuMetaData> validateBatchResponse(
            List<MenuAndRecipeCrawlingDto> batch,
            MenuAndRecipeMetaDataBatchDto response
    ) {
        log.debug(
                "[MenuAndRecipeGeminiService] Gemini 배치 응답 검증 시작 | validateBatchResponse() - START | batchSize: {}",
                batch.size()
        );

        if (Objects.isNull(response)
                || Objects.isNull(response.menus())
                || response.menus().size() != batch.size()) {
            Integer actualSize = Objects.isNull(response) || Objects.isNull(response.menus())
                    ? null
                    : response.menus().size();
            log.warn(
                    "[MenuAndRecipeGeminiService] Gemini 배치 응답 검증 실패 | reason: INVALID_BATCH_SIZE | expectedSize: {}, actualSize: {}, responseNull: {}, menusNull: {}",
                    batch.size(),
                    actualSize,
                    Objects.isNull(response),
                    Objects.nonNull(response) && Objects.isNull(response.menus())
            );
            throw new CustomException(GeminiErrorCode.INVALID_METADATA);
        }

        Map<Integer, MenuMetaData> result = new LinkedHashMap<>();

        for (int index = 0; index < response.menus().size(); index++) {
            MenuMetaData metaData = response.menus().get(index);
            validateMetaData(metaData, index, batch.size());

            if (result.putIfAbsent(metaData.sequence(), metaData) != null) {
                log.warn(
                        "[MenuAndRecipeGeminiService] Gemini 배치 응답 검증 실패 | reason: DUPLICATE_SEQUENCE | responseIndex: {}, duplicateSequence: {}",
                        index,
                        metaData.sequence()
                );
                throw new CustomException(GeminiErrorCode.INVALID_METADATA);
            }
        }

        log.debug(
                "[MenuAndRecipeGeminiService] Gemini 배치 응답 검증 종료 | validateBatchResponse() - END | resultSize: {}",
                result.size()
        );
        return result;
    }

    /**
     * Gemini가 생성한 단일 메뉴 메타데이터의 유효성을 검증합니다.
     *
     * @param metaData 검증할 메뉴 메타데이터
     * @param responseIndex 배치 응답 내부 순번
     * @param batchSize 유효한 메뉴 순번의 최댓값
     */
    private void validateMetaData(MenuMetaData metaData, int responseIndex, int batchSize) {
        log.debug(
                "[MenuAndRecipeGeminiService] 메뉴 메타데이터 검증 시작 | validateMetaData() - START | responseIndex: {}",
                responseIndex
        );

        if (Objects.isNull(metaData)) {
            log.warn(
                    "[MenuAndRecipeGeminiService] 메뉴 메타데이터 검증 실패 | reason: METADATA_NULL | responseIndex: {}",
                    responseIndex
            );
            throw new CustomException(GeminiErrorCode.INVALID_METADATA);
        }
        if (Objects.isNull(metaData.sequence())
                || metaData.sequence() < 1
                || metaData.sequence() > batchSize) {
            log.warn(
                    "[MenuAndRecipeGeminiService] 메뉴 메타데이터 검증 실패 | reason: INVALID_SEQUENCE | responseIndex: {}, sequence: {}, batchSize: {}",
                    responseIndex,
                    metaData.sequence(),
                    batchSize
            );
            throw new CustomException(GeminiErrorCode.INVALID_METADATA);
        }
        if (Objects.isNull(metaData.menuType())) {
            log.warn(
                    "[MenuAndRecipeGeminiService] 메뉴 메타데이터 검증 실패 | reason: MENU_TYPE_NULL | responseIndex: {}, sequence: {}",
                    responseIndex,
                    metaData.sequence()
            );
            throw new CustomException(GeminiErrorCode.INVALID_METADATA);
        }
        if (Objects.isNull(metaData.recommendationType())) {
            log.warn(
                    "[MenuAndRecipeGeminiService] 메뉴 메타데이터 검증 실패 | reason: RECOMMENDATION_TYPE_NULL | responseIndex: {}, sequence: {}",
                    responseIndex,
                    metaData.sequence()
            );
            throw new CustomException(GeminiErrorCode.INVALID_METADATA);
        }
        if (Objects.isNull(metaData.timeRequired())) {
            log.warn(
                    "[MenuAndRecipeGeminiService] 메뉴 메타데이터 검증 실패 | reason: TIME_REQUIRED_NULL | responseIndex: {}, sequence: {}",
                    responseIndex,
                    metaData.sequence()
            );
            throw new CustomException(GeminiErrorCode.INVALID_METADATA);
        }
        if (metaData.timeRequired() < MIN_TIME_REQUIRED
                || metaData.timeRequired() > MAX_TIME_REQUIRED) {
            log.warn(
                    "[MenuAndRecipeGeminiService] 메뉴 메타데이터 검증 실패 | reason: TIME_REQUIRED_OUT_OF_RANGE | responseIndex: {}, sequence: {}, timeRequired: {}, min: {}, max: {}",
                    responseIndex,
                    metaData.sequence(),
                    metaData.timeRequired(),
                    MIN_TIME_REQUIRED,
                    MAX_TIME_REQUIRED
            );
            throw new CustomException(GeminiErrorCode.INVALID_METADATA);
        }

        log.debug(
                "[MenuAndRecipeGeminiService] 메뉴 메타데이터 검증 종료 | validateMetaData() - END | responseIndex: {}, sequence: {}",
                responseIndex,
                metaData.sequence()
        );
    }

    /**
     * Gemini 호출 전 입력 메뉴명 중복 및 null 값을 검증합니다.
     *
     * @param crawlingDtos 검증할 크롤링 메뉴 목록
     */
    private void validateUniqueMenuNames(List<MenuAndRecipeCrawlingDto> crawlingDtos) {
        log.debug(
                "[MenuAndRecipeGeminiService] 입력 메뉴명 검증 시작 | validateUniqueMenuNames() - START | menuCount: {}",
                crawlingDtos.size()
        );

        Set<String> menuNames = new HashSet<>();
        for (int index = 0; index < crawlingDtos.size(); index++) {
            MenuAndRecipeCrawlingDto crawlingDto = crawlingDtos.get(index);
            if (Objects.isNull(crawlingDto)) {
                log.warn(
                        "[MenuAndRecipeGeminiService] 입력 메뉴명 검증 실패 | reason: CRAWLING_DTO_NULL | inputIndex: {}",
                        index
                );
                throw new CustomException(GeminiErrorCode.INVALID_METADATA);
            }
            if (Objects.isNull(crawlingDto.menuName()) || crawlingDto.menuName().isBlank()) {
                log.warn(
                        "[MenuAndRecipeGeminiService] 입력 메뉴명 검증 실패 | reason: INVALID_INPUT_MENU_NAME | inputIndex: {}, menuName: {}",
                        index,
                        crawlingDto.menuName()
                );
                throw new CustomException(GeminiErrorCode.INVALID_METADATA);
            }
            if (!menuNames.add(crawlingDto.menuName())) {
                log.warn(
                        "[MenuAndRecipeGeminiService] 입력 메뉴명 검증 실패 | reason: DUPLICATE_INPUT_MENU_NAME | inputIndex: {}, menuName: {}",
                        index,
                        crawlingDto.menuName()
                );
                throw new CustomException(GeminiErrorCode.INVALID_METADATA);
            }
        }

        log.debug(
                "[MenuAndRecipeGeminiService] 입력 메뉴명 검증 종료 | validateUniqueMenuNames() - END | uniqueMenuCount: {}",
                menuNames.size()
        );
    }
}
