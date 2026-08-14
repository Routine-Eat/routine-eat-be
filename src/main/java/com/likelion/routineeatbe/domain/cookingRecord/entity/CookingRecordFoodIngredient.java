package com.likelion.routineeatbe.domain.cookingRecord.entity;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "cooking_record_food_ingredient")
public class CookingRecordFoodIngredient extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "primary_used_amount_value", nullable = false)
    private Double primaryUsedAmountValue;

    @Column(name = "secondary_used_amount_value")
    private Double secondaryUsedAmountValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cooking_record_id", nullable = false)
    private CookingRecord cookingRecord;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_ingredient_id", nullable = false)
    private FoodIngredient foodIngredient;

    public static CookingRecordFoodIngredient create(
            CookingRecord cookingRecord,
            FoodIngredient foodIngredient,
            Double primaryUsedAmountValue,
            Double secondaryUsedAmountValue
    ) {
        CookingRecordFoodIngredient cookingRecordFoodIngredient =
                CookingRecordFoodIngredient.builder()
                        .cookingRecord(cookingRecord)
                        .foodIngredient(foodIngredient)
                        .primaryUsedAmountValue(primaryUsedAmountValue)
                        .secondaryUsedAmountValue(secondaryUsedAmountValue)
                        .build();
        cookingRecord.addFoodIngredient(cookingRecordFoodIngredient);
        return cookingRecordFoodIngredient;
    }
}
