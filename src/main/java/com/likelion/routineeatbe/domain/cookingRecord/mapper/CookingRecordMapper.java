package com.likelion.routineeatbe.domain.cookingRecord.mapper;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingAiContextDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingRecordSearchResult;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingCompleteResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordDetailResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordFoodIngredientAmountResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordFoodIngredientsResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordInProgressResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordListItemResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordListResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordStepTitlesResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingSessionLogItemResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingSessionLogListResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingResultSaveResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepDetailResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepFoodIngredientResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepTipResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepTitleResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CurrentCookingStepDetailResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CurrentCookingStepFoodIngredientResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CurrentCookingStepResDto;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecordFoodIngredient;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingStepFoodIngredient;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSessionLog;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.cookingTip.entity.CookingStepTip;
import com.likelion.routineeatbe.domain.cookingTip.entity.CookingTip;
import com.likelion.routineeatbe.domain.cookingTip.entity.CookingTipContent;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class CookingRecordMapper {

    /**
     * 진행 중인 CookingRecord Entity를 진행 중인 요리 세션 응답 DTO로 변환합니다.
     *
     * @param cookingRecord 진행 중인 사용자 소유 요리 기록
     * @return 진행 중인 요리 기록 PK가 포함된 응답
     */
    public CookingRecordInProgressResDto toCookingRecordInProgressResDto(
            CookingRecord cookingRecord
    ) {
        return CookingRecordInProgressResDto.create(cookingRecord.getId());
    }

    /**
     * 진행 중인 요리 세션 Entity를 전체 단계 제목 응답 DTO로 변환합니다.
     *
     * @param cookingSession 진행 중인 요리 세션
     * @param cookingSteps 요리 세션에 연결된 전체 요리 단계
     * @return 전체 단계 개수와 단계 제목 목록이 포함된 응답 DTO
     */
    public CookingRecordStepTitlesResDto toCookingRecordStepTitlesResDto(
            CookingSession cookingSession,
            List<CookingStep> cookingSteps
    ) {
        List<CookingStepTitleResDto> cookingStepTitles = cookingSteps.stream()
                .map(cookingStep -> CookingStepTitleResDto.create(
                        cookingStep.getLevel(),
                        cookingStep.getTitle()
                ))
                .toList();
        return CookingRecordStepTitlesResDto.create(
                cookingSession.getCookingStepCount(),
                cookingStepTitles
        );
    }

    /**
     * 완료된 CookingRecord Entity를 요리 완료 응답 DTO로 변환합니다.
     *
     * @param cookingRecord 완료 상태로 변경된 사용자 소유 요리 기록
     * @param cookedDate 요리 완료 날짜
     * @return 요리된 메뉴 이름과 완료 날짜가 포함된 응답
     */
    public CookingCompleteResDto toCookingCompleteResDto(
            CookingRecord cookingRecord,
            LocalDate cookedDate
    ) {
        return CookingCompleteResDto.create(
                cookingRecord.getRecipe().getMenu().getName(),
                cookedDate
        );
    }

    /**
     * 요리 기록과 단계, 재료 정보를 Gemini 요리 답변용 컨텍스트 DTO로 변환합니다.
     *
     * @param cookingRecord 사용자 소유 요리 기록
     * @param cookingSteps 요리 세션에 저장된 실제 요리 단계 목록
     * @param foodIngredients 레시피에 등록된 음식 재료 목록
     * @return Gemini 프롬프트 생성에 사용할 요리 컨텍스트
     */
    public CookingAiContextDto toCookingAiContextDto(
            CookingRecord cookingRecord,
            List<CookingStep> cookingSteps,
            List<RecipeFoodIngredient> foodIngredients
    ) {
        CookingSession cookingSession = cookingRecord.getCookingSession();
        List<CookingAiContextDto.CookingStepContext> stepContexts = cookingSteps.stream()
                .map(cookingStep -> new CookingAiContextDto.CookingStepContext(
                        cookingStep.getLevel(),
                        cookingStep.getTitle(),
                        cookingStep.getContent(),
                        cookingStep.getSubContent()
                ))
                .toList();
        List<CookingAiContextDto.FoodIngredientContext> ingredientContexts =
                foodIngredients.stream()
                        .map(foodIngredient -> {
                            Double secondaryAmount = foodIngredient
                                    .getSecondaryNeedAmountValue() == null
                                    ? null
                                    : foodIngredient.getSecondaryNeedAmountValue()
                                            * cookingRecord.getServings();
                            return new CookingAiContextDto.FoodIngredientContext(
                                    foodIngredient.getFoodIngredient().getName(),
                                    foodIngredient.getPrimaryNeedAmountValue()
                                            * cookingRecord.getServings(),
                                    foodIngredient.getFoodIngredient()
                                            .getPrimaryUnit()
                                            .getDescription(),
                                    secondaryAmount,
                                    secondaryAmount == null
                                            ? null
                                            : foodIngredient.getFoodIngredient()
                                                    .getSecondaryUnit()
                                                    .getDescription()
                            );
                        })
                        .toList();
        return new CookingAiContextDto(
                cookingRecord.getUser().getId(),
                cookingRecord.getId(),
                cookingSession.getId(),
                cookingRecord.getRecipe().getId(),
                cookingRecord.getServings(),
                cookingSession.getCurrentCookingStepLevel(),
                cookingSession.getCookingStepCount(),
                cookingRecord.getRecipe().getMenu().getName(),
                cookingRecord.getRecipe().getMenu().getType().name(),
                cookingRecord.getRecipe().getMenu().getDifficultyLevel().name(),
                cookingRecord.getRecipe().getMenu().getTimeRequired(),
                cookingRecord.getRecipe().getMenu().getCalory(),
                stepContexts,
                ingredientContexts
        );
    }

    /**
     * 요리 기록 조회 결과를 목록 항목 응답 DTO로 변환합니다.
     *
     * @param result 변환할 요리 기록 조회 결과
     * @return 요리 기록 목록 항목 응답
     */
    public CookingRecordListItemResDto toCookingRecordListItemResDto(
            CookingRecordSearchResult result
    ) {
        return CookingRecordListItemResDto.create(
                result.cookingRecordId(),
                result.recipeId(),
                result.menuName(),
                result.thumbnailUrl(),
                result.favoriteRecipe(),
                result.createdAt().toLocalDate(),
                result.userDifficultyLevel(),
                result.usedFoodIngredientCount()
        );
    }

    /**
     * 요리 기록 Slice와 다음 커서를 목록 응답 DTO로 변환합니다.
     *
     * @param slice 요리 기록 조회 결과 Slice
     * @param nextCursor 다음 조회에 사용할 위치 커서
     * @return 요리 기록 목록과 다음 커서 정보
     */
    public CookingRecordListResDto toCookingRecordListResDto(
            Slice<CookingRecordSearchResult> slice,
            Integer nextCursor
    ) {
        List<CookingRecordListItemResDto> content = slice.getContent().stream()
                .map(this::toCookingRecordListItemResDto)
                .toList();
        return CookingRecordListResDto.create(content, slice.hasNext(), nextCursor);
    }

    /**
     * CookingSessionLog Entity를 AI 대화 기록 항목 응답 DTO로 변환합니다.
     *
     * @param cookingSessionLog 변환할 요리 세션 로그
     * @return 요리 세션 로그 PK, 타입과 내용이 포함된 응답 항목
     */
    public CookingSessionLogItemResDto toCookingSessionLogItemResDto(
            CookingSessionLog cookingSessionLog
    ) {
        return CookingSessionLogItemResDto.create(
                cookingSessionLog.getId(),
                cookingSessionLog.getType(),
                cookingSessionLog.getContent()
        );
    }

    /**
     * 요리 세션 로그 Slice와 다음 커서를 AI 대화 기록 목록 응답 DTO로 변환합니다.
     *
     * @param slice 요리 세션 로그 조회 결과 Slice
     * @param nextCursor 다음 조회에 사용할 위치 커서
     * @return AI 대화 기록 목록과 다음 커서 정보
     */
    public CookingSessionLogListResDto toCookingSessionLogListResDto(
            Slice<CookingSessionLog> slice,
            Integer nextCursor
    ) {
        List<CookingSessionLogItemResDto> content = slice.getContent().stream()
                .map(this::toCookingSessionLogItemResDto)
                .toList();
        return CookingSessionLogListResDto.create(content, slice.hasNext(), nextCursor);
    }

    /**
     * CookingRecord Entity와 연결된 메뉴 정보를 요리 기록 상세 응답 DTO로 변환합니다.
     *
     * @param cookingRecord 상세 조회할 사용자 소유 요리 기록
     * @return 메뉴 정보와 사용자 회고가 포함된 상세 응답
     */
    public CookingRecordDetailResDto toCookingRecordDetailResDto(
            CookingRecord cookingRecord
    ) {
        return CookingRecordDetailResDto.create(
                cookingRecord.getId(),
                cookingRecord.getRecipe().getMenu().getName(),
                cookingRecord.getRecipe().getMenu().getThumbnailUrl(),
                cookingRecord.getRecipe().getMenu().getTimeRequired(),
                cookingRecord.getRecipe().getMenu().getDifficultyLevel(),
                cookingRecord.getTasteRating(),
                cookingRecord.getDifficultyLevel(),
                cookingRecord.getCookingTip(),
                cookingRecord.getPhotoUrl()
        );
    }

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
                                    cookingRecordFoodIngredient.getId(),
                                    foodIngredientId,
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
     * @param firstCookingStepTips 첫 단계에 연결된 요리 팁과 콘텐츠
     * @param firstCookingStepFoodIngredients 첫 단계에 연결된 요리 기록 음식 재료
     * @return 요리 시작 응답 DTO
     */
    public CookingStartResDto toCookingStartResDto(
            CookingRecord cookingRecord,
            Recipe recipe,
            CookingStepGenerateGeminiResponseDto generated,
            CookingStep firstCookingStep,
            List<CookingStepTip> firstCookingStepTips,
            List<CookingStepFoodIngredient> firstCookingStepFoodIngredients
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
                .currentCookingStep(toCookingStepDetailResDto(
                        firstCookingStep,
                        firstCookingStepTips,
                        firstCookingStepFoodIngredients
                ))
                .cookingStepTitles(cookingStepTitles)
                .build();
    }

    /**
     * 요리 세션과 현재 요리 단계를 단계 이동 응답 DTO로 변환합니다.
     *
     * @param cookingSession 단계 이동이 완료된 요리 세션
     * @param cookingStep 현재 요리 단계
     * @param cookingStepTips 현재 단계에 연결된 요리 팁과 콘텐츠
     * @param cookingStepFoodIngredients 현재 단계에 연결된 요리 기록 음식 재료
     * @return 단계 이동 응답 DTO
     */
    public CookingStepNavigationResDto toCookingStepNavigationResDto(
            CookingSession cookingSession,
            CookingStep cookingStep,
            List<CookingStepTip> cookingStepTips,
            List<CookingStepFoodIngredient> cookingStepFoodIngredients
    ) {
        Integer currentLevel = cookingSession.getCurrentCookingStepLevel();
        Integer nextLevel = currentLevel < cookingSession.getCookingStepCount()
                ? currentLevel + 1
                : null;
        return CookingStepNavigationResDto.builder()
                .cookingStepCount(cookingSession.getCookingStepCount())
                .prevCookingStepLevel(currentLevel - 1)
                .nextCookingStepLevel(nextLevel)
                .currentCookingStep(toCookingStepDetailResDto(
                        cookingStep,
                        cookingStepTips,
                        cookingStepFoodIngredients
                ))
                .build();
    }

    /**
     * 요리 세션과 현재 요리 단계를 현재 단계 조회 응답 DTO로 변환합니다.
     *
     * @param cookingSession 조회할 요리 세션
     * @param cookingStep 현재 요리 단계
     * @param cookingStepTips 현재 단계에 연결된 요리 팁과 콘텐츠
     * @param cookingStepFoodIngredients 현재 단계에 연결된 요리 기록 음식 재료
     * @return 현재 요리 단계 조회 응답 DTO
     */
    public CurrentCookingStepResDto toCurrentCookingStepResDto(
            CookingSession cookingSession,
            CookingStep cookingStep,
            List<CookingStepTip> cookingStepTips,
            List<CookingStepFoodIngredient> cookingStepFoodIngredients
    ) {
        Integer currentLevel = cookingSession.getCurrentCookingStepLevel();
        Integer nextLevel = currentLevel < cookingSession.getCookingStepCount()
                ? currentLevel + 1
                : null;
        CurrentCookingStepDetailResDto currentCookingStep =
                CurrentCookingStepDetailResDto.create(
                        cookingStep.getLevel(),
                        cookingStep.getTitle(),
                        cookingStep.getThumbnailUrl(),
                        cookingStep.getContent(),
                        cookingStep.getSubContent(),
                        toCookingStepTips(cookingStepTips),
                        toCurrentCookingStepFoodIngredients(cookingStepFoodIngredients)
                );
        return CurrentCookingStepResDto.create(
                cookingSession.getCookingStepCount(),
                currentLevel - 1,
                nextLevel,
                currentCookingStep
        );
    }

    /**
     * CookingStep과 연결된 요리 팁 및 음식 재료를 현재 요리 단계 상세 DTO로 변환합니다.
     *
     * @param cookingStep 변환할 요리 단계
     * @param cookingStepTips 현재 단계에 연결된 요리 팁과 콘텐츠
     * @param cookingStepFoodIngredients 현재 단계에 연결된 요리 기록 음식 재료
     * @return 현재 요리 단계 상세 DTO
     */
    public CookingStepDetailResDto toCookingStepDetailResDto(
            CookingStep cookingStep,
            List<CookingStepTip> cookingStepTips,
            List<CookingStepFoodIngredient> cookingStepFoodIngredients
    ) {
        List<CookingStepTipResDto> tips = toCookingStepTips(cookingStepTips);
        return CookingStepDetailResDto.builder()
                .cookingStepId(cookingStep.getId())
                .level(cookingStep.getLevel())
                .title(cookingStep.getTitle())
                .thumbnailUrl(cookingStep.getThumbnailUrl())
                .content(cookingStep.getContent())
                .subContent(cookingStep.getSubContent())
                .tips(tips)
                .foodIngredients(toCookingStepFoodIngredients(cookingStepFoodIngredients))
                .build();
    }

    /**
     * 요리 단계에 연결된 팁과 콘텐츠를 정렬된 응답 DTO 목록으로 변환합니다.
     *
     * @param cookingStepTips 현재 단계에 연결된 요리 팁과 콘텐츠
     * @return 요리 팁 PK와 콘텐츠 정렬 순서로 정렬된 요리 팁 응답
     */
    private List<CookingStepTipResDto> toCookingStepTips(
            List<CookingStepTip> cookingStepTips
    ) {
        return cookingStepTips.stream()
                .sorted(Comparator.comparing(
                        cookingStepTip -> cookingStepTip.getCookingTip().getId()
                ))
                .flatMap(cookingStepTip -> {
                    CookingTip cookingTip = cookingStepTip.getCookingTip();
                    return cookingTip.getContents().stream()
                            .sorted(Comparator.comparing(CookingTipContent::getSortOrder))
                            .map(content -> CookingStepTipResDto.create(
                                    cookingTip.getId(),
                                    content.getSortOrder(),
                                    cookingTip.getTitle(),
                                    content.getContent(),
                                    content.getType()
                            ));
                })
                .toList();
    }

    /**
     * 현재 단계에 연결된 요리 기록 음식 재료를 현재 단계 조회 응답으로 변환합니다.
     *
     * @param cookingStepFoodIngredients 현재 단계에 연결된 요리 기록 음식 재료
     * @return 요리 기록 음식 재료 PK 순으로 정렬된 현재 단계 음식 재료 응답
     */
    private List<CurrentCookingStepFoodIngredientResDto>
            toCurrentCookingStepFoodIngredients(
                    List<CookingStepFoodIngredient> cookingStepFoodIngredients
            ) {
        return cookingStepFoodIngredients.stream()
                .map(CookingStepFoodIngredient::getCookingRecordFoodIngredient)
                .sorted(Comparator.comparing(CookingRecordFoodIngredient::getId))
                .map(cookingRecordFoodIngredient ->
                        CurrentCookingStepFoodIngredientResDto.create(
                                cookingRecordFoodIngredient.getId(),
                                cookingRecordFoodIngredient.getFoodIngredient().getId(),
                                cookingRecordFoodIngredient.getFoodIngredient().getName(),
                                cookingRecordFoodIngredient.getPrimaryUsedAmountValue(),
                                cookingRecordFoodIngredient.getFoodIngredient().getPrimaryUnit(),
                                cookingRecordFoodIngredient.getSecondaryUsedAmountValue(),
                                cookingRecordFoodIngredient.getSecondaryUsedAmountValue() == null
                                        ? null
                                        : cookingRecordFoodIngredient
                                                .getFoodIngredient()
                                                .getSecondaryUnit()
                        ))
                .toList();
    }

    /**
     * 현재 단계에 연결된 요리 기록 음식 재료를 실제 사용량 응답으로 변환합니다.
     *
     * @param cookingStepFoodIngredients 현재 단계에 연결된 요리 기록 음식 재료
     * @return 요리 기록 음식 재료 PK 순으로 정렬된 단계별 음식 재료 응답
     */
    private List<CookingStepFoodIngredientResDto> toCookingStepFoodIngredients(
            List<CookingStepFoodIngredient> cookingStepFoodIngredients
    ) {
        return cookingStepFoodIngredients.stream()
                .map(CookingStepFoodIngredient::getCookingRecordFoodIngredient)
                .sorted(Comparator.comparing(CookingRecordFoodIngredient::getId))
                .map(cookingRecordFoodIngredient ->
                        CookingStepFoodIngredientResDto.create(
                            cookingRecordFoodIngredient.getId(),
                            cookingRecordFoodIngredient.getFoodIngredient().getId(),
                            cookingRecordFoodIngredient.getFoodIngredient().getName(),
                            cookingRecordFoodIngredient.getPrimaryUsedAmountValue(),
                            cookingRecordFoodIngredient.getFoodIngredient().getPrimaryUnit(),
                            cookingRecordFoodIngredient.getSecondaryUsedAmountValue(),
                            cookingRecordFoodIngredient.getSecondaryUsedAmountValue() == null
                                    ? null
                                    : cookingRecordFoodIngredient
                                            .getFoodIngredient()
                                            .getSecondaryUnit()
                    ))
                .toList();
    }
}
