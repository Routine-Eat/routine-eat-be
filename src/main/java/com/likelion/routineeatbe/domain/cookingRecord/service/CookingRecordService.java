package com.likelion.routineeatbe.domain.cookingRecord.service;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingStartReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.exception.CookingRecordErrorCode;
import com.likelion.routineeatbe.domain.cookingRecord.mapper.CookingRecordMapper;
import com.likelion.routineeatbe.domain.cookingRecord.repository.CookingRecordRepository;
import com.likelion.routineeatbe.domain.cookingRecord.service.gemini.CookingStepGenerateGeminiService;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.entity.RecipeStep;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeStepRepository;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.repository.RecipeFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CookingRecordService {

    private static final Set<CookingSessionStatus> BLOCKING_STATUSES =
            EnumSet.of(CookingSessionStatus.IN_PROGRESS, CookingSessionStatus.COMPLETED);

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final RecipeFoodIngredientRepository recipeFoodIngredientRepository;
    private final CookingRecordRepository cookingRecordRepository;
    private final CookingStepGenerateGeminiService geminiService;
    private final CookingRecordPersistenceService persistenceService;
    private final CookingRecordMapper cookingRecordMapper;

    /**
     * (1) 작업 목적
     * 사용자와 레시피 정보를 바탕으로 맞춤 요리 단계를 생성하고 요리를 시작합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자, 레시피, 재료와 기존 단계를 조회하고 중복 요리 시작 여부를 확인합니다.
     * - Gemini로 체크리스트와 요리 단계를 생성한 후 하나의 트랜잭션으로 저장합니다.
     * - 저장 결과와 생성 데이터를 요리 시작 응답 DTO로 변환합니다.
     *
     * @param userNumber 사용자 고유 식별번호
     * @param request 레시피 PK와 요청 인분 수
     * @return 요리 시작 결과
     */
    public CookingStartResDto startCooking(
            String userNumber,
            CookingStartReqDto request
    ) {
        log.info(
                "[CookingRecordService] 요리 시작 시작 | startCooking() - START | userNumber: {}, recipeId: {}, servings: {}",
                userNumber,
                request.recipeId(),
                request.servings()
        );

        User user = userRepository.findByLoginNumber(userNumber)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));
        Recipe recipe = recipeRepository.findByIdWithMenu(request.recipeId())
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.RECIPE_NOT_FOUND));
        if (cookingRecordRepository.existsBlockingSession(
                user.getId(),
                recipe.getId(),
                BLOCKING_STATUSES
        )) {
            throw new CustomException(CookingRecordErrorCode.COOKING_ALREADY_STARTED);
        }

        List<RecipeFoodIngredient> ingredients = recipeFoodIngredientRepository
                .findAllByRecipeIdInWithFoodIngredient(List.of(recipe.getId()));
        if (ingredients.isEmpty()) {
            throw new CustomException(CookingRecordErrorCode.RECIPE_FOOD_INGREDIENT_EMPTY);
        }
        List<RecipeStep> recipeSteps = recipeStepRepository
                .findAllByRecipeIdOrderByLevelAsc(recipe.getId());
        if (recipeSteps.isEmpty()) {
            throw new CustomException(CookingRecordErrorCode.RECIPE_STEP_EMPTY);
        }

        CookingStepGenerateGeminiResponseDto generated = geminiService.generate(
                user,
                recipe,
                ingredients,
                recipeSteps,
                request.servings()
        );
        CookingRecord cookingRecord = persistenceService.save(
                user.getId(),
                recipe.getId(),
                request.servings(),
                generated
        );
        CookingStartResDto result = cookingRecordMapper.toCookingStartResDto(
                cookingRecord,
                recipe,
                generated
        );

        log.info(
                "[CookingRecordService] 요리 시작 종료 | startCooking() - END | cookingRecordId: {}",
                result.cookingRecordId()
        );
        return result;
    }
}
