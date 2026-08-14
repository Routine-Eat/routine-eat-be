package com.likelion.routineeatbe.domain.cookingRecord.dto.request;

import com.likelion.routineeatbe.domain.cookingRecord.enums.TasteRating;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(title = "CookingResultSaveReqDto", description = "요리 결과 저장 요청 DTO")
public record CookingResultSaveReqDto(
        @Schema(description = "맛 평가", example = "LEVEL_3")
        @NotNull(message = "맛 평가는 필수입니다.")
        TasteRating tasteRating,

        @Schema(description = "실제 요리 난이도", example = "LEVEL_2")
        @NotNull(message = "요리 난이도는 필수입니다.")
        DifficultyLevel difficultyLevel
) {
}
