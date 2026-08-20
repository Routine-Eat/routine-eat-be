package com.likelion.routineeatbe.domain.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    THREE_MEAL_REPORT_ARRIVED(
            "세 끼 리포트 도착!",
            "지금 내 끼니 기록을 확인하고, 추천 메뉴도 받아보세요!"
    ),
    MEAL_PLAN_COMPLETED(
            "식단 완료",
            "축하드려요, 식단을 완료했어요! 완료한 식단은 마이페이지에서 볼 수 있어요."
    );

    private final String title;
    private final String content;
}
