package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "CookingResultSaveResDto", description = "요리 결과 저장 응답 DTO")
public record CookingResultSaveResDto(
        @Schema(description = "저장된 요리 기록 PK", example = "1")
        Long savedCookingRecordId
) {

    public static CookingResultSaveResDto create(Long savedCookingRecordId) {
        return CookingResultSaveResDto.builder()
                .savedCookingRecordId(savedCookingRecordId)
                .build();
    }
}
