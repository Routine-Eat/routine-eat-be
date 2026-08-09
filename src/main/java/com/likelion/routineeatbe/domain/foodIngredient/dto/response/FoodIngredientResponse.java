package com.likelion.routineeatbe.domain.foodIngredient.dto.response;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "FoodIngredientResponse: 식재료 응답 DTO")
public class FoodIngredientResponse {

    @Schema(description = "식재료 id",example = "1")
    private Long id;

    @Schema(description = "식재료 이름",example = "감자")
    private String name;

    @Schema(description = "식재료 종류",example = "POTATO_AND_STARCH")
    private FoodIngredientType type;

    @Schema(description = "100g/ml 당 가격",example = "550")
    private Long pricePerHundred;

    @Schema(description = "식재료 주단위",example = "G")
    private PrimaryUnit primaryUnit;

    @Schema(description = "식재료 보조 단위",example = "GAE")
    private SecondaryUnit secondaryUnit;

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
