package com.likelion.routineeatbe.domain.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@JsonPropertyOrder({"content", "hasNext", "nextCursor"})
@Schema(title = "NotificationListResDto", description = "알림 목록 커서 조회 응답 DTO")
public record NotificationListResDto(
        @Schema(description = "조회된 알림 목록")
        List<NotificationResDto> content,
        @Schema(description = "다음 데이터 존재 여부")
        boolean hasNext,
        @Schema(description = "다음 조회에 사용할 위치 커서")
        Integer nextCursor
) {

    public static NotificationListResDto create(
            List<NotificationResDto> content,
            boolean hasNext,
            Integer nextCursor
    ) {
        return NotificationListResDto.builder()
                .content(content)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }
}
