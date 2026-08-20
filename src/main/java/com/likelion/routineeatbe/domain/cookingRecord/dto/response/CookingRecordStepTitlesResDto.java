package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(title = "CookingRecordStepTitlesResDto", description = "진행 중인 요리 전체 단계 제목 조회 응답 DTO")
public record CookingRecordStepTitlesResDto(
        @Schema(description = "전체 요리 단계 개수", example = "10")
        Integer cookingStepCount,
        @Schema(description = "요리 단계 제목 목록")
        List<CookingStepTitleResDto> cookingStepTitles
) {

    public static CookingRecordStepTitlesResDto create(
            Integer cookingStepCount,
            List<CookingStepTitleResDto> cookingStepTitles
    ) {
        return CookingRecordStepTitlesResDto.builder()
                .cookingStepCount(cookingStepCount)
                .cookingStepTitles(List.copyOf(cookingStepTitles))
                .build();
    }
}
