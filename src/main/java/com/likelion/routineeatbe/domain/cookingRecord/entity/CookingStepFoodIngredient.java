package com.likelion.routineeatbe.domain.cookingRecord.entity;

import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "cooking_step_food_ingredient",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cooking_step_food_ingredient_step_record_food",
                columnNames = {"cooking_step_id", "cooking_record_food_ingredient_id"}
        )
)
@AttributeOverrides({
        @AttributeOverride(
                name = "createdAt",
                column = @Column(name = "created_at", nullable = false, updatable = false)
        ),
        @AttributeOverride(
                name = "updatedAt",
                column = @Column(name = "updated_at", nullable = false)
        )
})
public class CookingStepFoodIngredient extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cooking_step_food_ingredient_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cooking_step_id", nullable = false)
    private CookingStep cookingStep;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cooking_record_food_ingredient_id", nullable = false)
    private CookingRecordFoodIngredient cookingRecordFoodIngredient;

    public static CookingStepFoodIngredient create(
            CookingStep cookingStep,
            CookingRecordFoodIngredient cookingRecordFoodIngredient
    ) {
        CookingStepFoodIngredient cookingStepFoodIngredient =
                CookingStepFoodIngredient.builder()
                        .cookingStep(cookingStep)
                        .cookingRecordFoodIngredient(cookingRecordFoodIngredient)
                        .build();
        cookingStep.addCookingStepFoodIngredient(cookingStepFoodIngredient);
        cookingRecordFoodIngredient.addCookingStepFoodIngredient(cookingStepFoodIngredient);
        return cookingStepFoodIngredient;
    }
}
