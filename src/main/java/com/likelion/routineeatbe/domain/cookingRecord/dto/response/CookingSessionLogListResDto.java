package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@JsonPropertyOrder({"content", "hasNext", "nextCursor"})
@Schema(title = "CookingSessionLogListResDto", description = "AI 대화 기록 커서 조회 응답 DTO")
public record CookingSessionLogListResDto(
        @Schema(description = "조회된 AI 대화 기록 목록")
        List<CookingSessionLogItemResDto> content,
        @Schema(description = "다음 데이터 존재 여부", example = "true")
        boolean hasNext,
        @Schema(description = "다음 조회에 사용할 위치 커서", example = "11", nullable = true)
        Integer nextCursor
) {

    public static CookingSessionLogListResDto create(
            List<CookingSessionLogItemResDto> content,
            boolean hasNext,
            Integer nextCursor
    ) {
        return CookingSessionLogListResDto.builder()
                .content(List.copyOf(content))
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }
}
