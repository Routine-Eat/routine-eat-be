package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@JsonPropertyOrder({"content", "hasNext", "nextCursor"})
@Schema(title = "CookingRecordListResDto", description = "요리 기록 커서 조회 응답 DTO")
public record CookingRecordListResDto(
        @Schema(description = "조회된 요리 기록 목록")
        List<CookingRecordListItemResDto> content,
        @Schema(description = "다음 데이터 존재 여부")
        boolean hasNext,
        @Schema(description = "다음 조회에 사용할 위치 커서")
        Integer nextCursor
) {

    public static CookingRecordListResDto create(
            List<CookingRecordListItemResDto> content,
            boolean hasNext,
            Integer nextCursor
    ) {
        return CookingRecordListResDto.builder()
                .content(content)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }
}
