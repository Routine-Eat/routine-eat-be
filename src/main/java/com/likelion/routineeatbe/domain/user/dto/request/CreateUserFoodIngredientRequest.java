package com.likelion.routineeatbe.domain.user.dto.request;

import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(title = "CreateUserFoodIngredientRequest: 사용자-식재료 관계 생성 요청 DTO")
public record CreateUserFoodIngredientRequest(
        @NotNull(message = "관계 타입 입력 필수")
        @Schema(description = "사용자-식재료 관계 타입", example = "OWN")
        UserFoodIngredientType relationType,

        @NotEmpty(message = "식재료 리스트 입력 필수")
        @Schema(description = "관계 식재료 id 리스트", example = """
                [
                  { "foodIngredientId": 1,"primaryAmountValue":200,"secondaryAmountValue":2}
                ]
                """)
        @Valid
        List<FoodIngredientDto> foodIngredientList
) {
        public record FoodIngredientDto(
                @NotNull(message = "식재료 id 입력 필수")
                @Schema(description = "보조 단위 수량", example = "2")
                Long foodIngredientId,
                @Schema(description = "기본 단위 수량", example = "200")
                Double primaryAmountValue,
                @Schema(description = "보조 단위 수량", example = "2")
                Double secondaryAmountValue
        ){}
}
