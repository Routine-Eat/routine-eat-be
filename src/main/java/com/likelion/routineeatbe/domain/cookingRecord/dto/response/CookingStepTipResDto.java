package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(title = "CookingStepTipResDto", description = "요리 단계 팁 DTO")
public record CookingStepTipResDto(
        @Schema(description = "팁 제목", example = "칼로 써는 방법 배워볼래요.")
        String tipTitle,
        @Schema(description = "팁 설명 본문")
        String tipContent,
        @Schema(description = "팁 이미지 URL 목록")
        List<String> imageUrls
) {

    public static CookingStepTipResDto create(
            String tipTitle,
            String tipContent,
            List<String> imageUrls
    ) {
        return CookingStepTipResDto.builder()
                .tipTitle(tipTitle)
                .tipContent(tipContent)
                .imageUrls(List.copyOf(imageUrls))
                .build();
    }
}
