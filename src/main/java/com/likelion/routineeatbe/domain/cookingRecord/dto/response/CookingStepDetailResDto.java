package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(title = "CookingStepDetailResDto", description = "현재 요리 단계 상세 DTO")
public record CookingStepDetailResDto(
        @Schema(description = "요리 단계 PK", example = "1")
        Long cookingStepId,
        @Schema(description = "요리 단계 번호", example = "2")
        Long level,
        @Schema(description = "요리 단계 제목", example = "재료 준비: 대파 준비하기")
        String title,
        @Schema(description = "요리 단계 썸네일 URL")
        String thumbnailUrl,
        @Schema(description = "요리 단계 설명 본문")
        String content,
        @Schema(description = "초보자용 요리 단계 부연 설명")
        String subContent,
        @Schema(description = "요리 단계 팁 목록")
        List<CookingStepTipResDto> stepTips
) {

    public static CookingStepDetailResDto create(
            Long cookingStepId,
            Long level,
            String title,
            String thumbnailUrl,
            String content,
            String subContent,
            List<CookingStepTipResDto> stepTips
    ) {
        return CookingStepDetailResDto.builder()
                .cookingStepId(cookingStepId)
                .level(level)
                .title(title)
                .thumbnailUrl(thumbnailUrl)
                .content(content)
                .subContent(subContent)
                .stepTips(List.copyOf(stepTips))
                .build();
    }
}
