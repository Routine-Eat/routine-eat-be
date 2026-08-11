package com.likelion.routineeatbe.domain.foodIngredient.service;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.foodIngredient.repository.FoodIngredientRepository;
import java.util.EnumMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodIngredientBulkSearchService {

    private final FoodIngredientRepository foodIngredientRepository;

    /**
     * (1) 작업 목적
     * 전체 음식 재료를 조회하여 FoodIngredientType별로 그룹화합니다.
     *
     * (2) 세부 작업 내용
     * - 타입과 식별자 순서로 음식 재료를 한 번에 조회합니다.
     * - Gemini 프롬프트에서 안정적인 순서로 사용할 수 있도록 EnumMap으로 그룹화합니다.
     *
     * @return 음식 재료 타입별 음식 재료 목록
     */
    @Transactional(readOnly = true)
    public Map<FoodIngredientType, List<FoodIngredient>> findAllGroupedByType() {
        log.info("[FoodIngredientBulkSearchService] 타입별 음식 재료 일괄 조회 시작 | findAllGroupedByType() - START");

        /*
            1. 모든 음식 재료들을 FoodIngredientType과 PK 기준 오름차순으로 조회한다.
            - 각 음식 재료 타입별 음식 재료 리스트를 저장하기 위한 EnumMap을 생성한다.
         */
        List<FoodIngredient> foodIngredients = foodIngredientRepository.findAllByOrderByTypeAscIdAsc();
        Map<FoodIngredientType, List<FoodIngredient>> result = new EnumMap<>(FoodIngredientType.class);

        /*
            2. 각 음식 재료 타입(FoodIngredientType)별로 List를 만들어 EnumMap에 저장한다.
            - key: FoodIngredientType
            - value: List<FoodIIngredient>
         */
        for (FoodIngredientType type : FoodIngredientType.values()) {
            List<FoodIngredient> groupedFoodIngredients = foodIngredients.stream()
                    .filter(foodIngredient -> foodIngredient.getType() == type)
                    .toList();
            if (!groupedFoodIngredients.isEmpty()) {
                result.put(type, groupedFoodIngredients);
            }
        }

        log.info(
                "[FoodIngredientBulkSearchService] 타입별 음식 재료 일괄 조회 종료 | findAllGroupedByType() - END | ingredientCount: {}, typeCount: {}",
                foodIngredients.size(),
                result.size()
        );
        return Collections.unmodifiableMap(result);
    }
}
