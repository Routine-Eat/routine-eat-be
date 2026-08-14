package com.likelion.routineeatbe.domain.cookingRecord.mapper;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepDetailResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepTitleResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CookingRecordMapper {

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
