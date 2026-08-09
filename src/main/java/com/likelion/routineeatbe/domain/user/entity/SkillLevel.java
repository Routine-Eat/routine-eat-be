package com.likelion.routineeatbe.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SkillLevel {
    BEGINNER("초보"),
    AVERAGE("중수"),
    PRO("고수");

    private final String value;
}
