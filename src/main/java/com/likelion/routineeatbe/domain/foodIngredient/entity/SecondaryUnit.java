package com.likelion.routineeatbe.domain.foodIngredient.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SecondaryUnit {
    MO("모"),
    MARI("마리"),
    GAE("개"),
    JULGI("줄기"),
    TSP("작은술"),
    INBUN("인분");

    private final String description;

}

