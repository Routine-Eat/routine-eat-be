package com.likelion.routineeatbe.domain.cookingRecord.service;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingStartReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.exception.CookingRecordErrorCode;
import com.likelion.routineeatbe.domain.cookingRecord.mapper.CookingRecordMapper;
import com.likelion.routineeatbe.domain.cookingRecord.repository.CookingRecordRepository;
import com.likelion.routineeatbe.domain.cookingRecord.service.gemini.CookingStepGenerateGeminiService;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.cookingSession.repository.CookingStepRepository;
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
import org.springframework.transaction.annotation.Transactional;

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
    private final CookingStepRepository cookingStepRepository;
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
     * - 저장된 1번 단계의 상세 정보와 생성 데이터를 요리 시작 응답 DTO로 변환합니다.
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
        CookingSession cookingSession = cookingRecord.getCookingSession();
        CookingStep firstCookingStep = cookingStepRepository
                .findByCookingSessionIdAndLevel(cookingSession.getId(), 1L)
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_STEP_NOT_FOUND
                ));
        CookingStartResDto result = cookingRecordMapper.toCookingStartResDto(
                cookingRecord,
                recipe,
                generated,
                firstCookingStep
        );

        log.info(
                "[CookingRecordService] 요리 시작 종료 | startCooking() - END | cookingRecordId: {}",
                result.cookingRecordId()
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * 사용자의 요리 세션을 다음 요리 단계로 이동하거나 마지막 단계에서 완료 처리합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 소유 요리 기록과 세션을 비관적 쓰기 잠금으로 조회합니다.
     * - 진행 중 세션의 현재 단계가 마지막이면 완료 상태로 변경합니다.
     * - 마지막 단계가 아니면 현재 단계를 증가시키고 다음 단계 상세 정보를 반환합니다.
     *
     * @param cookingRecordId 요리 기록 PK
     * @param userNumber 사용자 고유 식별번호
     * @return 이동한 요리 단계 정보, 요리 완료 시 null
     */
    @Transactional
    public CookingStepNavigationResDto moveToNextCookingStep(
            Long cookingRecordId,
            String userNumber
    ) {
        log.info(
                "[CookingRecordService] 다음 요리 단계 이동 시작 | moveToNextCookingStep() - START | cookingRecordId: {}, userNumber: {}",
                cookingRecordId,
                userNumber
        );

        User user = userRepository.findByLoginNumber(userNumber)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));
        CookingRecord cookingRecord = cookingRecordRepository
                .findByIdAndUserIdForUpdate(cookingRecordId, user.getId())
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_RECORD_NOT_FOUND
                ));
        CookingSession cookingSession = cookingRecord.getCookingSession();
        if (cookingSession == null) {
            throw new CustomException(CookingRecordErrorCode.COOKING_SESSION_NOT_FOUND);
        }
        if (cookingSession.getStatus() != CookingSessionStatus.IN_PROGRESS) {
            throw new CustomException(
                    CookingRecordErrorCode.COOKING_SESSION_NOT_IN_PROGRESS
            );
        }
        validateCookingStepState(cookingSession);

        if (cookingSession.isLastStep()) {
            cookingSession.complete();
            log.info(
                    "[CookingRecordService] 다음 요리 단계 이동 종료 | moveToNextCookingStep() - END | cookingRecordId: {}, status: {}",
                    cookingRecordId,
                    cookingSession.getStatus()
            );
            return null;
        }

        cookingSession.moveToNextStep();
        CookingStep cookingStep = cookingStepRepository.findByCookingSessionIdAndLevel(
                        cookingSession.getId(),
                        cookingSession.getCurrentCookingStepLevel().longValue()
                )
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_STEP_NOT_FOUND
                ));
        CookingStepNavigationResDto result = cookingRecordMapper.toCookingStepNavigationResDto(
                cookingSession,
                cookingStep
        );

        log.info(
                "[CookingRecordService] 다음 요리 단계 이동 종료 | moveToNextCookingStep() - END | cookingRecordId: {}, currentLevel: {}, nextLevel: {}",
                cookingRecordId,
                result.currentCookingStep().level(),
                result.nextCookingStepLevel()
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * 사용자의 요리 세션을 이전 요리 단계로 이동합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 소유 요리 기록과 세션을 비관적 쓰기 잠금으로 조회합니다.
     * - 진행 중 세션의 현재 단계가 1이면 변경하지 않고 null을 반환합니다.
     * - 현재 단계가 2 이상이면 단계를 감소시키고 이전 단계 상세 정보를 반환합니다.
     *
     * @param cookingRecordId 요리 기록 PK
     * @param userNumber 사용자 고유 식별번호
     * @return 이동한 요리 단계 정보, 현재 단계가 1이면 null
     */
    @Transactional
    public CookingStepNavigationResDto moveToPreviousCookingStep(
            Long cookingRecordId,
            String userNumber
    ) {
        log.info(
                "[CookingRecordService] 이전 요리 단계 이동 시작 | moveToPreviousCookingStep() - START | cookingRecordId: {}, userNumber: {}",
                cookingRecordId,
                userNumber
        );

        User user = userRepository.findByLoginNumber(userNumber)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));
        CookingRecord cookingRecord = cookingRecordRepository
                .findByIdAndUserIdForUpdate(cookingRecordId, user.getId())
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_RECORD_NOT_FOUND
                ));
        CookingSession cookingSession = cookingRecord.getCookingSession();
        if (cookingSession == null) {
            throw new CustomException(CookingRecordErrorCode.COOKING_SESSION_NOT_FOUND);
        }
        if (cookingSession.getStatus() != CookingSessionStatus.IN_PROGRESS) {
            throw new CustomException(
                    CookingRecordErrorCode.COOKING_SESSION_NOT_IN_PROGRESS
            );
        }
        validateCookingStepState(cookingSession);

        if (cookingSession.isFirstStep()) {
            log.info(
                    "[CookingRecordService] 이전 요리 단계 이동 종료 | moveToPreviousCookingStep() - END | cookingRecordId: {}, currentLevel: 1",
                    cookingRecordId
            );
            return null;
        }

        cookingSession.moveToPreviousStep();
        CookingStep cookingStep = cookingStepRepository.findByCookingSessionIdAndLevel(
                        cookingSession.getId(),
                        cookingSession.getCurrentCookingStepLevel().longValue()
                )
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_STEP_NOT_FOUND
                ));
        CookingStepNavigationResDto result =
                cookingRecordMapper.toCookingStepNavigationResDto(
                        cookingSession,
                        cookingStep
                );

        log.info(
                "[CookingRecordService] 이전 요리 단계 이동 종료 | moveToPreviousCookingStep() - END | cookingRecordId: {}, currentLevel: {}, prevLevel: {}",
                cookingRecordId,
                result.currentCookingStep().level(),
                result.prevCookingStepLevel()
        );
        return result;
    }

    /**
     * 요리 세션의 전체 단계 수와 현재 단계 번호가 이동 가능한 범위인지 검증합니다.
     *
     * @param cookingSession 검증할 요리 세션
     */
    private void validateCookingStepState(CookingSession cookingSession) {
        log.debug(
                "[CookingRecordService] 요리 단계 상태 검증 시작 | validateCookingStepState() - START | cookingSessionId: {}",
                cookingSession.getId()
        );
        if (cookingSession.getCookingStepCount() == null
                || cookingSession.getCookingStepCount() < 1
                || cookingSession.getCurrentCookingStepLevel() == null
                || cookingSession.getCurrentCookingStepLevel() < 1
                || cookingSession.getCurrentCookingStepLevel()
                        > cookingSession.getCookingStepCount()) {
            throw new CustomException(CookingRecordErrorCode.INVALID_COOKING_STEP_STATE);
        }
        log.debug(
                "[CookingRecordService] 요리 단계 상태 검증 종료 | validateCookingStepState() - END | currentLevel: {}, cookingStepCount: {}",
                cookingSession.getCurrentCookingStepLevel(),
                cookingSession.getCookingStepCount()
        );
    }
}
