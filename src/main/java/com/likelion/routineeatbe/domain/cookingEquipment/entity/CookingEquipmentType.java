package com.likelion.routineeatbe.domain.cookingEquipment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CookingEquipmentType {
    APPLIANCE("조리기기"),
    UTENSIL("조리도구"),
    PREP_TOOL("손질도구"),
    ETC("기타");

    private final String value;
}
