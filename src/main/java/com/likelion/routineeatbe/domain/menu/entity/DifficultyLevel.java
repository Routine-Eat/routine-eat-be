package com.likelion.routineeatbe.domain.menu.entity;

public enum DifficultyLevel {
    LEVEL_1,
    LEVEL_2,
    LEVEL_3,
    LEVEL_4,
    LEVEL_5;

    public static DifficultyLevel fromScore(int score) {
        return switch (score) {
            case 1 -> LEVEL_1;
            case 2 -> LEVEL_2;
            case 3 -> LEVEL_3;
            case 4 -> LEVEL_4;
            case 5 -> LEVEL_5;
            default -> throw new IllegalArgumentException("난이도 점수는 1 이상 5 이하여야 합니다.");
        };
    }
}
