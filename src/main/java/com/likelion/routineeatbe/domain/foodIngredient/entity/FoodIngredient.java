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


}
