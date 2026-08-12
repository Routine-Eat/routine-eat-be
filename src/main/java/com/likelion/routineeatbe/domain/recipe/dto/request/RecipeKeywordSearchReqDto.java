package com.likelion.routineeatbe.domain.recipe.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecipeKeywordSearchReqDto(
        @Schema(
                description = "사용자 고유 식별번호",
                example = "1234",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Size(min = 4, max = 4)
        String userNumber,

        @Schema(
                description = "메뉴/레시피명 검색어",
                example = "감자",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Size(max = 100)
        String searchWord,

        @Schema(
                description = "1부터 시작하는 조회 위치. 이후 조회는 응답의 nextCursor를 사용합니다.",
                example = "1",
                defaultValue = "1",
                minimum = "1",
                maximum = "2147483647"
        )
        @Min(1)
        @Max(Integer.MAX_VALUE)
        Long cursor,

        @Schema(
                description = "한 번에 조회할 레시피 개수",
                example = "10",
                defaultValue = "10",
                minimum = "1",
                maximum = "100"
        )
        @Min(1)
        @Max(100)
        Integer size
) {

    public RecipeKeywordSearchReqDto {
        cursor = cursor == null ? 1L : cursor;
        size = size == null ? 10 : size;
    }
}
