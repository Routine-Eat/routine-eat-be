package com.likelion.routineeatbe.domain.mealPlan.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MealPlanType {
    VAGETABLES("채소 식단"),
    PROTIEN("단백질 식단"),
    BALANCE("균형 식단");

    private final String value;
}
