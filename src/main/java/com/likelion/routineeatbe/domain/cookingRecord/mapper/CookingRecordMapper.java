package com.likelion.routineeatbe.domain.cookingRecord.mapper;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepTitleResDto;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CookingRecordMapper {

    /**
     * 저장된 요리 기록과 Gemini 생성 결과를 요리 시작 응답 DTO로 변환합니다.
     *
     * @param cookingRecord 저장된 요리 기록
     * @param recipe 요리를 시작한 레시피
     * @param generated Gemini가 생성한 체크리스트와 요리 단계
     * @return 요리 시작 응답 DTO
     */
    public CookingStartResDto toCookingStartResDto(
            CookingRecord cookingRecord,
            Recipe recipe,
            CookingStepGenerateGeminiResponseDto generated
    ) {
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
                .cookingStepCount(generated.cookingSteps().size())
                .cookingStepTitles(cookingStepTitles)
                .build();
    }
}
