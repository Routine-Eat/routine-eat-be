package com.likelion.routineeatbe.domain.foodIngredient.entity;

import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "food_ingredient")
public class FoodIngredient extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200, comment = "재료명")
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FoodIngredientType type;

    @Column(nullable = false, comment = "100g/100ml당 재료 가격")
    private Long pricePerHundred;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PrimaryUnit primaryUnit;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SecondaryUnit secondaryUnit;

    @Column(nullable = false,comment = "제외 식재료 대표 여부")
    private Boolean exception;
}
