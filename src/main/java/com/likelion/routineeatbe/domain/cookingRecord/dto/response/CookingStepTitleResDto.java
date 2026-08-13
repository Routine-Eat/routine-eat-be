package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "CookingStepTitleResDto", description = "요리 단계 제목 DTO")
public record CookingStepTitleResDto(
        @Schema(description = "요리 단계 번호", example = "1")
        Long stepLevel,
        @Schema(description = "요리 단계 제목", example = "재료 준비: 대파 준비하기")
        String stepTitle
) {

    public static CookingStepTitleResDto create(Long stepLevel, String stepTitle) {
        return CookingStepTitleResDto.builder()
                .stepLevel(stepLevel)
                .stepTitle(stepTitle)
                .build();
    }
}
