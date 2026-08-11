package com.likelion.routineeatbe.domain.menu.service;

import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import com.likelion.routineeatbe.domain.cookingEquipment.service.CookingEquipmentBulkSearchService;
import com.likelion.routineeatbe.domain.menu.dto.response.InitMenuAndRecipeCookingEquipmentResDto;
import com.likelion.routineeatbe.domain.menu.service.gemini.InitMenuAndRecipeCookingEquipmentGeminiService;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipeCookingEquipment.exception.RecipeCookingEquipmentErrorCode;
import com.likelion.routineeatbe.domain.recipeCookingEquipment.service.RecipeCookingEquipmentPersistenceService;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitMenuAndRecipeCookingEquipmentService {

    private final RecipeRepository recipeRepository;
    private final CookingEquipmentBulkSearchService cookingEquipmentBulkSearchService;
    private final InitMenuAndRecipeCookingEquipmentGeminiService geminiService;
    private final RecipeCookingEquipmentPersistenceService persistenceService;

    /**
     * (1) 작업 목적
     * DB에 저장된 기본 레시피별 필요 조리 도구 연결 데이터를 초기화합니다.
     *
     * (2) 세부 작업 내용
     * - 아직 조리 도구가 없는 기본 레시피를 조회합니다.
     * - 전체 조리 도구 기준 데이터를 Gemini 입력으로 제공합니다.
     * - 모든 Gemini 분석이 완료된 후 하나의 저장 트랜잭션으로 반영합니다.
     *
     * @return 초기화된 레시피 조리 도구 연결 개수를 포함한 응답 DTO
     */
    public InitMenuAndRecipeCookingEquipmentResDto initialize() {
        log.info(
                "[InitMenuAndRecipeCookingEquipmentService] 레시피 조리 도구 초기화 시작 | initialize() - START"
        );

        List<Recipe> recipes = recipeRepository.findAllForCookingEquipmentInitialization();
        if (recipes.isEmpty()) {
            InitMenuAndRecipeCookingEquipmentResDto result =
                    InitMenuAndRecipeCookingEquipmentResDto.create(0L);
            log.info(
                    "[InitMenuAndRecipeCookingEquipmentService] 레시피 조리 도구 초기화 종료 | initialize() - END | initCount: 0"
            );
            return result;
        }

        List<CookingEquipment> cookingEquipments = cookingEquipmentBulkSearchService.findAll();
        if (cookingEquipments.isEmpty()) {
            throw new CustomException(
                    RecipeCookingEquipmentErrorCode.COOKING_EQUIPMENT_DATA_EMPTY
            );
        }

        Map<Long, List<Long>> cookingEquipmentIdsByRecipeId =
                geminiService.generateCookingEquipmentIds(recipes, cookingEquipments);
        long initCount = persistenceService.saveAll(cookingEquipmentIdsByRecipeId);
        InitMenuAndRecipeCookingEquipmentResDto result =
                InitMenuAndRecipeCookingEquipmentResDto.create(initCount);

        log.info(
                "[InitMenuAndRecipeCookingEquipmentService] 레시피 조리 도구 초기화 종료 | initialize() - END | initCount: {}",
                initCount
        );
        return result;
    }
}
