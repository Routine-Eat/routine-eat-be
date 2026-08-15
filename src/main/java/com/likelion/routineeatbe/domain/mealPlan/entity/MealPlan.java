package com.likelion.routineeatbe.domain.mealPlan.entity;

import com.likelion.routineeatbe.domain.mealPlan.dto.request.CreateMealPlanRequest;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "meal_plan")
public class MealPlan extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,comment = "식단 종류",length = 10)
    @Enumerated(EnumType.STRING)
    private MealPlanType type;

    @Column(nullable = false,comment = "식단 상태 종류")
    @Enumerated(EnumType.STRING)
    private MealPlanStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    public static MealPlan createMealPlan(CreateMealPlanRequest request,User user){
        return MealPlan.builder()
                .type(request.mealPlanType())
                .status(request.mealPlanStatus())
                .user(user)
                .build();
    }
    public void updateMealPlanStatus(MealPlanStatus status){
        this.status=status;
    }
}
