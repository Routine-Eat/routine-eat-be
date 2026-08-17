package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import com.likelion.routineeatbe.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "요리 중 AI 응답의 multipart/form-data 구조")
public record CookingAiMultipartResDto(
        @Schema(description = "시스템 동작 또는 AI 텍스트 답변 JSON 파트")
        GlobalResponse<Object> response,
        @Schema(
                description = "AI 텍스트 답변을 읽은 WAV 음성 파일. 시스템 동작 응답에서는 생략됩니다.",
                type = "string",
                format = "binary",
                nullable = true
        )
        byte[] audio
) {

    public static CookingAiMultipartResDto create(
            GlobalResponse<Object> response,
            byte[] audio
    ) {
        return CookingAiMultipartResDto.builder()
                .response(response)
                .audio(audio == null ? null : audio.clone())
                .build();
    }

    @Override
    public byte[] audio() {
        return audio == null ? null : audio.clone();
    }
}
