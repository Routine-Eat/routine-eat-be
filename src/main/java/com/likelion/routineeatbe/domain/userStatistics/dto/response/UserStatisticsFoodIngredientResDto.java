package com.likelion.routineeatbe.domain.userStatistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "UserStatisticsFoodIngredientResDto", description = "사용자 통계 음식 재료 응답 DTO")
public record UserStatisticsFoodIngredientResDto(
        @Schema(description = "음식 재료 PK", example = "1")
        Long foodIngredientId,
        @Schema(description = "음식 재료 이름", example = "달걀")
        String foodIngredientName
) {

    public static UserStatisticsFoodIngredientResDto create(
            Long foodIngredientId,
            String foodIngredientName
    ) {
        return UserStatisticsFoodIngredientResDto.builder()
                .foodIngredientId(foodIngredientId)
                .foodIngredientName(foodIngredientName)
                .build();
    }
}
