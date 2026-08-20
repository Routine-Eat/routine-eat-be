package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import com.likelion.routineeatbe.domain.cookingTip.enums.CookingTipContentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "CookingStepTipResDto", description = "요리 단계 팁 DTO")
public record CookingStepTipResDto(
        @Schema(description = "요리 팁 PK", example = "1")
        Long cookingTipId,
        @Schema(description = "요리 팁 내부 콘텐츠 정렬 순서", example = "1")
        Integer sortNum,
        @Schema(description = "요리 팁 제목", example = "대파 써는 법")
        String cookingTipTitle,
        @Schema(description = "요리 팁 텍스트 또는 이미지 URL")
        String cookingTipContent,
        @Schema(description = "요리 팁 콘텐츠 타입", example = "TEXT")
        CookingTipContentType cookingTipType
) {

    public static CookingStepTipResDto create(
            Long cookingTipId,
            Integer sortNum,
            String cookingTipTitle,
            String cookingTipContent,
            CookingTipContentType cookingTipType
    ) {
        return CookingStepTipResDto.builder()
                .cookingTipId(cookingTipId)
                .sortNum(sortNum)
                .cookingTipTitle(cookingTipTitle)
                .cookingTipContent(cookingTipContent)
                .cookingTipType(cookingTipType)
                .build();
    }
}
