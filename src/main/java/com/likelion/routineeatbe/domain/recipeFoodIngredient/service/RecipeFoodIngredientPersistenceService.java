package com.likelion.routineeatbe.domain.recipeFoodIngredient.service;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.repository.FoodIngredientRepository;
import com.likelion.routineeatbe.domain.menu.dto.gemini.InitMenuAndRecipeFoodIngredientGeminiResponseDto.FoodIngredientNeedAmount;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.repository.MenuRepository;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.exception.RecipeFoodIngredientErrorCode;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.repository.RecipeFoodIngredientRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeFoodIngredientPersistenceService {

    private final MenuRepository menuRepository;
    private final FoodIngredientRepository foodIngredientRepository;
    private final RecipeFoodIngredientRepository recipeFoodIngredientRepository;

    /**
     * (1) 작업 목적
     * Gemini가 생성한 메뉴별 음식 재료 필요량을 RecipeFoodIngredient Entity로 저장합니다.
     *
     * (2) 세부 작업 내용
     * - 메뉴와 음식 재료를 식별자로 일괄 조회합니다.
     * - 조회 결과와 Gemini 응답의 참조 무결성을 검증합니다.
     * - 모든 RecipeFoodIngredient를 하나의 트랜잭션으로 저장합니다.
     *
     * @param needAmountsByMenuId 메뉴 ID별 음식 재료 필요량 목록
     * @return 저장된 RecipeFoodIngredient 개수
     */
    @Transactional
    public long saveAll(Map<Long, List<FoodIngredientNeedAmount>> needAmountsByMenuId) {
        log.info(
                "[RecipeFoodIngredientPersistenceService] 메뉴 음식 재료 일괄 저장 시작 | saveAll() - START | menuCount: {}",
                needAmountsByMenuId.size()
        );

        if (needAmountsByMenuId.isEmpty()) {
            log.info(
                    "[RecipeFoodIngredientPersistenceService] 메뉴 음식 재료 일괄 저장 종료 | saveAll() - END | savedCount: 0"
            );
            return 0L;
        }

        Map<Long, Menu> menusById = findMenusById(needAmountsByMenuId.keySet());
        Set<Long> foodIngredientIds = needAmountsByMenuId.values().stream()
                .flatMap(List::stream)
                .map(FoodIngredientNeedAmount::foodIngredientId)
                .collect(Collectors.toSet());
        Map<Long, FoodIngredient> foodIngredientsById = findFoodIngredientsById(foodIngredientIds);

        List<RecipeFoodIngredient> recipeFoodIngredients = new ArrayList<>();
        needAmountsByMenuId.forEach((menuId, needAmounts) -> {
            Menu menu = menusById.get(menuId);
            needAmounts.forEach(needAmount -> recipeFoodIngredients.add(
                    RecipeFoodIngredient.create(
                            menu,
                            foodIngredientsById.get(needAmount.foodIngredientId()),
                            needAmount.primaryNeedAmountValue(),
                            needAmount.secondaryNeedAmountValue()
                    )
            ));
        });

        try {
            recipeFoodIngredientRepository.saveAllAndFlush(recipeFoodIngredients);
        } catch (DataIntegrityViolationException exception) {
            throw new CustomException(RecipeFoodIngredientErrorCode.ALREADY_INITIALIZED);
        }

        long result = recipeFoodIngredients.size();
        log.info(
                "[RecipeFoodIngredientPersistenceService] 메뉴 음식 재료 일괄 저장 종료 | saveAll() - END | savedCount: {}",
                result
        );
        return result;
    }

    /**
     * 저장 대상 메뉴를 식별자로 일괄 조회하고 누락 여부를 검증합니다.
     *
     * @param menuIds 조회할 메뉴 식별자 집합
     * @return 메뉴 식별자별 Menu Entity
     */
    private Map<Long, Menu> findMenusById(Set<Long> menuIds) {
        log.debug(
                "[RecipeFoodIngredientPersistenceService] 저장 대상 메뉴 조회 시작 | findMenusById() - START | menuCount: {}",
                menuIds.size()
        );

        Map<Long, Menu> result = menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));
        if (result.size() != menuIds.size()) {
            throw new CustomException(RecipeFoodIngredientErrorCode.MENU_NOT_FOUND);
        }

        log.debug(
                "[RecipeFoodIngredientPersistenceService] 저장 대상 메뉴 조회 종료 | findMenusById() - END | resultSize: {}",
                result.size()
        );
        return result;
    }

    /**
     * 저장 대상 음식 재료를 식별자로 일괄 조회하고 누락 여부를 검증합니다.
     *
     * @param foodIngredientIds 조회할 음식 재료 식별자 집합
     * @return 음식 재료 식별자별 FoodIngredient Entity
     */
    private Map<Long, FoodIngredient> findFoodIngredientsById(Set<Long> foodIngredientIds) {
        log.debug(
                "[RecipeFoodIngredientPersistenceService] 저장 대상 음식 재료 조회 시작 | findFoodIngredientsById() - START | ingredientCount: {}",
                foodIngredientIds.size()
        );

        Map<Long, FoodIngredient> result = foodIngredientRepository.findAllById(foodIngredientIds).stream()
                .collect(Collectors.toMap(FoodIngredient::getId, Function.identity()));
        if (result.size() != foodIngredientIds.size()) {
            throw new CustomException(RecipeFoodIngredientErrorCode.FOOD_INGREDIENT_NOT_FOUND);
        }

        log.debug(
                "[RecipeFoodIngredientPersistenceService] 저장 대상 음식 재료 조회 종료 | findFoodIngredientsById() - END | resultSize: {}",
                result.size()
        );
        return result;
    }
}
