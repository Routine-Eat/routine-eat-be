package com.likelion.routineeatbe.domain.user.entity;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.user.dto.request.CreateUserFoodIngredientRequest;
import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "user_foodingredient")
public class UserFoodIngredient extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserFoodIngredientType relationType;

    private LocalDateTime expirationDate;

    private Double primaryAmountValue;

    private Double secondaryAmountValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_ingredient_id", nullable = false)
    private FoodIngredient foodIngredient;

    public static UserFoodIngredient createUserFoodIngredient(
            User user,FoodIngredient foodIngredient,UserFoodIngredientType relationType,
            Double primaryAmountValue,Double secondaryAmountValue){
        return UserFoodIngredient.builder()
                .user(user)
                .foodIngredient(foodIngredient)
                .relationType(relationType)
                .primaryAmountValue(primaryAmountValue)
                .secondaryAmountValue(secondaryAmountValue)
                .build();
    }

    public void updateAmountValue(Double primaryAmountValue,Double secondaryAmountValue){
        this.primaryAmountValue=primaryAmountValue;
        // secondaryAmountValue가 입력되었을 때만 덮어쓰기 (null이면 기존 값 유지)
        if (secondaryAmountValue != null) {
            this.secondaryAmountValue = secondaryAmountValue;
        }
    }

    public double deductPrimaryAmountValue(double amount) {
        if (amount <= 0 || primaryAmountValue == null || primaryAmountValue <= 0) {
            return amount;
        }
        double deductedAmount = Math.min(primaryAmountValue, amount);
        this.primaryAmountValue -= deductedAmount;
        return amount - deductedAmount;
    }

    public double deductSecondaryAmountValue(double amount) {
        if (amount <= 0 || secondaryAmountValue == null || secondaryAmountValue <= 0) {
            return amount;
        }
        double deductedAmount = Math.min(secondaryAmountValue, amount);
        this.secondaryAmountValue -= deductedAmount;
        return amount - deductedAmount;
    }

}
