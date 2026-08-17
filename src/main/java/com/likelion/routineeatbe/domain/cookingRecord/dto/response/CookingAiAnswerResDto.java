package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "CookingAiAnswerResDto", description = "요리 중 AI 답변 DTO")
public record CookingAiAnswerResDto(
        @Schema(description = "AI가 생성한 요리 답변")
        String answer
) {

    public static CookingAiAnswerResDto create(String answer) {
        return CookingAiAnswerResDto.builder()
                .answer(answer)
                .build();
    }
}
