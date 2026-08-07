package com.likelion.routineeatbe.domain.menu.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecommendationType {
    DEFAULT("기본"),
    SIMPLE("자취생 간단 레시피"),
    DIET("다이어트에 좋은"),
    GLUTEN_FREE("글루텐 프리 식단");

    private final String value;
}
