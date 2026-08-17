package com.likelion.routineeatbe.domain.cookingTip.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "CookingTipInitResDto: 요리 팁 초기화 응답 DTO")
public record CookingTipInitResDto(
        @Schema(description = "초기화 후 DB에 저장된 전체 요리 팁 개수", example = "68")
        long cookingTipCount,

        @Schema(description = "초기화 후 DB에 저장된 전체 요리 팁 콘텐츠 개수", example = "169")
        long cookingTipContentCount
) {

    public static CookingTipInitResDto create(
            long cookingTipCount,
            long cookingTipContentCount
    ) {
        return CookingTipInitResDto.builder()
                .cookingTipCount(cookingTipCount)
                .cookingTipContentCount(cookingTipContentCount)
                .build();
    }
}
