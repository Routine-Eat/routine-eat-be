package com.likelion.routineeatbe.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.likelion.routineeatbe.domain.menu.crawling.MenuAndRecipeCrawler;
import com.likelion.routineeatbe.domain.menu.dto.response.FoodSafetyKoreaRecipeApiResponseDto;
import com.likelion.routineeatbe.domain.menu.exception.MenuCrawlingErrorCode;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuAndRecipeCrawlingServiceTest {

    @InjectMocks
    private MenuAndRecipeCrawlingService menuAndRecipeCrawlingService;

    @Mock
    private MenuAndRecipeCrawler menuAndRecipeCrawler;

    @Mock
    private MenuAndRecipePersistenceService menuAndRecipePersistenceService;

    @Test
    @DisplayName("조회 범위를 입력하면 지정 범위를 한 번 조회하여 저장한다")
    void 조회_범위_입력_지정_범위_저장_성공() {
        // given
        FoodSafetyKoreaRecipeApiResponseDto response = createResponse("1", "범위 메뉴");
        given(menuAndRecipeCrawler.crawl(1, 10)).willReturn(response);

        // when
        menuAndRecipeCrawlingService.crawlAndSave(1, 10);

        // then
        then(menuAndRecipeCrawler).should().crawl(1, 10);
        then(menuAndRecipePersistenceService).should().saveAll(anyList());
    }

    @Test
    @DisplayName("조회 범위를 생략하면 전체 건수까지 반복 조회하여 저장한다")
    void 조회_범위_생략_전체_데이터_저장_성공() {
        // given
        given(menuAndRecipeCrawler.crawlAll())
                .willReturn(createResponse("1501", "전체 조회 메뉴"));

        // when
        menuAndRecipeCrawlingService.crawlAndSave(null, null);

        // then
        then(menuAndRecipeCrawler).should().crawlAll();
        then(menuAndRecipePersistenceService).should().saveAll(anyList());
    }

    @Test
    @DisplayName("시작 위치만 입력하면 잘못된 조회 범위 예외가 발생한다")
    void 시작_위치만_입력_실패_잘못된_조회_범위() {
        // when & then
        assertThatThrownBy(() -> menuAndRecipeCrawlingService.crawlAndSave(1, null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MenuCrawlingErrorCode.INVALID_CRAWLING_RANGE);

        then(menuAndRecipeCrawler).shouldHaveNoInteractions();
        then(menuAndRecipePersistenceService).shouldHaveNoInteractions();
    }

    private FoodSafetyKoreaRecipeApiResponseDto createResponse(String totalCount, String menuName) {
        FoodSafetyKoreaRecipeApiResponseDto.RecipeRow row = FoodSafetyKoreaRecipeApiResponseDto.RecipeRow.builder()
                .menuName(menuName)
                .calories("100")
                .ingredientDetails("테스트 재료")
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
