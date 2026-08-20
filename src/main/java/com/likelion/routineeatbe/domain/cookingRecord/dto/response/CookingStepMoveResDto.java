package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        title = "CookingStepMoveResDto",
        description = "다음 요리 단계 이동 또는 요리 완료 응답 DTO",
        oneOf = {CookingStepNavigationResDto.class, CookingCompleteResDto.class}
)
public sealed interface CookingStepMoveResDto
        permits CookingStepNavigationResDto, CookingCompleteResDto {
}
