package com.likelion.routineeatbe.domain.notification.repository;

import com.likelion.routineeatbe.domain.notification.entity.Notification;
import org.springframework.data.domain.Slice;

public interface NotificationRepositoryCustom {

    /**
     * 사용자 알림을 생성일과 알림 PK 내림차순으로 위치 커서 조회합니다.
     *
     * @param userNumber 사용자 고유 식별번호
     * @param cursor 1부터 시작하는 조회 위치
     * @param size 한 번에 조회할 알림 개수
     * @return 알림 목록 Slice
     */
    Slice<Notification> searchByUserNumber(String userNumber, Integer cursor, Integer size);
}
