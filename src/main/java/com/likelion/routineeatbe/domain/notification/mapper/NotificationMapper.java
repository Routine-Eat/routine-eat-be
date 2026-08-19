package com.likelion.routineeatbe.domain.notification.mapper;

import com.likelion.routineeatbe.domain.notification.dto.response.NotificationResDto;
import com.likelion.routineeatbe.domain.notification.entity.Notification;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    private static final DateTimeFormatter CREATED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 알림 Entity를 알림 응답 DTO로 변환합니다.
     *
     * @param notification 변환할 알림 Entity
     * @return 알림 응답 DTO
     */
    public NotificationResDto toNotificationResDto(Notification notification) {
        return NotificationResDto.create(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.isRead(),
                notification.getCreatedAt().format(CREATED_AT_FORMATTER),
                notification.getContentId()
        );
    }
}
