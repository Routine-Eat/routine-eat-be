package com.likelion.routineeatbe.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.likelion.routineeatbe.domain.menu.dto.MenuAndRecipeMetaDataBatchDto;
import com.likelion.routineeatbe.domain.menu.dto.MenuAndRecipeMetaDataBatchDto.MenuMetaData;
import com.likelion.routineeatbe.domain.menu.dto.MenuAndRecipeMetaDataDto;
import com.likelion.routineeatbe.domain.menu.dto.response.MenuAndRecipeCrawlingDto;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.menu.service.gemini.MenuAndRecipeGeminiService;
import com.likelion.routineeatbe.global.config.GeminiProperties;
import com.likelion.routineeatbe.domain.menu.dto.gemini.MenuAndRecipeGeminiFunctionDeclarationDto;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import com.likelion.routineeatbe.global.util.GeminiRetryDelayStrategy;
import com.likelion.routineeatbe.global.util.GeminiUtil;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuAndRecipeGeminiServiceTest {

    @Mock
    private GeminiUtil geminiUtil;

    @Mock
    private GeminiRetryDelayStrategy retryDelayStrategy;

    private MenuAndRecipeGeminiService menuAndRecipeGeminiService;

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
                new GeminiProperties.Retry(
                        4,
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(8),
                        0.25
                )
        );
        menuAndRecipeGeminiService = new MenuAndRecipeGeminiService(
                geminiUtil,
                retryDelayStrategy,
                properties
        );
    }

    @Test
    @DisplayName("메뉴 10개 이하는 하나의 Gemini 배치로 메타데이터를 생성한다")
    void 메뉴_10개_이하_단일_Gemini_배치_생성_성공() {
        // given
        List<MenuAndRecipeCrawlingDto> crawlingDtos = List.of(
                createCrawlingDto("마파두부"),
                createCrawlingDto("비빔밥")
        );
        given(geminiUtil.callFunction(
                eq("menu-model"),
                anyString(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        )).willReturn(createBatchResponse(2));

        // when
        Map<String, MenuAndRecipeMetaDataDto> result =
                menuAndRecipeGeminiService.generateMetaData(crawlingDtos);

        // then
        assertThat(result.keySet()).containsExactly("마파두부", "비빔밥");
        assertThat(result.get("마파두부").thumbnailUrl())
                .isEqualTo("https://example.com/main.jpg");
        then(geminiUtil).should().callFunction(
                eq("menu-model"),
                anyString(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        );

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        then(geminiUtil).should().callFunction(
                eq("menu-model"),
                promptCaptor.capture(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        );
        assertThat(promptCaptor.getValue()).contains("마파두부", "비빔밥", "100.0 kcal", "두부, 소스");
        assertThat(promptCaptor.getValue()).contains(
                "1분 이상 1440분 이하",
                "24시간을 초과하는 숙성, 발효, 저장, 보관 등의 장기 대기시간은 제외"
        );
        assertThat(promptCaptor.getValue()).doesNotContain("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("Gemini 응답은 순번으로 연결하고 결과에는 원본 메뉴명을 유지한다")
    void Gemini_응답_순번_연결_원본_메뉴명_유지_성공() {
        // given
        MenuAndRecipeCrawlingDto crawlingDto = createCrawlingDto("새콤한연어샐러드");
        given(geminiUtil.callFunction(
                eq("menu-model"),
                anyString(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        )).willReturn(createBatchResponse(1));

        // when
        Map<String, MenuAndRecipeMetaDataDto> result =
                menuAndRecipeGeminiService.generateMetaData(List.of(crawlingDto));

        // then
        assertThat(result).containsOnlyKeys("새콤한연어샐러드");
    }

    @Test
    @DisplayName("메뉴 11개는 10개와 1개 배치로 나누어 호출한다")
    void 메뉴_11개_두_개_Gemini_배치_생성_성공() {
        // given
        List<MenuAndRecipeCrawlingDto> crawlingDtos = IntStream.rangeClosed(1, 11)
                .mapToObj(index -> createCrawlingDto("메뉴" + index))
                .toList();
        given(geminiUtil.callFunction(
                eq("menu-model"),
                anyString(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        )).willReturn(
                createBatchResponse(10),
                createBatchResponse(1)
        );

        // when
        Map<String, MenuAndRecipeMetaDataDto> result =
                menuAndRecipeGeminiService.generateMetaData(crawlingDtos);

        // then
        assertThat(result.keySet()).containsExactlyElementsOf(
                crawlingDtos.stream().map(MenuAndRecipeCrawlingDto::menuName).toList()
        );
        then(geminiUtil).should(times(2)).callFunction(
                eq("menu-model"),
                anyString(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        );
    }

    @Test
    @DisplayName("빈 메뉴 목록이면 Gemini를 호출하지 않는다")
    void 빈_메뉴_목록_Gemini_호출_생략_성공() {
        // when
        Map<String, MenuAndRecipeMetaDataDto> result =
                menuAndRecipeGeminiService.generateMetaData(List.of());

        // then
        assertThat(result).isEmpty();
        then(geminiUtil).shouldHaveNoInteractions();
        then(retryDelayStrategy).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("429 발생 후 다음 호출이 성공하면 백오프 후 결과를 반환한다")
    void Gemini_429_발생_백오프_재시도_성공() {
        // given
        MenuAndRecipeCrawlingDto crawlingDto = createCrawlingDto("마파두부");
        given(geminiUtil.callFunction(
                eq("menu-model"),
                anyString(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        )).willThrow(new CustomException(GeminiErrorCode.RATE_LIMIT_EXCEEDED))
                .willReturn(createBatchResponse(1));

        // when
        Map<String, MenuAndRecipeMetaDataDto> result =
                menuAndRecipeGeminiService.generateMetaData(List.of(crawlingDto));

        // then
        assertThat(result).containsKey("마파두부");
        then(geminiUtil).should(times(2)).callFunction(
                eq("menu-model"),
                anyString(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        );
        then(retryDelayStrategy).should().waitBeforeRetry(1);
    }

    @Test
    @DisplayName("Gemini 배치 응답의 순번이 중복된 후 다음 호출이 정상이면 재시도하여 성공한다")
    void Gemini_배치_응답_순번_중복_백오프_재시도_성공() {
        // given
        List<MenuAndRecipeCrawlingDto> crawlingDtos = List.of(
                createCrawlingDto("마파두부"),
                createCrawlingDto("비빔밥")
        );
        MenuAndRecipeMetaDataBatchDto duplicatedSequenceResponse =
                MenuAndRecipeMetaDataBatchDto.create(List.of(
                        MenuMetaData.create(
                                1,
                                MenuType.KOREAN,
                                RecommendationType.DEFAULT,
                                30
                        ),
                        MenuMetaData.create(
                                1,
                                MenuType.KOREAN,
                                RecommendationType.DEFAULT,
                                20
                        )
                ));
        given(geminiUtil.callFunction(
                eq("menu-model"),
                anyString(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        )).willReturn(duplicatedSequenceResponse, createBatchResponse(2));

        // when
        Map<String, MenuAndRecipeMetaDataDto> result =
                menuAndRecipeGeminiService.generateMetaData(crawlingDtos);

        // then
        assertThat(result.keySet()).containsExactly("마파두부", "비빔밥");
        then(geminiUtil).should(times(2)).callFunction(
                eq("menu-model"),
                anyString(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        );
        then(retryDelayStrategy).should().waitBeforeRetry(1);
    }

    @Test
    @DisplayName("429가 최대 시도 횟수까지 발생하면 RATE_LIMIT_EXCEEDED를 반환한다")
    void Gemini_429_최대_시도_초과_실패() {
        // given
        given(geminiUtil.callFunction(
                eq("menu-model"),
                anyString(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        )).willThrow(new CustomException(GeminiErrorCode.RATE_LIMIT_EXCEEDED));

        // when & then
        assertThatThrownBy(() -> menuAndRecipeGeminiService.generateMetaData(
                List.of(createCrawlingDto("마파두부"))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(GeminiErrorCode.RATE_LIMIT_EXCEEDED);
        then(geminiUtil).should(times(4)).callFunction(
                eq("menu-model"),
                anyString(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        );
        then(retryDelayStrategy).should(times(1)).waitBeforeRetry(1);
        then(retryDelayStrategy).should(times(1)).waitBeforeRetry(2);
        then(retryDelayStrategy).should(times(1)).waitBeforeRetry(3);
    }

    @Test
    @DisplayName("429가 아닌 Gemini 오류는 재시도하지 않는다")
    void Gemini_일반_오류_재시도_없이_실패() {
        // given
        given(geminiUtil.callFunction(
                eq("menu-model"),
                anyString(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        )).willThrow(new CustomException(GeminiErrorCode.API_CALL_FAILED));

        // when & then
        assertThatThrownBy(() -> menuAndRecipeGeminiService.generateMetaData(
                List.of(createCrawlingDto("마파두부"))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(GeminiErrorCode.API_CALL_FAILED);
        then(geminiUtil).should(times(1)).callFunction(
                eq("menu-model"),
                anyString(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        );
        then(retryDelayStrategy).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Gemini 배치 응답의 순번이 범위를 벗어나면 실패한다")
    void Gemini_배치_응답_순번_범위_초과_실패() {
        // given
        given(geminiUtil.callFunction(
                eq("menu-model"),
                anyString(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        )).willReturn(MenuAndRecipeMetaDataBatchDto.create(List.of(
                MenuMetaData.create(
                        2,
                        MenuType.KOREAN,
                        RecommendationType.DEFAULT,
                        30
                )
        )));

        // when & then
        assertThatThrownBy(() -> menuAndRecipeGeminiService.generateMetaData(
                List.of(createCrawlingDto("마파두부"))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(GeminiErrorCode.INVALID_METADATA);
    }

    @Test
    @DisplayName("Gemini가 유효하지 않은 조리시간을 반환하면 실패한다")
    void Gemini_유효하지_않은_조리시간_실패() {
        // given
        given(geminiUtil.callFunction(
                eq("menu-model"),
                anyString(),
                any(MenuAndRecipeGeminiFunctionDeclarationDto.class),
                eq(MenuAndRecipeMetaDataBatchDto.class)
        )).willReturn(MenuAndRecipeMetaDataBatchDto.create(List.of(
                MenuMetaData.create(
                        1,
                        MenuType.CHINESE,
                        RecommendationType.DEFAULT,
                        0
                )
        )));

        // when & then
        assertThatThrownBy(() -> menuAndRecipeGeminiService.generateMetaData(
                List.of(createCrawlingDto("마파두부"))))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(GeminiErrorCode.INVALID_METADATA);
    }

    private MenuAndRecipeMetaDataBatchDto createBatchResponse(int menuCount) {
        List<MenuMetaData> menus = new ArrayList<>();
        for (int sequence = 1; sequence <= menuCount; sequence++) {
            menus.add(MenuMetaData.create(
                    sequence,
                    MenuType.KOREAN,
                    RecommendationType.DEFAULT,
                    30
            ));
        }
        return MenuAndRecipeMetaDataBatchDto.create(menus);
    }

    private MenuAndRecipeCrawlingDto createCrawlingDto(String menuName) {
        return MenuAndRecipeCrawlingDto.create(
                menuName,
                100.0,
                "두부, 소스",
                "https://example.com/main.jpg",
                List.of(
                        MenuAndRecipeCrawlingDto.RecipeRow.create(
                                "두부를 볶는다.",
                                "https://example.com/image.jpg"
                        ),
                        MenuAndRecipeCrawlingDto.RecipeRow.create("소스를 넣고 끓인다.", null)
                )
        );
    }
}
