package com.likelion.routineeatbe.domain.favoriteRecipe.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FavoriteRecipeSearchReqDto(
        @Schema(
                description = "사용자 고유 식별번호",
                example = "1234",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Size(min = 4, max = 4)
        String userNumber,

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
                description = "한 번에 조회할 찜 레시피 개수",
                example = "10",
                defaultValue = "10",
                minimum = "1",
                maximum = "100"
        )
        @Min(1)
        @Max(100)
        Integer size
) {

    public FavoriteRecipeSearchReqDto {
        cursor = cursor == null ? 1L : cursor;
        size = size == null ? 10 : size;
    }
}
