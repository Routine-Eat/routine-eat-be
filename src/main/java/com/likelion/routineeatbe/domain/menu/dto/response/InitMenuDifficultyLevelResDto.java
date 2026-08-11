package com.likelion.routineeatbe.domain.menu.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "InitMenuDifficultyLevelResDto: 메뉴 난이도 초기화 응답 DTO")
public record InitMenuDifficultyLevelResDto(
        @Schema(description = "난이도를 초기화한 메뉴 개수", example = "1156")
        long initCount
) {

    public static InitMenuDifficultyLevelResDto create(long initCount) {
        return InitMenuDifficultyLevelResDto.builder()
                .initCount(initCount)
                .build();
    }
}
