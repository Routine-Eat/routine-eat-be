package com.likelion.routineeatbe.domain.foodIngredient.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PrimaryUnit {
    G("그램"),
    ML("밀리리터");

    private final String description;
}
