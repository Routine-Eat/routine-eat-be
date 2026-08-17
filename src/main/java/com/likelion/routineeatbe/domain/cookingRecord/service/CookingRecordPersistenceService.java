package com.likelion.routineeatbe.domain.cookingRecord.service;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto.GeneratedCookingStep;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.ModifiedCookingRecordFoodIngredientReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecordFoodIngredient;
import com.likelion.routineeatbe.domain.cookingRecord.enums.TasteRating;
import com.likelion.routineeatbe.domain.cookingRecord.exception.CookingRecordErrorCode;
import com.likelion.routineeatbe.domain.cookingRecord.repository.CookingRecordRepository;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import com.likelion.routineeatbe.domain.cookingTip.entity.CookingStepTip;
import com.likelion.routineeatbe.domain.cookingTip.entity.CookingTip;
import com.likelion.routineeatbe.domain.cookingTip.repository.CookingTipRepository;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.repository.RecipeFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import com.likelion.routineeatbe.domain.user.repository.UserFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CookingRecordPersistenceService {

    private static final Set<CookingSessionStatus> BLOCKING_STATUSES =
            EnumSet.of(CookingSessionStatus.IN_PROGRESS, CookingSessionStatus.COMPLETED);
    private static final String CHECK_LIST_TITLE = "요리 시작 전 체크리스트";

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeFoodIngredientRepository recipeFoodIngredientRepository;
    private final CookingRecordRepository cookingRecordRepository;
    private final UserFoodIngredientRepository userFoodIngredientRepository;
    private final CookingTipRepository cookingTipRepository;

    /**
     * (1) 작업 목적
     * 완료된 사용자 소유 요리 기록에 맛 평가, 난이도와 선택 이미지 URL을 저장합니다.
     *
     * (2) 세부 작업 내용
     * - 요리 기록을 비관적 쓰기 잠금으로 조회해 동시 회고 저장을 직렬화합니다.
     * - 연결된 세션이 완료 상태인지 재검증한 뒤 요리 기록을 변경합니다.
     * - 요청된 음식 재료 사용량을 갱신하고 최종 사용량만큼 사용자 보유 재료를 차감합니다.
     * - 수정 목록이 비어 있으면 요리 시작 시 초기화된 사용량을 그대로 사용합니다.
     * - 저장 완료 후 요리 세션을 종료 상태로 변경해 중복 차감을 방지합니다.
     *
     * @param userId 사용자 PK
     * @param cookingRecordId 요리 기록 PK
     * @param tasteRating 맛 평가
     * @param difficultyLevel 실제 요리 난이도
     * @param cookingTip 사용자가 작성한 요리 팁
     * @param modifiedFoodIngredients 실제 사용량을 수정할 요리 기록 음식 재료 목록
     * @param photoUrl 선택 이미지의 CloudFront URL
     * @return 요리 결과가 저장된 요리 기록
     */
    @Transactional
    public CookingRecord saveCookingResult(
            Long userId,
            Long cookingRecordId,
            TasteRating tasteRating,
            DifficultyLevel difficultyLevel,
            String cookingTip,
            List<ModifiedCookingRecordFoodIngredientReqDto> modifiedFoodIngredients,
            String photoUrl
    ) {
        log.info(
                "[CookingRecordPersistenceService] 요리 결과 저장 시작 | saveCookingResult() - START | userId: {}, cookingRecordId: {}",
                userId,
                cookingRecordId
        );

        CookingRecord cookingRecord = cookingRecordRepository
                .findByIdAndUserIdForUpdate(cookingRecordId, userId)
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_RECORD_NOT_FOUND
                ));
        CookingSession cookingSession = cookingRecord.getCookingSession();
        if (cookingSession == null) {
            throw new CustomException(CookingRecordErrorCode.COOKING_SESSION_NOT_FOUND);
        }
        if (cookingSession.getStatus() != CookingSessionStatus.COMPLETED) {
            throw new CustomException(CookingRecordErrorCode.COOKING_SESSION_NOT_COMPLETED);
        }

        List<CookingRecordFoodIngredient> usedFoodIngredients =
                cookingRecord.getFoodIngredients();
        updateUsedFoodIngredientAmounts(usedFoodIngredients, modifiedFoodIngredients);
        cookingRecord.saveCookingResult(tasteRating, difficultyLevel, cookingTip, photoUrl);
        if (!usedFoodIngredients.isEmpty()) {
            Map<Long, Double> remainingPrimaryAmounts = usedFoodIngredients.stream()
                    .collect(Collectors.toMap(
                            usedFoodIngredient -> usedFoodIngredient
                                    .getFoodIngredient()
                                    .getId(),
                            CookingRecordFoodIngredient::getPrimaryUsedAmountValue,
                            Double::sum
                    ));
            Map<Long, Double> remainingSecondaryAmounts = usedFoodIngredients.stream()
                    .filter(usedFoodIngredient ->
                            usedFoodIngredient.getSecondaryUsedAmountValue() != null)
                    .collect(Collectors.toMap(
                            usedFoodIngredient -> usedFoodIngredient
                                    .getFoodIngredient()
                                    .getId(),
                            CookingRecordFoodIngredient::getSecondaryUsedAmountValue,
                            Double::sum
                    ));
            List<UserFoodIngredient> ownedFoodIngredients = userFoodIngredientRepository
                    .findAllForUpdateByUserIdAndRelationTypeAndFoodIngredientIds(
                            userId,
                            UserFoodIngredientType.OWN,
                            remainingPrimaryAmounts.keySet().stream().sorted().toList()
                    );
            for (UserFoodIngredient ownedFoodIngredient : ownedFoodIngredients) {
                Long foodIngredientId = ownedFoodIngredient.getFoodIngredient().getId();
                remainingPrimaryAmounts.computeIfPresent(
                        foodIngredientId,
                        (id, amount) -> ownedFoodIngredient
                                .deductPrimaryAmountValue(amount)
                );
                remainingSecondaryAmounts.computeIfPresent(
                        foodIngredientId,
                        (id, amount) -> ownedFoodIngredient
                                .deductSecondaryAmountValue(amount)
                );
            }
        }
        cookingSession.terminate();
        log.info(
                "[CookingRecordPersistenceService] 요리 결과 저장 종료 | saveCookingResult() - END | cookingRecordId: {}, status: {}",
                cookingRecord.getId(),
                cookingSession.getStatus()
        );
        return cookingRecord;
    }

    /**
     * 요청된 요리 기록 음식 재료의 실제 사용량을 갱신합니다.
     * 수정 목록이 없으면 요리 시작 시 초기화된 사용량을 유지합니다.
     *
     * @param usedFoodIngredients 요리 시작 시 초기화된 요리 기록 음식 재료 목록
     * @param modifiedFoodIngredients 실제 사용량 수정 요청 목록
     */
    private void updateUsedFoodIngredientAmounts(
            List<CookingRecordFoodIngredient> usedFoodIngredients,
            List<ModifiedCookingRecordFoodIngredientReqDto> modifiedFoodIngredients
    ) {
        log.debug(
                "[CookingRecordPersistenceService] 음식 재료 실제 사용량 수정 시작 | updateUsedFoodIngredientAmounts() - START | modifiedCount: {}",
                modifiedFoodIngredients == null ? 0 : modifiedFoodIngredients.size()
        );
        if (modifiedFoodIngredients == null || modifiedFoodIngredients.isEmpty()) {
            log.debug(
                    "[CookingRecordPersistenceService] 음식 재료 실제 사용량 수정 종료 | updateUsedFoodIngredientAmounts() - END | modifiedCount: 0"
            );
            return;
        }

        Set<Long> requestedIds = new HashSet<>();
        for (ModifiedCookingRecordFoodIngredientReqDto modifiedFoodIngredient
                : modifiedFoodIngredients) {
            if (!requestedIds.add(
                    modifiedFoodIngredient.cookingRecordFoodIngredientId()
            )) {
                throw new CustomException(
                        CookingRecordErrorCode.DUPLICATE_COOKING_RECORD_FOOD_INGREDIENT
                );
            }
        }
        Map<Long, CookingRecordFoodIngredient> usedFoodIngredientsById =
                usedFoodIngredients.stream()
                        .collect(Collectors.toMap(
                                CookingRecordFoodIngredient::getId,
                                Function.identity()
                        ));
        if (!usedFoodIngredientsById.keySet().containsAll(requestedIds)) {
            throw new CustomException(
                    CookingRecordErrorCode.COOKING_RECORD_FOOD_INGREDIENT_NOT_FOUND
            );
        }

        modifiedFoodIngredients.forEach(modifiedFoodIngredient ->
                usedFoodIngredientsById
                        .get(modifiedFoodIngredient.cookingRecordFoodIngredientId())
                        .updateUsedAmountValues(
                                modifiedFoodIngredient.usedPrimaryAmountValue(),
                                modifiedFoodIngredient.usedSecondaryAmountValue()
                        ));
        log.debug(
                "[CookingRecordPersistenceService] 음식 재료 실제 사용량 수정 종료 | updateUsedFoodIngredientAmounts() - END | modifiedCount: {}",
                modifiedFoodIngredients.size()
        );
    }

    /**
     * (1) 작업 목적
     * 검증된 Gemini 결과를 요리 기록, 세션, 체크리스트와 요리 단계로 원자적으로 저장합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 행을 잠가 동일 사용자의 동시 요리 시작 요청을 직렬화합니다.
     * - 같은 사용자와 레시피에 진행 중 또는 완료된 세션이 있으면 저장을 차단합니다.
     * - 레시피의 1인분 음식 재료 필요량에 요청 인분 수를 곱해 사용량으로 저장합니다.
     * - 체크리스트는 level 0, 실제 요리 단계는 level 1 이상으로 저장합니다.
     * - Gemini가 선택한 요리 팁을 단계별 CookingStepTip으로 연결합니다.
     *
     * @param userId 사용자 PK
     * @param recipeId 레시피 PK
     * @param servings 요청 인분 수
     * @param generated Gemini가 생성한 체크리스트와 요리 단계
     * @return 저장된 요리 기록
     */
    @Transactional
    public CookingRecord save(
            Long userId,
            Long recipeId,
            Integer servings,
            CookingStepGenerateGeminiResponseDto generated
    ) {
        log.info(
                "[CookingRecordPersistenceService] 요리 시작 데이터 저장 시작 | save() - START | userId: {}, recipeId: {}",
                userId,
                recipeId
        );

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.RECIPE_NOT_FOUND));
        if (cookingRecordRepository.existsBlockingSession(userId, recipeId, BLOCKING_STATUSES)) {
            throw new CustomException(CookingRecordErrorCode.COOKING_ALREADY_STARTED);
        }
        List<RecipeFoodIngredient> recipeFoodIngredients = recipeFoodIngredientRepository
                .findAllByRecipeIdInWithFoodIngredient(List.of(recipeId));
        if (recipeFoodIngredients.isEmpty()) {
            throw new CustomException(CookingRecordErrorCode.RECIPE_FOOD_INGREDIENT_EMPTY);
        }

        Set<Long> cookingTipIds = generated.cookingSteps().stream()
                .flatMap(cookingStep -> cookingStep.cookingTipIds().stream())
                .collect(Collectors.toSet());
        Map<Long, CookingTip> cookingTipMap = cookingTipRepository.findAllById(cookingTipIds)
                .stream()
                .collect(Collectors.toMap(CookingTip::getId, Function.identity()));
        if (cookingTipMap.size() != cookingTipIds.size()) {
            throw new CustomException(CookingRecordErrorCode.COOKING_TIP_NOT_FOUND);
        }

        CookingRecord cookingRecord = CookingRecord.create(user, recipe, servings);
        for (RecipeFoodIngredient recipeFoodIngredient : recipeFoodIngredients) {
            Double secondaryUsedAmountValue =
                    recipeFoodIngredient.getSecondaryNeedAmountValue() == null
                            ? null
                            : recipeFoodIngredient.getSecondaryNeedAmountValue() * servings;
            CookingRecordFoodIngredient.create(
                    cookingRecord,
                    recipeFoodIngredient.getFoodIngredient(),
                    recipeFoodIngredient.getPrimaryNeedAmountValue() * servings,
                    secondaryUsedAmountValue
            );
        }
        CookingSession cookingSession = CookingSession.create(
                cookingRecord,
                generated.cookingSteps().size()
        );
        generated.checkListBeforeStart().forEach(content -> CookingStep.create(
                cookingSession,
                0L,
                CHECK_LIST_TITLE,
                content,
                null
        ));
        for (GeneratedCookingStep generatedCookingStep : generated.cookingSteps()) {
            CookingStep cookingStep = CookingStep.create(
                    cookingSession,
                    generatedCookingStep.level().longValue(),
                    generatedCookingStep.title(),
                    generatedCookingStep.content(),
                    generatedCookingStep.subContent()
            );
            generatedCookingStep.cookingTipIds().forEach(cookingTipId ->
                    CookingStepTip.create(cookingStep, cookingTipMap.get(cookingTipId))
            );
        }

        CookingRecord result = cookingRecordRepository.saveAndFlush(cookingRecord);
        log.info(
                "[CookingRecordPersistenceService] 요리 시작 데이터 저장 종료 | save() - END | cookingRecordId: {}, foodIngredientCount: {}",
                result.getId(),
                result.getFoodIngredients().size()
        );
        return result;
    }
}
