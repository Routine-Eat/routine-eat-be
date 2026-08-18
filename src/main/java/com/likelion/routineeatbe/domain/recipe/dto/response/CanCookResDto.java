package com.likelion.routineeatbe.domain.recipe.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record CanCookResDto(
        @Schema(description = "요리 가능 여부", example = "true")
        boolean canCook
) {

    public static CanCookResDto create(boolean canCook) {
        return CanCookResDto.builder()
                .canCook(canCook)
                .build();
    }
}
