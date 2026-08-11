package com.likelion.routineeatbe.domain.menu.service;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.foodIngredient.service.FoodIngredientBulkSearchService;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeFoodIngredientGeminiResponseDto.FoodIngredientNeedAmount;
import com.likelion.routineeatbe.domain.menu.dto.response.InitMenuAndRecipeFoodIngredientResDto;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.repository.MenuRepository;
import com.likelion.routineeatbe.domain.menu.service.gemini.InitMenuAndRecipeFoodIngredientGeminiService;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.exception.RecipeFoodIngredientErrorCode;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.service.RecipeFoodIngredientPersistenceService;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitMenuAndRecipeFoodIngredientService {

    private final MenuRepository menuRepository;
    private final FoodIngredientBulkSearchService foodIngredientBulkSearchService;
    private final InitMenuAndRecipeFoodIngredientGeminiService geminiService;
    private final RecipeFoodIngredientPersistenceService persistenceService;

    /**
     * (1) 작업 목적
     * DB에 저장된 메뉴별 1인분 음식 재료 필요량 데이터를 초기화합니다.
     *
     * (2) 세부 작업 내용
     * - 아직 음식 재료 필요량이 없는 메뉴를 조회합니다.
     * - 전체 음식 재료를 타입별로 조회하여 Gemini 입력으로 제공합니다.
     * - Gemini 응답 전체가 생성된 후 하나의 저장 트랜잭션으로 반영합니다.
     *
     * @return 초기화된 메뉴 음식 재료 개수를 포함한 응답 DTO
     */
    public InitMenuAndRecipeFoodIngredientResDto initialize() {
        log.info(
                "[InitMenuAndRecipeFoodIngredientService] 메뉴 음식 재료 초기화 시작 | initialize() - START"
        );

        /*
            1. 아직 메뉴/레시피별로 필요한 음식이 저장되어있지 않은 Menu Entity를 List로 조회한다.
            - 만약 존재하지 않는다면 이미 메뉴/레시피별로 필요한 음식이 전부 저장되어있는 상태이기에 바로 함수를 종료한다.
         */
        List<Menu> menus = menuRepository.findAllForFoodIngredientInitialization();
        if (menus.isEmpty()) {
            InitMenuAndRecipeFoodIngredientResDto result =
                    InitMenuAndRecipeFoodIngredientResDto.create(0L);
            log.info(
                    "[InitMenuAndRecipeFoodIngredientService] 메뉴 음식 재료 초기화 종료 | initialize() - END | initCount: 0"
            );
            return result;
        }

        /*
            2. FoodIngredient 테이블에서 FoodIngredientType 타입별로 어떤 재료들이 있는지 Map 형식으로 조회한다.
            - key: FoodIngredientType
            - value: List<FoodIngredient>
            - 만약 응답값이 비어있다면 예외를 발생시킨다
         */
        Map<FoodIngredientType, List<FoodIngredient>> foodIngredientsByType =
                foodIngredientBulkSearchService.findAllGroupedByType();
        if (foodIngredientsByType.isEmpty()) {
            throw new CustomException(RecipeFoodIngredientErrorCode.FOOD_INGREDIENT_DATA_EMPTY);
        }

        /*
            3. Map<FoodIngredientType, List<FoodIngredient>> 형태로 각 음식 재료 타입별 음식 리스트를 추출했다면,
            아직 음식 재료 데이터가 저장되지 않은 List<Menu>와 Map<FoodIngredientType, List<FoodIngredient>>를 아용해
            Gemini에게 "각 메뉴별로 필요한 음식 재료 데이터를 생성해라" 하고 요청한다.
         */
        Map<Long, List<FoodIngredientNeedAmount>> needAmountsByMenuId =
                geminiService.generateNeedAmounts(menus, foodIngredientsByType);

        /*
            4. Gemini가 생성한 각 메뉴별 필요한 음식 재료 데이터들을 UserRecipeIngredient에 저장한다.
            이후 응답 데이터를 반환한다.
            - initCount: 초기화한 음식 재료 데이터 개수
        */
        long initCount = persistenceService.saveAll(needAmountsByMenuId);
        InitMenuAndRecipeFoodIngredientResDto result =
                InitMenuAndRecipeFoodIngredientResDto.create(initCount);

        log.info(
                "[InitMenuAndRecipeFoodIngredientService] 메뉴 음식 재료 초기화 종료 | initialize() - END | initCount: {}",
                initCount
        );
        return result;
    }
}
