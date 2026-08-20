package com.likelion.routineeatbe.domain.menu.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "InitMenuAndRecipeFoodIngredientResDto: 메뉴 음식 재료 초기화 응답 DTO")
public record InitMenuAndRecipeFoodIngredientResDto(
        @Schema(description = "초기화된 메뉴 음식 재료 개수", example = "1100")
        long initCount
) {

    public static InitMenuAndRecipeFoodIngredientResDto create(long initCount) {
        return InitMenuAndRecipeFoodIngredientResDto.builder()
                .initCount(initCount)
                .build();
    }
}
