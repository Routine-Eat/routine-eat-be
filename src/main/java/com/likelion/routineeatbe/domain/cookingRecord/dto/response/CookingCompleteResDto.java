package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;

@Builder
@Schema(title = "CookingCompleteResDto", description = "요리 완료 응답 DTO")
public record CookingCompleteResDto(
        @Schema(description = "요리된 메뉴 이름", example = "오징어볶음")
        String cookedMenuName,
        @Schema(description = "요리 완료 날짜", example = "2026-08-21")
        LocalDate cookedDate
) implements CookingStepMoveResDto {

    public static CookingCompleteResDto create(
            String cookedMenuName,
            LocalDate cookedDate
    ) {
        return CookingCompleteResDto.builder()
                .cookedMenuName(cookedMenuName)
                .cookedDate(cookedDate)
                .build();
    }
}
