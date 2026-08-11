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
    Long foodIngredientId,

    @Schema(description = "식재료 이름",example = "감자")
    String foodIngredientName,

    @Schema(description = "식재료 종류",example = "POTATO_AND_STARCH")
    FoodIngredientType foodIngredientType,

    @Schema(description = "100g/ml 당 가격",example = "550")
    Long foodIngredientPricePerHundred,

    @Schema(description = "식재료 주단위",example = "G")
    PrimaryUnit foodIngredientPrimaryUnit,

    @Schema(description = "식재료 보조 단위",example = "GAE")
    SecondaryUnit foodIngredientSecondaryUnit,

    @Schema(description = "알레르기 대표 여부",example = "true")
    Boolean foodIngredientAllergy,

    @Schema(description = "비선호 대표 여부",example = "true")
    Boolean foodIngredientDislike
    ){

    public static FoodIngredientResponse from(FoodIngredient foodIngredient){
        return FoodIngredientResponse.builder()
                .foodIngredientId(foodIngredient.getId())
                .foodIngredientName(foodIngredient.getName())
                .foodIngredientType(foodIngredient.getType())
                .foodIngredientPricePerHundred(foodIngredient.getPricePerHundred())
                .foodIngredientPrimaryUnit(foodIngredient.getPrimaryUnit())
                .foodIngredientSecondaryUnit(foodIngredient.getSecondaryUnit())
                .foodIngredientAllergy(foodIngredient.getAllergy())
                .foodIngredientDislike(foodIngredient.getDislike())
                .build();
    }
}
