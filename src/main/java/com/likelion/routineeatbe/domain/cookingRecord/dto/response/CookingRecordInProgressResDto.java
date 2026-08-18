package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "CookingRecordInProgressResDto", description = "진행 중인 요리 세션 조회 응답 DTO")
public record CookingRecordInProgressResDto(
        @Schema(description = "진행 중인 요리 기록 PK", example = "1")
        Long cookingRecordId
) {

    public static CookingRecordInProgressResDto create(Long cookingRecordId) {
        return CookingRecordInProgressResDto.builder()
                .cookingRecordId(cookingRecordId)
                .build();
    }
}
