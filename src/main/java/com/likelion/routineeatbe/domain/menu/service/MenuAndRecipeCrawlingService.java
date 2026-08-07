package com.likelion.routineeatbe.domain.menu.service;

import com.likelion.routineeatbe.domain.menu.crawling.MenuAndRecipeCrawler;
import com.likelion.routineeatbe.domain.menu.dto.MenuAndRecipeMetaDataDto;
import com.likelion.routineeatbe.domain.menu.dto.response.FoodSafetyKoreaRecipeApiResponseDto;
import com.likelion.routineeatbe.domain.menu.dto.response.MenuAndRecipeCrawlingDto;
import com.likelion.routineeatbe.domain.menu.exception.MenuCrawlingErrorCode;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuAndRecipeCrawlingService {

    private final MenuAndRecipeCrawler menuAndRecipeCrawler;
    private final MenuAndRecipeGeminiService menuAndRecipeGeminiService;
    private final MenuAndRecipePersistenceService menuAndRecipePersistenceService;

    /**
     * (1) 작업 목적
     * 식품안전청 조리식품 API 데이터를 조회하여 메뉴, 레시피, 조리 단계로 저장합니다.
     *
     * (2) 세부 작업 내용
     * - 조회 범위가 없으면 Crawler를 통해 전체 데이터를 조회합니다.
     * - 조회 범위가 있으면 지정 범위를 한 번 호출합니다.
     * - 외부 API 호출이 모두 완료된 후 저장 트랜잭션을 실행합니다.
     *
     * @param startIdx 조회 시작 위치, 전체 조회 시 null
     * @param endIdx 조회 종료 위치, 전체 조회 시 null
     */
    public void crawlAndSave(Integer startIdx, Integer endIdx) {
        log.info(
                "[MenuAndRecipeCrawlingService] 메뉴 및 레시피 크롤링 저장 시작 | crawlAndSave() - START | startIdx: {}, endIdx: {}",
                startIdx,
                endIdx
        );

        /*
            1. 선택 파라미터 검증
            - 시작 위치와 종료 위치는 모두 입력하거나 모두 생략해야 합니다.
         */
        validateOptionalRange(startIdx, endIdx);

        /*
            2. 식품안전청 데이터 조회
            - 범위 입력 여부에 따라 지정 범위 또는 전체 데이터를 조회합니다.
         */
        List<MenuAndRecipeCrawlingDto> crawlingDtos = Objects.isNull(startIdx)
                ? crawlAll()
                : crawlRange(startIdx, endIdx);

        List<MenuAndRecipeCrawlingDto> newMenuDtos =
                menuAndRecipePersistenceService.findNewMenuDtos(crawlingDtos);

        if (newMenuDtos.isEmpty()) {
            log.info(
                    "[MenuAndRecipeCrawlingService] 신규 메뉴 없음 | crawlAndSave() - END | savedMenuCount: 0"
            );
            return;
        }

        /*
            3. Gemini로 각 메뉴별 메타데이터를 생성한다.
            - 각 메뉴별 메타 데이터는 MenuAndRecipeCrawlingDto 내부 데이터들(menuName, List<RecipeRow>)로 결정된다.
            - 메타 데이터:
                1. 메뉴 종류(Menu.MenuType)
                2. 요리 소요 시간 (Menu.timeRequired)
                3. 메뉴 추천 타입 (Menu.RecommendationType)
         */
        Map<String, MenuAndRecipeMetaDataDto> metaDataByMenuName =
                menuAndRecipeGeminiService.generateMetaData(newMenuDtos);
        /*
            4. 메뉴 및 레시피 저장
            - 모든 외부 API 호출이 성공한 경우에만 저장 트랜잭션을 실행합니다.
         */
        menuAndRecipePersistenceService.saveAll(newMenuDtos, metaDataByMenuName);

        log.info(
                "[MenuAndRecipeCrawlingService] 메뉴 및 레시피 크롤링 저장 종료 | crawlAndSave() - END | savedMenuCount: {}",
                newMenuDtos.size()
        );
    }

    /**
     * 시작 위치와 종료 위치의 선택 입력 조합을 검증합니다.
     *
     * @param startIdx 조회 시작 위치
     * @param endIdx 조회 종료 위치
     */
    private void validateOptionalRange(Integer startIdx, Integer endIdx) {
        log.debug(
                "[MenuAndRecipeCrawlingService] 선택 조회 범위 검증 시작 | validateOptionalRange() - START | startIdx: {}, endIdx: {}",
                startIdx,
                endIdx
        );

        if (Objects.isNull(startIdx) != Objects.isNull(endIdx)) {
            throw new CustomException(MenuCrawlingErrorCode.INVALID_CRAWLING_RANGE);
        }

        log.debug("[MenuAndRecipeCrawlingService] 선택 조회 범위 검증 종료 | validateOptionalRange() - END");
    }

    /**
     * 지정한 단일 범위의 조리식품 데이터를 조회하고 저장용 DTO로 변환합니다.
     *
     * @param startIdx 조회 시작 위치
     * @param endIdx 조회 종료 위치
     * @return 저장 대상 메뉴 DTO 목록
     */
    private List<MenuAndRecipeCrawlingDto> crawlRange(int startIdx, int endIdx) {
        log.debug(
                "[MenuAndRecipeCrawlingService] 지정 범위 조회 시작 | crawlRange() - START | startIdx: {}, endIdx: {}",
                startIdx,
                endIdx
        );

        FoodSafetyKoreaRecipeApiResponseDto response = menuAndRecipeCrawler.crawl(startIdx, endIdx);
        List<MenuAndRecipeCrawlingDto> result =
                MenuAndRecipeCrawlingDto.fromFoodSafetyKoreaRecipeApiResponseDtoToList(response);

        log.debug(
                "[MenuAndRecipeCrawlingService] 지정 범위 조회 종료 | crawlRange() - END | resultSize: {}",
                result.size()
        );
        return result;
    }

    /**
     * Crawler에서 조회한 전체 조리식품 응답을 저장용 DTO로 변환합니다.
     *
     * @return 전체 저장 대상 메뉴 DTO 목록
     */
    private List<MenuAndRecipeCrawlingDto> crawlAll() {
        log.debug("[MenuAndRecipeCrawlingService] 전체 데이터 조회 시작 | crawlAll() - START");

        FoodSafetyKoreaRecipeApiResponseDto response = menuAndRecipeCrawler.crawlAll();
        List<MenuAndRecipeCrawlingDto> result =
                MenuAndRecipeCrawlingDto.fromFoodSafetyKoreaRecipeApiResponseDtoToList(response);

        log.debug(
                "[MenuAndRecipeCrawlingService] 전체 데이터 조회 종료 | crawlAll() - END | resultSize: {}",
                result.size()
        );
        return result;
    }
}
