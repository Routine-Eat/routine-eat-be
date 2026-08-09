package com.likelion.routineeatbe.domain.foodIngredient.dto.response;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "FoodIngredientResponse: 식재료 응답 DTO")
public record FoodIngredientResponse (

    @Schema(description = "식재료 id",example = "1")
    Long id,

    @Schema(description = "식재료 이름",example = "감자")
    String name,

    @Schema(description = "식재료 종류",example = "POTATO_AND_STARCH")
    FoodIngredientType type,

    @Schema(description = "100g/ml 당 가격",example = "550")
    Long pricePerHundred,

    @Schema(description = "식재료 주단위",example = "G")
    PrimaryUnit primaryUnit,

    @Schema(description = "식재료 보조 단위",example = "GAE")
    SecondaryUnit secondaryUnit
    ){

    public static FoodIngredientResponse from(FoodIngredient foodIngredient){
        return FoodIngredientResponse.builder()
                .id(foodIngredient.getId())
                .name(foodIngredient.getName())
                .type(foodIngredient.getType())
                .pricePerHundred(foodIngredient.getPricePerHundred())
                .primaryUnit(foodIngredient.getPrimaryUnit())
                .secondaryUnit(foodIngredient.getSecondaryUnit())
                .build();
    }
}
