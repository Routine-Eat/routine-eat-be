package com.likelion.routineeatbe.domain.notification.dto.response;

import com.likelion.routineeatbe.domain.notification.enums.NotificationType;
import lombok.Builder;

@Builder
public record NotificationResDto(
        Long notificationId,
        NotificationType notificationType,
        String notificationTitle,
        String notificationContent,
        boolean isRead,
        String createdAt,
        Long contentId
) {
    public static NotificationResDto create(
            Long notificationId,
            NotificationType notificationType,
            String notificationTitle,
            String notificationContent,
            boolean isRead,
            String createdAt,
            Long contentId
    ) {
        return NotificationResDto.builder()
                .notificationId(notificationId)
                .notificationType(notificationType)
                .notificationTitle(notificationTitle)
                .notificationContent(notificationContent)
                .isRead(isRead)
                .createdAt(createdAt)
                .contentId(contentId)
                .build();
    }
}
