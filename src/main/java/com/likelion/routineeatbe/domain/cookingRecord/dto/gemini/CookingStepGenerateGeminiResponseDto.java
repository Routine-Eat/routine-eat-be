package com.likelion.routineeatbe.domain.cookingRecord.dto.gemini;

import com.likelion.routineeatbe.domain.cookingSession.enums.CookingStepStage;
import java.util.List;
import lombok.Builder;

@Builder
public record CookingStepGenerateGeminiResponseDto(
        List<String> checkListBeforeStart,
        List<GeneratedCookingStep> cookingSteps
) {

    public static CookingStepGenerateGeminiResponseDto create(
            List<String> checkListBeforeStart,
            List<GeneratedCookingStep> cookingSteps
    ) {
        return CookingStepGenerateGeminiResponseDto.builder()
                .checkListBeforeStart(List.copyOf(checkListBeforeStart))
                .cookingSteps(List.copyOf(cookingSteps))
                .build();
    }

    @Builder
    public record GeneratedCookingStep(
            Integer level,
            CookingStepStage stage,
            String title,
            String content,
            String subContent
    ) {

        public static GeneratedCookingStep create(
                Integer level,
                CookingStepStage stage,
                String title,
                String content,
                String subContent
        ) {
            return GeneratedCookingStep.builder()
                    .level(level)
                    .stage(stage)
                    .title(title)
                    .content(content)
                    .subContent(subContent)
                    .build();
        }
    }
}
