package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(title = "CookingStartResDto", description = "요리 시작 응답 DTO")
public record CookingStartResDto(
        @Schema(description = "요리 기록 PK", example = "1")
        Long cookingRecordId,
        @Schema(description = "레시피 이름", example = "계란 대파 볶음밥")
        String recipeName,
        @Schema(description = "레시피 썸네일 URL")
        String recipeThumbnailUrl,
        @Schema(description = "레시피 요리 소요 시간(분)", example = "8")
        Integer recipeTimeRequired,
        @Schema(description = "요리 시작 전 체크리스트")
        List<String> checkListBeforeStart,
        @Schema(description = "전체 요리 단계 개수", example = "10")
        Integer cookingStepCount,
        @Schema(description = "요리 단계 제목 목록")
        List<CookingStepTitleResDto> cookingStepTitles
) {

    public static CookingStartResDto create(
            Long cookingRecordId,
            String recipeName,
            String recipeThumbnailUrl,
            Integer recipeTimeRequired,
            List<String> checkListBeforeStart,
            Integer cookingStepCount,
            List<CookingStepTitleResDto> cookingStepTitles
    ) {
        return CookingStartResDto.builder()
                .cookingRecordId(cookingRecordId)
                .recipeName(recipeName)
                .recipeThumbnailUrl(recipeThumbnailUrl)
                .recipeTimeRequired(recipeTimeRequired)
                .checkListBeforeStart(List.copyOf(checkListBeforeStart))
                .cookingStepCount(cookingStepCount)
                .cookingStepTitles(List.copyOf(cookingStepTitles))
                .build();
    }
}
