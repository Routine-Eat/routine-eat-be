package com.likelion.routineeatbe.domain.menu.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "InitMenuAndRecipeCookingEquipmentResDto: 레시피 조리 도구 초기화 응답 DTO")
public record InitMenuAndRecipeCookingEquipmentResDto(
        @Schema(description = "초기화된 레시피 조리 도구 연결 데이터 개수", example = "1100")
        long initCount
) {

    public static InitMenuAndRecipeCookingEquipmentResDto create(long initCount) {
        return InitMenuAndRecipeCookingEquipmentResDto.builder()
                .initCount(initCount)
                .build();
    }
}
