package com.likelion.routineeatbe.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserFoodIngredientType {
    EXCEPTION("제외"),
    OWN("보유"),
    RESERVATION("예약");

    private final String value;
}
