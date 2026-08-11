package com.likelion.routineeatbe.domain.cookingEquipment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CookingEquipmentSymbol {
    EESSENTIAL("필수적"),
    RECOMMEND("추천");

    private final String value;
}
