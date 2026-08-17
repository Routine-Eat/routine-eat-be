package com.likelion.routineeatbe.domain.cookingRecord.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CookingSessionLogSearchReqDto(
        @Schema(
                description = "사용자 고유 식별번호",
                example = "1234",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "사용자 고유 식별번호는 필수입니다.")
        @Size(min = 4, max = 4, message = "사용자 고유 식별번호는 4자리여야 합니다.")
        @Pattern(regexp = "^[0-9]{4}$", message = "사용자 고유 식별번호는 숫자 4자리여야 합니다.")
        String userNumber,

        @Schema(
                description = "1부터 시작하는 조회 위치. 이후 조회는 응답의 nextCursor를 사용합니다.",
                example = "1",
                defaultValue = "1",
                minimum = "1",
                maximum = "2147483547"
        )
        @Min(value = 1, message = "커서는 1 이상이어야 합니다.")
        @Max(value = 2147483547, message = "커서는 2147483547 이하여야 합니다.")
        Integer cursor,

        @Schema(
                description = "한 번에 조회할 요리 세션 로그 개수",
                example = "10",
                defaultValue = "10",
                minimum = "1",
                maximum = "100"
        )
        @Min(value = 1, message = "조회 크기는 1 이상이어야 합니다.")
        @Max(value = 100, message = "조회 크기는 100 이하여야 합니다.")
        Integer size
) {

    public CookingSessionLogSearchReqDto {
        cursor = cursor == null ? 1 : cursor;
        size = size == null ? 10 : size;
    }
}
