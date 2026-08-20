package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "CookingStepNavigationResDto", description = "요리 단계 이동 응답 DTO")
public record CookingStepNavigationResDto(
        @Schema(description = "전체 요리 단계 개수", example = "10")
        Integer cookingStepCount,
        @Schema(description = "이전 요리 단계 번호", example = "1")
        Integer prevCookingStepLevel,
        @Schema(description = "다음 요리 단계 번호", example = "3", nullable = true)
        Integer nextCookingStepLevel,
        @Schema(description = "현재 요리 단계 상세 정보")
        CookingStepDetailResDto currentCookingStep
) implements CookingStepMoveResDto {

    public static CookingStepNavigationResDto create(
            Integer cookingStepCount,
            Integer prevCookingStepLevel,
            Integer nextCookingStepLevel,
            CookingStepDetailResDto currentCookingStep
    ) {
        return CookingStepNavigationResDto.builder()
                .cookingStepCount(cookingStepCount)
                .prevCookingStepLevel(prevCookingStepLevel)
                .nextCookingStepLevel(nextCookingStepLevel)
                .currentCookingStep(currentCookingStep)
                .build();
    }
}
