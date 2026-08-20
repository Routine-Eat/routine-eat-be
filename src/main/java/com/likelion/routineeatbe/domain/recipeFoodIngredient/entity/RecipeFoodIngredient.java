package com.likelion.routineeatbe.domain.recipeFoodIngredient.entity;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.global.common.BaseTimeEntity;
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
        name = "recipe_food_ingredient",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recipe_food_ingredient_recipe_food_ingredient",
                columnNames = {"recipe_id", "food_ingredient_id"}
        )
)
public class RecipeFoodIngredient extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_food_ingredient_id")
    private Long id;

    @Column(name = "primary_need_amount_value", nullable = false)
    private Double primaryNeedAmountValue;

    @Column(name = "secondary_need_amount_value")
    private Double secondaryNeedAmountValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_ingredient_id", nullable = false)
    private FoodIngredient foodIngredient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    public static RecipeFoodIngredient create(
            Recipe recipe,
            FoodIngredient foodIngredient,
            Double primaryNeedAmountValue,
            Double secondaryNeedAmountValue
    ) {
        return RecipeFoodIngredient.builder()
                .recipe(recipe)
                .foodIngredient(foodIngredient)
                .primaryNeedAmountValue(primaryNeedAmountValue)
                .secondaryNeedAmountValue(secondaryNeedAmountValue)
                .build();
    }
}
