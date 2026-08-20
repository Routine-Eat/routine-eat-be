package com.likelion.routineeatbe.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.likelion.routineeatbe.domain.menu.crawling.MenuAndRecipeCrawler;
import com.likelion.routineeatbe.domain.menu.dto.MenuAndRecipeMetaDataDto;
import com.likelion.routineeatbe.domain.menu.dto.response.FoodSafetyKoreaRecipeApiResponseDto;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.menu.exception.MenuCrawlingErrorCode;
import com.likelion.routineeatbe.domain.menu.service.gemini.MenuAndRecipeGeminiService;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuAndRecipeCrawlingServiceTest {

    @InjectMocks
    private MenuAndRecipeCrawlingService menuAndRecipeCrawlingService;

    @Mock
    private MenuAndRecipeCrawler menuAndRecipeCrawler;

    @Mock
    private MenuAndRecipeGeminiService menuAndRecipeGeminiService;

    @Mock
    private MenuAndRecipePersistenceService menuAndRecipePersistenceService;

    @Test
    @DisplayName("조회 범위를 입력하면 크롤링, Gemini 메타데이터 생성, 저장을 순서대로 수행한다")
    void 조회_범위_입력_크롤링_Gemini_저장_성공() {
        // given
        FoodSafetyKoreaRecipeApiResponseDto response = createResponse("1", "범위 메뉴");
        given(menuAndRecipeCrawler.crawl(1, 10)).willReturn(response);
        given(menuAndRecipePersistenceService.findNewMenuDtos(anyList()))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(menuAndRecipeGeminiService.generateMetaData(anyList()))
                .willReturn(createMetaData("범위 메뉴"));

        // when
        menuAndRecipeCrawlingService.crawlAndSave(1, 10);

        // then
        InOrder inOrder = Mockito.inOrder(
                menuAndRecipeCrawler,
                menuAndRecipePersistenceService,
                menuAndRecipeGeminiService
        );
        inOrder.verify(menuAndRecipeCrawler).crawl(1, 10);
        inOrder.verify(menuAndRecipePersistenceService).findNewMenuDtos(anyList());
        inOrder.verify(menuAndRecipeGeminiService).generateMetaData(anyList());
        inOrder.verify(menuAndRecipePersistenceService).saveAll(anyList(), anyMap());
    }

    @Test
    @DisplayName("조회 범위를 생략하면 전체 데이터를 크롤링하고 저장한다")
    void 조회_범위_생략_전체_데이터_저장_성공() {
        // given
        given(menuAndRecipeCrawler.crawlAll()).willReturn(createResponse("1501", "전체 조회 메뉴"));
        given(menuAndRecipePersistenceService.findNewMenuDtos(anyList()))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(menuAndRecipeGeminiService.generateMetaData(anyList()))
                .willReturn(createMetaData("전체 조회 메뉴"));

        // when
        menuAndRecipeCrawlingService.crawlAndSave(null, null);

        // then
        then(menuAndRecipeCrawler).should().crawlAll();
        then(menuAndRecipeGeminiService).should().generateMetaData(anyList());
        then(menuAndRecipePersistenceService).should().saveAll(anyList(), anyMap());
    }

    @Test
    @DisplayName("신규 메뉴가 없으면 Gemini 호출과 저장을 생략한다")
    void 신규_메뉴_없음_Gemini_저장_생략_성공() {
        // given
        given(menuAndRecipeCrawler.crawl(1, 1)).willReturn(createResponse("1", "기존 메뉴"));
        given(menuAndRecipePersistenceService.findNewMenuDtos(anyList())).willReturn(List.of());

        // when
        menuAndRecipeCrawlingService.crawlAndSave(1, 1);

        // then
        then(menuAndRecipeGeminiService).shouldHaveNoInteractions();
        then(menuAndRecipePersistenceService).should().findNewMenuDtos(anyList());
        then(menuAndRecipePersistenceService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Gemini 메타데이터 생성에 실패하면 저장하지 않는다")
    void Gemini_메타데이터_생성_실패_저장_생략() {
        // given
        given(menuAndRecipeCrawler.crawl(1, 1)).willReturn(createResponse("1", "신규 메뉴"));
        given(menuAndRecipePersistenceService.findNewMenuDtos(anyList()))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(menuAndRecipeGeminiService.generateMetaData(anyList()))
                .willThrow(new CustomException(GeminiErrorCode.API_CALL_FAILED));

        // when & then
        assertThatThrownBy(() -> menuAndRecipeCrawlingService.crawlAndSave(1, 1))
                .isInstanceOf(CustomException.class);
        then(menuAndRecipePersistenceService).should().findNewMenuDtos(anyList());
        then(menuAndRecipePersistenceService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("시작 위치만 입력하면 잘못된 조회 범위 예외가 발생한다")
    void 시작_위치만_입력_잘못된_조회_범위_실패() {
        // when & then
        assertThatThrownBy(() -> menuAndRecipeCrawlingService.crawlAndSave(1, null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MenuCrawlingErrorCode.INVALID_CRAWLING_RANGE);

        then(menuAndRecipeCrawler).shouldHaveNoInteractions();
        then(menuAndRecipeGeminiService).shouldHaveNoInteractions();
        then(menuAndRecipePersistenceService).shouldHaveNoInteractions();
    }

    private Map<String, MenuAndRecipeMetaDataDto> createMetaData(String menuName) {
        return Map.of(
                menuName,
                MenuAndRecipeMetaDataDto.create(
                        MenuType.KOREAN,
                        RecommendationType.DEFAULT,
                        30,
                        "https://example.com/main.jpg"
                )
        );
    }

    private FoodSafetyKoreaRecipeApiResponseDto createResponse(String totalCount, String menuName) {
        FoodSafetyKoreaRecipeApiResponseDto.RecipeRow row =
                FoodSafetyKoreaRecipeApiResponseDto.RecipeRow.builder()
                        .menuName(menuName)
                        .calories("100")
                        .ingredientDetails("테스트 재료")
                        .mainImageUrl("https://example.com/main.jpg")
                        .manual01("테스트 조리 단계")
                        .build();

        return FoodSafetyKoreaRecipeApiResponseDto.builder()
                .cookRecipeData(FoodSafetyKoreaRecipeApiResponseDto.CookRecipeData.builder()
                        .totalCount(totalCount)
                        .rows(List.of(row))
                        .build())
                .build();
    }
}
