package com.likelion.routineeatbe.domain.cookingRecord.mapper;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordFoodIngredientAmountResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordFoodIngredientsResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingResultSaveResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepDetailResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepTitleResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecordFoodIngredient;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CookingRecordMapper {

    /**
     * 사용자 보유량과 요리 시작 시 저장된 사용량을 차감 전후 예상량으로 변환합니다.
     *
     * @param cookingRecord 사용 음식 재료가 초기화된 요리 기록
     * @param recipeFoodIngredients 요리 기록의 레시피 음식 재료 목록
     * @param ownedFoodIngredients 사용자가 현재 보유한 음식 재료 목록
     * @return 요리 전후 사용자 보유 예상량 응답
     */
    public CookingRecordFoodIngredientsResDto toCookingRecordFoodIngredientsResDto(
            CookingRecord cookingRecord,
            List<RecipeFoodIngredient> recipeFoodIngredients,
            List<UserFoodIngredient> ownedFoodIngredients
    ) {
        Map<Long, CookingRecordFoodIngredient> cookingRecordFoodIngredientsByFoodIngredientId =
                cookingRecord.getFoodIngredients().stream()
                        .collect(Collectors.toMap(
                                foodIngredient -> foodIngredient.getFoodIngredient().getId(),
                                Function.identity()
                        ));
        Map<Long, List<UserFoodIngredient>> ownedFoodIngredientsByFoodIngredientId =
                ownedFoodIngredients.stream()
                        .collect(Collectors.groupingBy(
                                foodIngredient -> foodIngredient.getFoodIngredient().getId()
                        ));
        List<CookingRecordFoodIngredientAmountResDto> foodIngredients =
                recipeFoodIngredients.stream()
                        .map(recipeFoodIngredient -> {
                            Long foodIngredientId = recipeFoodIngredient
                                    .getFoodIngredient()
                                    .getId();
                            CookingRecordFoodIngredient cookingRecordFoodIngredient =
                                    cookingRecordFoodIngredientsByFoodIngredientId.get(
                                            foodIngredientId
                                    );
                            List<UserFoodIngredient> ownedAmounts =
                                    ownedFoodIngredientsByFoodIngredientId.getOrDefault(
                                            foodIngredientId,
                                            List.of()
                                    );
                            double prevPrimaryAmountValue = ownedAmounts.stream()
                                    .mapToDouble(foodIngredient ->
                                            foodIngredient.getPrimaryAmountValue() == null
                                                    ? 0.0
                                                    : foodIngredient.getPrimaryAmountValue())
                                    .sum();
                            double currentPrimaryAmountValue = Math.max(
                                    prevPrimaryAmountValue
                                                    - cookingRecordFoodIngredient
                                                    .getPrimaryUsedAmountValue(),
                                    0.0
                            );
                            boolean hasSecondaryAmount =
                                    cookingRecordFoodIngredient
                                            .getSecondaryUsedAmountValue() != null;
                            Double prevSecondaryAmountValue = hasSecondaryAmount
                                    ? ownedAmounts.stream()
                                            .mapToDouble(foodIngredient ->
                                                    foodIngredient.getSecondaryAmountValue() == null
                                                            ? 0.0
                                                            : foodIngredient
                                                                    .getSecondaryAmountValue())
                                            .sum()
                                    : null;
                            Double currentSecondaryAmountValue = hasSecondaryAmount
                                    ? Math.max(
                                            prevSecondaryAmountValue
                                                    - cookingRecordFoodIngredient
                                                            .getSecondaryUsedAmountValue(),
                                            0.0
                                    )
                                    : null;
                            return CookingRecordFoodIngredientAmountResDto.create(
                                    recipeFoodIngredient.getId(),
                                    recipeFoodIngredient.getFoodIngredient().getName(),
                                    prevPrimaryAmountValue,
                                    currentPrimaryAmountValue,
                                    recipeFoodIngredient.getFoodIngredient().getPrimaryUnit(),
                                    prevSecondaryAmountValue,
                                    currentSecondaryAmountValue,
                                    hasSecondaryAmount
                                            ? recipeFoodIngredient.getFoodIngredient()
                                                    .getSecondaryUnit()
                                            : null
                            );
                        })
                        .toList();
        return CookingRecordFoodIngredientsResDto.create(foodIngredients);
    }

    /**
     * 요리 결과가 저장된 CookingRecord Entity를 응답 DTO로 변환합니다.
     *
     * @param cookingRecord 요리 결과가 저장된 요리 기록
     * @return 저장된 요리 기록 PK 응답
     */
    public CookingResultSaveResDto toCookingResultSaveResDto(CookingRecord cookingRecord) {
        return CookingResultSaveResDto.builder()
                .savedCookingRecordId(cookingRecord.getId())
                .build();
    }

    /**
     * 저장된 요리 기록, Gemini 생성 결과와 첫 요리 단계를 요리 시작 응답 DTO로 변환합니다.
     *
     * @param cookingRecord 저장된 요리 기록
     * @param recipe 요리를 시작한 레시피
     * @param generated Gemini가 생성한 체크리스트와 요리 단계
     * @param firstCookingStep 저장된 첫 번째 요리 단계
     * @return 요리 시작 응답 DTO
     */
    public CookingStartResDto toCookingStartResDto(
            CookingRecord cookingRecord,
            Recipe recipe,
            CookingStepGenerateGeminiResponseDto generated,
            CookingStep firstCookingStep
    ) {
        CookingSession cookingSession = cookingRecord.getCookingSession();
        Integer currentLevel = cookingSession.getCurrentCookingStepLevel();
        Integer nextLevel = currentLevel < cookingSession.getCookingStepCount()
                ? currentLevel + 1
                : null;
        List<CookingStepTitleResDto> cookingStepTitles = generated.cookingSteps().stream()
                .map(cookingStep -> CookingStepTitleResDto.builder()
                        .stepLevel(cookingStep.level().longValue())
                        .stepTitle(cookingStep.title())
                        .build())
                .toList();
        return CookingStartResDto.builder()
                .cookingRecordId(cookingRecord.getId())
                .recipeName(recipe.getMenu().getName())
                .recipeThumbnailUrl(recipe.getMenu().getThumbnailUrl())
                .recipeTimeRequired(recipe.getMenu().getTimeRequired())
                .checkListBeforeStart(List.copyOf(generated.checkListBeforeStart()))
                .cookingStepCount(cookingSession.getCookingStepCount())
                .prevCookingStepLevel(currentLevel - 1)
                .nextCookingStepLevel(nextLevel)
                .currentCookingStep(toCookingStepDetailResDto(firstCookingStep))
                .cookingStepTitles(cookingStepTitles)
                .build();
    }

    /**
     * 요리 세션과 현재 요리 단계를 단계 이동 응답 DTO로 변환합니다.
     *
     * @param cookingSession 단계 이동이 완료된 요리 세션
     * @param cookingStep 현재 요리 단계
     * @return 단계 이동 응답 DTO
     */
    public CookingStepNavigationResDto toCookingStepNavigationResDto(
            CookingSession cookingSession,
            CookingStep cookingStep
    ) {
        Integer currentLevel = cookingSession.getCurrentCookingStepLevel();
        Integer nextLevel = currentLevel < cookingSession.getCookingStepCount()
                ? currentLevel + 1
                : null;
        return CookingStepNavigationResDto.builder()
                .cookingStepCount(cookingSession.getCookingStepCount())
                .prevCookingStepLevel(currentLevel - 1)
                .nextCookingStepLevel(nextLevel)
                .currentCookingStep(toCookingStepDetailResDto(cookingStep))
                .build();
    }

    /**
     * CookingStep Entity를 빈 팁 목록을 포함한 현재 요리 단계 상세 DTO로 변환합니다.
     *
     * @param cookingStep 변환할 요리 단계
     * @return 현재 요리 단계 상세 DTO
     */
    public CookingStepDetailResDto toCookingStepDetailResDto(CookingStep cookingStep) {
        return CookingStepDetailResDto.builder()
                .cookingStepId(cookingStep.getId())
                .level(cookingStep.getLevel())
                .title(cookingStep.getTitle())
                .thumbnailUrl(cookingStep.getThumbnailUrl())
                .content(cookingStep.getContent())
                .subContent(cookingStep.getSubContent())
                .stepTips(List.of())
                .build();
    }
}
