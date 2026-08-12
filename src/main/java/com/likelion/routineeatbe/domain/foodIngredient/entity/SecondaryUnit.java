package com.likelion.routineeatbe.domain.foodIngredient.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SecondaryUnit {
    MO("모"),
    CM("cm"),
    MARI("마리"),
    GAE("개"),
    JULGI("줄기"),
    TSP("작은술"),
    INBUN("인분"),
    GONGGI("공기"),
    CUP("컵"),
    AL("알"),
    JANG("장"),
    BONGJI("봉지"),
    TBSP("큰술"),
    PINCH("꼬집");

    private final String description;

}

