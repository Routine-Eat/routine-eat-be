package com.likelion.routineeatbe.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserFoodIngredientType {
    ALLERGY("알러지"),
    DISLIKE("비선호"),
    OWN("보유"),
    RESERVATION("예약");

    private final String value;
}
