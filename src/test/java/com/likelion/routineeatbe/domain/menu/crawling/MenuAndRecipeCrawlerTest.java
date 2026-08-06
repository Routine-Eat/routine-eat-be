package com.likelion.routineeatbe.domain.menu.crawling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.likelion.routineeatbe.domain.menu.dto.response.FoodSafetyKoreaRecipeApiResponseDto;
import com.likelion.routineeatbe.domain.menu.exception.FoodSafetyKoreaApiMessageCode;
import com.likelion.routineeatbe.domain.menu.exception.MenuCrawlingErrorCode;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.net.SocketTimeoutException;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

class MenuAndRecipeCrawlerTest {

    private static final String BASE_URL = "https://openapi.foodsafetykorea.go.kr/api";
    private static final String EXPECTED_URL = BASE_URL + "/test-key/COOKRCP01/json/1/1";
    private static final String TWO_ROWS_URL = BASE_URL + "/test-key/COOKRCP01/json/1/2";
    private static final String FIRST_PAGE_URL = BASE_URL + "/test-key/COOKRCP01/json/1/1000";
    private static final String SECOND_PAGE_URL = BASE_URL + "/test-key/COOKRCP01/json/1001/1501";

    private MockRestServiceServer mockServer;
    private MenuAndRecipeCrawler crawler;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        FoodSafetyKoreaProperties properties = new FoodSafetyKoreaProperties(
                BASE_URL,
                "test-key",
                "COOKRCP01",
                "json",
                1_000
        );
        crawler = new MenuAndRecipeCrawler(restTemplate, properties);
    }

    @Test
    @DisplayName("식품안전나라 API 정상 응답을 DTO로 반환한다")
    void crawl_success_returnsResponseDto() {
        // given
        String responseBody = """
                {
                  "COOKRCP01": {
                    "total_count": "1",
                    "row": [{"RCP_NM": "테스트 메뉴"}],
                    "RESULT": {"CODE": "INFO-000", "MSG": "정상 처리되었습니다."}
                  }
                }
                """;
        mockServer.expect(once(), requestTo(EXPECTED_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        // when
        FoodSafetyKoreaRecipeApiResponseDto result = crawler.crawl(1, 1);

        // then
        assertThat(result.cookRecipeData().totalCount()).isEqualTo("1");
        assertThat(result.cookRecipeData().rows()).hasSize(1);
        assertThat(result.cookRecipeData().rows().getFirst().menuName()).isEqualTo("테스트 메뉴");
        mockServer.verify();
    }

    @Test
    @DisplayName("응답 본문이 없으면 잘못된 외부 응답 예외가 발생한다")
    void crawl_emptyBody_throwsInvalidResponse() {
        // given
        mockServer.expect(once(), requestTo(EXPECTED_URL))
                .andRespond(withNoContent());

        // when & then
        assertThatThrownBy(() -> crawler.crawl(1, 1))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MenuCrawlingErrorCode.INVALID_EXTERNAL_API_RESPONSE);
        mockServer.verify();
    }

    @Test
    @DisplayName("API 결과 코드가 정상이 아니면 외부 API 호출 실패 예외가 발생한다")
    void crawl_errorResultCode_throwsMatchedMessageCode() {
        // given
        String responseBody = """
                {
                  "COOKRCP01": {
                    "total_count": "0",
                    "row": [],
                    "RESULT": {"CODE": "INFO-100", "MSG": "인증키가 유효하지 않습니다."}
                  }
                }
                """;
        mockServer.expect(once(), requestTo(EXPECTED_URL))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> crawler.crawl(1, 1))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(FoodSafetyKoreaApiMessageCode.INFO_100);
        mockServer.verify();
    }

    @Test
    @DisplayName("알 수 없는 API 메시지 코드는 잘못된 외부 응답으로 처리한다")
    void crawl_unknownResultCode_throwsInvalidResponse() {
        // given
        String responseBody = """
                {
                  "COOKRCP01": {
                    "total_count": "0",
                    "row": [],
                    "RESULT": {"CODE": "UNKNOWN-999", "MSG": "알 수 없는 오류"}
                  }
                }
                """;
        mockServer.expect(once(), requestTo(EXPECTED_URL))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> crawler.crawl(1, 1))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MenuCrawlingErrorCode.INVALID_EXTERNAL_API_RESPONSE);
        mockServer.verify();
    }

    @Test
    @DisplayName("API 서버 오류가 발생하면 외부 API 호출 실패 예외가 발생한다")
    void crawl_serverError_throwsApiCallFailed() {
        // given
        mockServer.expect(once(), requestTo(EXPECTED_URL))
                .andRespond(withServerError());

        // when & then
        assertThatThrownBy(() -> crawler.crawl(1, 1))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MenuCrawlingErrorCode.EXTERNAL_API_CALL_FAILED);
        mockServer.verify();
    }

    @Test
    @DisplayName("시작 위치가 1보다 작으면 요청 범위 예외가 발생한다")
    void crawl_startIndexLessThanOne_throwsInvalidRange() {
        // when & then
        assertThatThrownBy(() -> crawler.crawl(0, 1))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MenuCrawlingErrorCode.INVALID_CRAWLING_RANGE);
    }

    @Test
    @DisplayName("시작 위치가 종료 위치보다 크면 요청 범위 예외가 발생한다")
    void crawl_startIndexExceedsEndIndex_throwsInvalidRange() {
        // when & then
        assertThatThrownBy(() -> crawler.crawl(2, 1))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MenuCrawlingErrorCode.INVALID_CRAWLING_RANGE);
    }

    @Test
    @DisplayName("조회 범위가 1000건을 초과하면 요청 범위 예외가 발생한다")
    void crawl_requestCountExceedsLimit_throwsInvalidRange() {
        // when & then
        assertThatThrownBy(() -> crawler.crawl(1, 1001))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MenuCrawlingErrorCode.INVALID_CRAWLING_RANGE);
    }

    @Test
    @DisplayName("API 응답 시간이 초과되면 타임아웃 예외가 발생한다")
    void crawl_socketTimeout_throwsApiTimeout() {
        // given
        RestTemplate timeoutRestTemplate = mock(RestTemplate.class);
        given(timeoutRestTemplate.getForObject(any(URI.class), eq(FoodSafetyKoreaRecipeApiResponseDto.class)))
                .willThrow(new ResourceAccessException("timeout", new SocketTimeoutException()));
        FoodSafetyKoreaProperties properties = new FoodSafetyKoreaProperties(
                BASE_URL,
                "test-key",
                "COOKRCP01",
                "json",
                1_000
        );
        MenuAndRecipeCrawler timeoutCrawler = new MenuAndRecipeCrawler(timeoutRestTemplate, properties);

        // when & then
        assertThatThrownBy(() -> timeoutCrawler.crawl(1, 1))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MenuCrawlingErrorCode.EXTERNAL_API_TIMEOUT);
    }

    @Test
    @DisplayName("전체 건수까지 반복 조회하고 페이지 간 중복 메뉴를 제거하여 병합한다")
    void 전체_데이터_반복_조회_페이지간_중복_제거_병합_성공() {
        // given
        String firstResponseBody = """
                {
                  "COOKRCP01": {
                    "total_count": "1501",
                    "row": [{"RCP_NM": "중복 메뉴"}],
                    "RESULT": {"CODE": "INFO-000", "MSG": "정상 처리되었습니다."}
                  }
                }
                """;
        String secondResponseBody = """
                {
                  "COOKRCP01": {
                    "total_count": "1501",
                    "row": [
                      {"RCP_NM": "중복 메뉴"},
                      {"RCP_NM": "신규 메뉴"}
                    ],
                    "RESULT": {"CODE": "INFO-000", "MSG": "정상 처리되었습니다."}
                  }
                }
                """;
        mockServer.expect(once(), requestTo(FIRST_PAGE_URL))
                .andRespond(withSuccess(firstResponseBody, MediaType.APPLICATION_JSON));
        mockServer.expect(once(), requestTo(SECOND_PAGE_URL))
                .andRespond(withSuccess(secondResponseBody, MediaType.APPLICATION_JSON));

        // when
        FoodSafetyKoreaRecipeApiResponseDto result = crawler.crawlAll();

        // then
        assertThat(result.cookRecipeData().totalCount()).isEqualTo("1501");
        assertThat(result.cookRecipeData().rows())
                .extracting(FoodSafetyKoreaRecipeApiResponseDto.RecipeRow::menuName)
                .containsExactly("중복 메뉴", "신규 메뉴");
        mockServer.verify();
    }

    @Test
    @DisplayName("전체 건수가 숫자가 아니면 잘못된 외부 응답 예외가 발생한다")
    void 전체_건수_형식_오류_실패_잘못된_외부_응답() {
        // given
        String responseBody = """
                {
                  "COOKRCP01": {
                    "total_count": "invalid",
                    "row": [],
                    "RESULT": {"CODE": "INFO-000", "MSG": "정상 처리되었습니다."}
                  }
                }
                """;
        mockServer.expect(once(), requestTo(FIRST_PAGE_URL))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> crawler.crawlAll())
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MenuCrawlingErrorCode.INVALID_EXTERNAL_API_RESPONSE);
        mockServer.verify();
    }

    @Test
    @DisplayName("단일 응답에서 메뉴명이 동일한 데이터는 첫 번째 데이터만 유지한다")
    void 단일_응답_동일_메뉴명_중복_제거_성공() {
        // given
        String responseBody = """
                {
                  "COOKRCP01": {
                    "total_count": "2",
                    "row": [
                      {"RCP_NM": "중복 메뉴", "RCP_PARTS_DTLS": "첫 번째 재료"},
                      {"RCP_NM": "중복 메뉴", "RCP_PARTS_DTLS": "두 번째 재료"}
                    ],
                    "RESULT": {"CODE": "INFO-000", "MSG": "정상 처리되었습니다."}
                  }
                }
                """;
        mockServer.expect(once(), requestTo(TWO_ROWS_URL))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        // when
        FoodSafetyKoreaRecipeApiResponseDto result = crawler.crawl(1, 2);

        // then
        assertThat(result.cookRecipeData().rows()).singleElement()
                .satisfies(row -> {
                    assertThat(row.menuName()).isEqualTo("중복 메뉴");
                    assertThat(row.ingredientDetails()).isEqualTo("첫 번째 재료");
                });
        mockServer.verify();
    }

    @Test
    @DisplayName("메뉴명은 정규화하지 않고 원본 문자열로 중복을 판단한다")
    void 메뉴명_원본_문자열_중복_판단_성공() {
        // given
        String responseBody = """
                {
                  "COOKRCP01": {
                    "total_count": "2",
                    "row": [
                      {"RCP_NM": "원본 메뉴"},
                      {"RCP_NM": " 원본 메뉴 "}
                    ],
                    "RESULT": {"CODE": "INFO-000", "MSG": "정상 처리되었습니다."}
                  }
                }
                """;
        mockServer.expect(once(), requestTo(TWO_ROWS_URL))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        // when
        FoodSafetyKoreaRecipeApiResponseDto result = crawler.crawl(1, 2);

        // then
        assertThat(result.cookRecipeData().rows())
                .extracting(FoodSafetyKoreaRecipeApiResponseDto.RecipeRow::menuName)
                .containsExactly("원본 메뉴", " 원본 메뉴 ");
        mockServer.verify();
    }
}
