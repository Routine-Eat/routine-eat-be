package com.likelion.routineeatbe.domain.foodIngredient.dto.response;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "FoodIngredientResponse: 식재료 응답 DTO")
public class FoodIngredientResponse {

    @Schema(description = "식재료 id",example = "1")
    private Long id;

    @Schema(description = "식재료 이름",example = "사과")
    private String name;

    @Schema(description = "식재료 종류",example = "FRUIT")
    private FoodIngredientType foodIngredientType;

}
