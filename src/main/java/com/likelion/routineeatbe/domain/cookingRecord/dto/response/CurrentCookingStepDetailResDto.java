package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(title = "CurrentCookingStepDetailResDto", description = "현재 요리 단계 상세 DTO")
public record CurrentCookingStepDetailResDto(
        @Schema(description = "현재 요리 단계 번호", example = "1")
        Long level,
        @Schema(description = "현재 요리 단계 제목", example = "재료 준비: 대파 준비하기")
        String title,
        @Schema(description = "현재 요리 단계 썸네일 URL")
        String thumbnailUrl,
        @Schema(description = "현재 요리 단계 설명 본문")
        String content,
        @Schema(description = "초보자용 현재 요리 단계 부연 설명")
        String subContent,
        @Schema(description = "현재 요리 단계 팁 목록")
        List<CookingStepTipResDto> tips,
        @Schema(description = "현재 요리 단계에서 사용하는 음식 재료 목록")
        List<CurrentCookingStepFoodIngredientResDto> foodIngredients
) {

    public static CurrentCookingStepDetailResDto create(
            Long level,
            String title,
            String thumbnailUrl,
            String content,
            String subContent,
            List<CookingStepTipResDto> tips,
            List<CurrentCookingStepFoodIngredientResDto> foodIngredients
    ) {
        return CurrentCookingStepDetailResDto.builder()
                .level(level)
                .title(title)
                .thumbnailUrl(thumbnailUrl)
                .content(content)
                .subContent(subContent)
                .tips(List.copyOf(tips))
                .foodIngredients(List.copyOf(foodIngredients))
                .build();
    }
}
