package com.likelion.routineeatbe.domain.recipeCookingEquipment.entity;

import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
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
        name = "recipe_cooking_equipment",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recipe_cooking_equipment_recipe_equipment",
                columnNames = {"recipe_id", "cooking_equipment_id"}
        )
)
public class RecipeCookingEquipment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_cooking_equipment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cooking_equipment_id", nullable = false)
    private CookingEquipment cookingEquipment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    public static RecipeCookingEquipment create(
            Recipe recipe,
            CookingEquipment cookingEquipment
    ) {
        return RecipeCookingEquipment.builder()
                .recipe(recipe)
                .cookingEquipment(cookingEquipment)
                .build();
    }
}
