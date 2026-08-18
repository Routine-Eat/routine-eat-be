package com.likelion.routineeatbe.domain.cookingRecord.dto.request;

import com.likelion.routineeatbe.domain.cookingRecord.enums.TasteRating;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(title = "CookingResultSaveReqDto", description = "요리 결과 저장 요청 DTO")
public record CookingResultSaveReqDto(
        @Schema(description = "맛 평가", example = "LEVEL_3")
        @NotNull(message = "맛 평가는 필수입니다.")
        TasteRating tasteRating,

        @Schema(description = "실제 요리 난이도", example = "LEVEL_2")
        @NotNull(message = "요리 난이도는 필수입니다.")
        DifficultyLevel difficultyLevel,

        @Schema(
                description = "사용자가 작성한 요리 팁",
                example = "참기름을 조금 더 넣으면 맛있습니다.",
                nullable = true
        )
        @Size(max = 500, message = "요리 팁은 500자 이하여야 합니다.")
        String cookingTip,

        @Schema(
                description = "실제 사용량을 수정할 요리 기록 음식 재료 목록",
                nullable = true
        )
        List<@NotNull(message = "수정할 요리 기록 음식 재료는 null일 수 없습니다.")
                @Valid ModifiedCookingRecordFoodIngredientReqDto>
                modifiedCookingRecordFoodIngredients
) {
}
