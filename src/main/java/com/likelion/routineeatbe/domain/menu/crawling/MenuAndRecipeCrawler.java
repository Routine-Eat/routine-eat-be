package com.likelion.routineeatbe.domain.menu.crawling;

import com.likelion.routineeatbe.domain.menu.dto.request.FoodSafetyKoreaRecipeApiRequestDto;
import com.likelion.routineeatbe.domain.menu.dto.response.FoodSafetyKoreaRecipeApiResponseDto;
import com.likelion.routineeatbe.domain.menu.dto.response.FoodSafetyKoreaRecipeApiResponseDto.CookRecipeData;
import com.likelion.routineeatbe.domain.menu.exception.FoodSafetyKoreaApiMessageCode;
import com.likelion.routineeatbe.domain.menu.exception.MenuCrawlingErrorCode;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(FoodSafetyKoreaProperties.class)
public class MenuAndRecipeCrawler {

    private final RestTemplate restTemplate;
    private final FoodSafetyKoreaProperties properties;

    /**
     * 식품안전청 조리식품 레시피 API를 호출한다.
     *
     * @param startIdx 조회 시작 위치
     * @param endIdx 조회 종료 위치
     * @return 식품안전청 레시피 API 원본 응답
     */
    public FoodSafetyKoreaRecipeApiResponseDto crawl(int startIdx, int endIdx) {
        log.info("[MenuAndRecipeCrawler.crawl] 식품안전청 메뉴, 레시피 크롤링 시작 | START - startIdx: {}, endIdx: {}", startIdx, endIdx);

        FoodSafetyKoreaRecipeApiRequestDto request = createRequest(startIdx, endIdx);
        URI requestUri = createRequestUri(request);

        try {
            FoodSafetyKoreaRecipeApiResponseDto response = restTemplate.getForObject(
                    requestUri,
                    FoodSafetyKoreaRecipeApiResponseDto.class
            );
            validateResponse(response);
            FoodSafetyKoreaRecipeApiResponseDto result = removeDuplicateMenus(response);

            log.info(
                    "[MenuAndRecipeCrawler.crawl] 식품안전청 메뉴, 레시피 크롤링 완료 END - startIdx: {}, endIdx: {}, totalCount: {}, resultSize: {}",
                    startIdx,
                    endIdx,
                    result.cookRecipeData().totalCount(),
                    result.cookRecipeData().rows().size()
            );
            return result;
        } catch (ResourceAccessException exception) {
            if (hasTimeoutCause(exception)) {
                log.error("식품안전청 API 응답 시간 초과 - startIdx: {}, endIdx: {}", startIdx, endIdx);
                throw new CustomException(MenuCrawlingErrorCode.EXTERNAL_API_TIMEOUT);
            }
            log.error("식품안전청 API 연결 실패 - startIdx: {}, endIdx: {}", startIdx, endIdx);
            throw new CustomException(MenuCrawlingErrorCode.EXTERNAL_API_CALL_FAILED);
        } catch (RestClientException exception) {
            log.error("식품안전청 API 호출 실패 - startIdx: {}, endIdx: {}", startIdx, endIdx);
            throw new CustomException(MenuCrawlingErrorCode.EXTERNAL_API_CALL_FAILED);
        }
    }

    /**
     * 식품안전청 조리식품 API의 전체 데이터를 최대 요청 건수 단위로 반복 조회한다.
     *
     * @return 전체 조리식품 데이터가 병합된 API 응답
     */
    public FoodSafetyKoreaRecipeApiResponseDto crawlAll() {
        log.info("[MenuAndRecipeCrawler.crawlAll] 식품안전청 전체 메뉴, 레시피 크롤링 시작 | START");

        int maxRequestCount = properties.maxRequestCount();
        int startIdx = 1;
        int endIdx = maxRequestCount;
        FoodSafetyKoreaRecipeApiResponseDto firstResponse = crawl(startIdx, endIdx);
        int totalCount = parseTotalCount(firstResponse);
        List<FoodSafetyKoreaRecipeApiResponseDto.RecipeRow> rows = new ArrayList<>(
                firstResponse.cookRecipeData().rows()
        );

        for (startIdx = endIdx + 1; startIdx <= totalCount; startIdx = endIdx + 1) {
            endIdx = Math.min(startIdx + maxRequestCount - 1, totalCount);
            FoodSafetyKoreaRecipeApiResponseDto response = crawl(startIdx, endIdx);
            rows.addAll(response.cookRecipeData().rows());
        }

        List<FoodSafetyKoreaRecipeApiResponseDto.RecipeRow> uniqueRows = removeDuplicateRows(rows);
        FoodSafetyKoreaRecipeApiResponseDto result = mergeResponses(firstResponse, uniqueRows);
        log.info(
                "[MenuAndRecipeCrawler.crawlAll] 식품안전청 전체 메뉴, 레시피 크롤링 종료 | END - totalCount: {}, resultSize: {}",
                totalCount,
                uniqueRows.size()
        );
        return result;
    }

    private FoodSafetyKoreaRecipeApiRequestDto createRequest(int startIdx, int endIdx) {
        long requestCount = (long) endIdx - startIdx + 1;
        if (startIdx < 1 || endIdx < startIdx || requestCount > properties.maxRequestCount()) {
            throw new CustomException(MenuCrawlingErrorCode.INVALID_CRAWLING_RANGE);
        }

        return FoodSafetyKoreaRecipeApiRequestDto.create(
                properties.apiKey(),
                properties.serviceId(),
                properties.dataType(),
                startIdx,
                endIdx
        );
    }

    private int parseTotalCount(FoodSafetyKoreaRecipeApiResponseDto response) {
        try {
            int totalCount = Integer.parseInt(response.cookRecipeData().totalCount());
            if (totalCount < 0) {
                throw new CustomException(MenuCrawlingErrorCode.INVALID_EXTERNAL_API_RESPONSE);
            }
            return totalCount;
        } catch (NullPointerException | NumberFormatException exception) {
            throw new CustomException(MenuCrawlingErrorCode.INVALID_EXTERNAL_API_RESPONSE);
        }
    }

    private FoodSafetyKoreaRecipeApiResponseDto mergeResponses(
            FoodSafetyKoreaRecipeApiResponseDto firstResponse,
            List<FoodSafetyKoreaRecipeApiResponseDto.RecipeRow> rows
    ) {
        CookRecipeData mergedData = CookRecipeData.builder()
                .totalCount(firstResponse.cookRecipeData().totalCount())
                .rows(rows)
                .result(firstResponse.cookRecipeData().result())
                .build();
        return FoodSafetyKoreaRecipeApiResponseDto.create(mergedData);
    }

    private FoodSafetyKoreaRecipeApiResponseDto removeDuplicateMenus(
            FoodSafetyKoreaRecipeApiResponseDto response
    ) {
        return mergeResponses(
                response,
                removeDuplicateRows(response.cookRecipeData().rows())
        );
    }

    /**
     * 크롤링해온 데이터들 중 중복된 행들을 제거한다.
     * Key: 메뉴명 / Value: RecipeRow 객체
     * @param rows
     * @return
     */
    private List<FoodSafetyKoreaRecipeApiResponseDto.RecipeRow> removeDuplicateRows(
            List<FoodSafetyKoreaRecipeApiResponseDto.RecipeRow> rows
    ) {
        Map<String, FoodSafetyKoreaRecipeApiResponseDto.RecipeRow> uniqueRows = new LinkedHashMap<>();
        rows.forEach(row -> uniqueRows.putIfAbsent(row.menuName(), row));
        return new ArrayList<>(uniqueRows.values());
    }

    private URI createRequestUri(FoodSafetyKoreaRecipeApiRequestDto request) {
        return UriComponentsBuilder.fromUriString(properties.baseUrl())
                .pathSegment(
                        request.keyId(),
                        request.serviceId(),
                        request.dataType(),
                        String.valueOf(request.startIdx()),
                        String.valueOf(request.endIdx())
                )
                .build()
                .encode()
                .toUri();
    }

    private void validateResponse(FoodSafetyKoreaRecipeApiResponseDto response) {
        if (response == null || response.cookRecipeData() == null) {
            throw new CustomException(MenuCrawlingErrorCode.INVALID_EXTERNAL_API_RESPONSE);
        }

        CookRecipeData cookRecipeData = response.cookRecipeData();
        if (cookRecipeData.result() == null || cookRecipeData.result().code() == null) {
            throw new CustomException(MenuCrawlingErrorCode.INVALID_EXTERNAL_API_RESPONSE);
        }

        FoodSafetyKoreaApiMessageCode messageCode = FoodSafetyKoreaApiMessageCode
                .findByCode(cookRecipeData.result().code())
                .orElseThrow(() -> new CustomException(MenuCrawlingErrorCode.INVALID_EXTERNAL_API_RESPONSE));

        if (messageCode != FoodSafetyKoreaApiMessageCode.INFO_000) {
            log.warn("식품안전청 API 오류 응답 - code: {}", cookRecipeData.result().code());
            throw new CustomException(messageCode);
        }
    }

    private boolean hasTimeoutCause(Throwable throwable) {
        Throwable current = throwable;
        while (Objects.nonNull(current)) {
            if (current instanceof SocketTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
