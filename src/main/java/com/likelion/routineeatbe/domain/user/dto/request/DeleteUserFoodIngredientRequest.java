package com.likelion.routineeatbe.domain.user.dto.request;

import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(title = "DeleteUserFoodIngredientRequest: 사용자-식재료 관계 삭제 요청 DTO")
public record DeleteUserFoodIngredientRequest(
        @Schema(description = "사용자-식재료 관계 타입", example = "OWN")
        @NotNull(message = "관계 타입 입력 필수")
        UserFoodIngredientType relationType,

        @NotEmpty(message = "식재료 리스트 입력 필수")
        @Schema(description = "관계 식재료 id 리스트", example = "[1,2,3]")
        @Valid
        List<Long> foodIngredientList
) {
}
