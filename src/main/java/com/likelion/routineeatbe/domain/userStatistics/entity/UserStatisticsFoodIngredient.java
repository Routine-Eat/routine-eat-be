package com.likelion.routineeatbe.domain.userStatistics.entity;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
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
@Table(name = "user_statistics_food_ingredient")
public class UserStatisticsFoodIngredient extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_statistics_food_ingredient_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_statistics_id", nullable = false)
    private UserStatistics userStatistics;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_ingredient_id", nullable = false)
    private FoodIngredient foodIngredient;

    public static UserStatisticsFoodIngredient create(
            UserStatistics userStatistics,
            FoodIngredient foodIngredient
    ) {
        return UserStatisticsFoodIngredient.builder()
                .userStatistics(userStatistics)
                .foodIngredient(foodIngredient)
                .build();
    }
}
