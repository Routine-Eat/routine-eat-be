package com.likelion.routineeatbe.domain.mealPlan.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MealPlanType {
    PRACTICE("실력 향상 식단"),
    USEALL("남은 재료 모두 사용 식단"),
    SIMPLE("요리가 간단한 메뉴 식단"),
    RECYCLING("한 재료로 모든 메뉴 식단");

    private final String value;
}
