package com.likelion.routineeatbe.domain.mealPlan.entity;

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

    @Column(nullable = false,comment = "식단 이름 종류")
    private String name;

    @Column(nullable = false,comment = "식단 종류")
    @Enumerated(EnumType.STRING)
    private MealPlanType type;

    @Column(nullable = false,comment = "식단 상태 종류")
    @Enumerated(EnumType.STRING)
    private MealPlanStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;
}
