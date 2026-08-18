package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "CurrentCookingStepResDto", description = "현재 요리 단계 조회 응답 DTO")
public record CurrentCookingStepResDto(
        @Schema(description = "전체 요리 단계 개수", example = "10")
        Integer cookingStepCount,
        @Schema(description = "이전 요리 단계 번호", example = "0")
        Integer prevCookingStepLevel,
        @Schema(description = "다음 요리 단계 번호", example = "2", nullable = true)
        Integer nextCookingStepLevel,
        @Schema(description = "현재 요리 단계 상세 정보")
        CurrentCookingStepDetailResDto currentCookingStep
) {

    public static CurrentCookingStepResDto create(
            Integer cookingStepCount,
            Integer prevCookingStepLevel,
            Integer nextCookingStepLevel,
            CurrentCookingStepDetailResDto currentCookingStep
    ) {
        return CurrentCookingStepResDto.builder()
                .cookingStepCount(cookingStepCount)
                .prevCookingStepLevel(prevCookingStepLevel)
                .nextCookingStepLevel(nextCookingStepLevel)
                .currentCookingStep(currentCookingStep)
                .build();
    }
}
