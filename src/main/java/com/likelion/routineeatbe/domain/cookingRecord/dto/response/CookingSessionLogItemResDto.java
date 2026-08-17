package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionLogType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "CookingSessionLogItemResDto", description = "AI 대화 기록 항목 응답 DTO")
public record CookingSessionLogItemResDto(
        @Schema(description = "요리 세션 로그 PK", example = "10")
        Long cookingSessionLogId,
        @Schema(description = "요리 세션 로그 타입", example = "USER")
        CookingSessionLogType cookingSessionLogType,
        @Schema(description = "사용자 발화, AI 답변 또는 시스템 동작 내용")
        String cookingSessionLogContent
) {

    public static CookingSessionLogItemResDto create(
            Long cookingSessionLogId,
            CookingSessionLogType cookingSessionLogType,
            String cookingSessionLogContent
    ) {
        return CookingSessionLogItemResDto.builder()
                .cookingSessionLogId(cookingSessionLogId)
                .cookingSessionLogType(cookingSessionLogType)
                .cookingSessionLogContent(cookingSessionLogContent)
                .build();
    }
}
