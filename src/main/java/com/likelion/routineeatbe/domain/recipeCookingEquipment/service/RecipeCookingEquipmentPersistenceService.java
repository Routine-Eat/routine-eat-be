package com.likelion.routineeatbe.domain.recipeCookingEquipment.service;

import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import com.likelion.routineeatbe.domain.cookingEquipment.repository.CookingEquipmentRepository;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipeCookingEquipment.entity.RecipeCookingEquipment;
import com.likelion.routineeatbe.domain.recipeCookingEquipment.exception.RecipeCookingEquipmentErrorCode;
import com.likelion.routineeatbe.domain.recipeCookingEquipment.repository.RecipeCookingEquipmentRepository;
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
public class RecipeCookingEquipmentPersistenceService {

    private final RecipeRepository recipeRepository;
    private final CookingEquipmentRepository cookingEquipmentRepository;
    private final RecipeCookingEquipmentRepository recipeCookingEquipmentRepository;

    /**
     * (1) 작업 목적
     * Gemini가 생성한 레시피별 조리 도구 식별자를 RecipeCookingEquipment Entity로 저장합니다.
     *
     * (2) 세부 작업 내용
     * - 레시피와 조리 도구를 식별자로 일괄 조회합니다.
     * - Gemini 결과의 참조 무결성을 검증합니다.
     * - 모든 레시피 조리 도구 연결 데이터를 하나의 트랜잭션으로 저장합니다.
     *
     * @param cookingEquipmentIdsByRecipeId 레시피 ID별 조리 도구 식별자 목록
     * @return 저장된 RecipeCookingEquipment 개수
     */
    @Transactional
    public long saveAll(Map<Long, List<Long>> cookingEquipmentIdsByRecipeId) {
        log.info(
                "[RecipeCookingEquipmentPersistenceService] 레시피 조리 도구 일괄 저장 시작 | saveAll() - START | recipeCount: {}",
                cookingEquipmentIdsByRecipeId.size()
        );

        if (cookingEquipmentIdsByRecipeId.isEmpty()) {
            log.info(
                    "[RecipeCookingEquipmentPersistenceService] 레시피 조리 도구 일괄 저장 종료 | saveAll() - END | savedCount: 0"
            );
            return 0L;
        }

        Map<Long, Recipe> recipesById = findRecipesById(cookingEquipmentIdsByRecipeId.keySet());
        Set<Long> cookingEquipmentIds = cookingEquipmentIdsByRecipeId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        Map<Long, CookingEquipment> cookingEquipmentsById = findCookingEquipmentsById(
                cookingEquipmentIds
        );

        List<RecipeCookingEquipment> recipeCookingEquipments = new ArrayList<>();
        cookingEquipmentIdsByRecipeId.forEach((recipeId, equipmentIds) -> {
            Recipe recipe = recipesById.get(recipeId);
            equipmentIds.forEach(cookingEquipmentId -> recipeCookingEquipments.add(
                    RecipeCookingEquipment.create(
                            recipe,
                            cookingEquipmentsById.get(cookingEquipmentId)
                    )
            ));
        });

        try {
            recipeCookingEquipmentRepository.saveAllAndFlush(recipeCookingEquipments);
        } catch (DataIntegrityViolationException exception) {
            throw new CustomException(RecipeCookingEquipmentErrorCode.ALREADY_INITIALIZED);
        }

        long result = recipeCookingEquipments.size();
        log.info(
                "[RecipeCookingEquipmentPersistenceService] 레시피 조리 도구 일괄 저장 종료 | saveAll() - END | savedCount: {}",
                result
        );
        return result;
    }

    /**
     * 저장 대상 레시피를 식별자로 일괄 조회하고 누락 여부를 검증합니다.
     *
     * @param recipeIds 조회할 레시피 식별자 집합
     * @return 레시피 식별자별 Recipe Entity
     */
    private Map<Long, Recipe> findRecipesById(Set<Long> recipeIds) {
        log.debug(
                "[RecipeCookingEquipmentPersistenceService] 저장 대상 레시피 조회 시작 | findRecipesById() - START | recipeCount: {}",
                recipeIds.size()
        );

        Map<Long, Recipe> result = recipeRepository.findAllById(recipeIds).stream()
                .collect(Collectors.toMap(Recipe::getId, Function.identity()));
        if (result.size() != recipeIds.size()) {
            throw new CustomException(RecipeCookingEquipmentErrorCode.RECIPE_NOT_FOUND);
        }

        log.debug(
                "[RecipeCookingEquipmentPersistenceService] 저장 대상 레시피 조회 종료 | findRecipesById() - END | resultSize: {}",
                result.size()
        );
        return result;
    }

    /**
     * 저장 대상 조리 도구를 식별자로 일괄 조회하고 누락 여부를 검증합니다.
     *
     * @param cookingEquipmentIds 조회할 조리 도구 식별자 집합
     * @return 조리 도구 식별자별 CookingEquipment Entity
     */
    private Map<Long, CookingEquipment> findCookingEquipmentsById(
            Set<Long> cookingEquipmentIds
    ) {
        log.debug(
                "[RecipeCookingEquipmentPersistenceService] 저장 대상 조리 도구 조회 시작 | findCookingEquipmentsById() - START | equipmentCount: {}",
                cookingEquipmentIds.size()
        );

        Map<Long, CookingEquipment> result = cookingEquipmentRepository
                .findAllById(cookingEquipmentIds)
                .stream()
                .collect(Collectors.toMap(CookingEquipment::getId, Function.identity()));
        if (result.size() != cookingEquipmentIds.size()) {
            throw new CustomException(
                    RecipeCookingEquipmentErrorCode.COOKING_EQUIPMENT_NOT_FOUND
            );
        }

        log.debug(
                "[RecipeCookingEquipmentPersistenceService] 저장 대상 조리 도구 조회 종료 | findCookingEquipmentsById() - END | resultSize: {}",
                result.size()
        );
        return result;
    }
}
