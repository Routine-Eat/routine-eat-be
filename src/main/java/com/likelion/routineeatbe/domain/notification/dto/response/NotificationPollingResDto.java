package com.likelion.routineeatbe.domain.notification.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record NotificationPollingResDto(
        int newNotificationCount,
        List<NotificationResDto> newNotificationList
) {
    public static NotificationPollingResDto create(
            List<NotificationResDto> notificationList
    ) {
        return NotificationPollingResDto.builder()
                .newNotificationCount(notificationList.size())
                .newNotificationList(notificationList)
                .build();
    }
}
