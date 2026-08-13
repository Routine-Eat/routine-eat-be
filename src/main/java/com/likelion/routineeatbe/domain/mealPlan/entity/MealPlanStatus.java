package com.likelion.routineeatbe.domain.mealPlan.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MealPlanStatus {
    DONE("완료"),
    PROGRESS("진행 중"),
    SAVED("저장");

    private final String value;
}
