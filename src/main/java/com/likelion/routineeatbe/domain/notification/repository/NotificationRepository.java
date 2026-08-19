package com.likelion.routineeatbe.domain.notification.repository;

import com.likelion.routineeatbe.domain.notification.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends
        JpaRepository<Notification, Long>,
        NotificationRepositoryCustom {

    /**
     * 사용자 식별번호로 읽지 않은 알림을 최신순으로 조회합니다.
     *
     * @param userNumber 사용자 고유 식별번호
     * @return 읽지 않은 알림 목록
     */
    List<Notification> findByUser_LoginNumberAndIsReadFalseOrderByCreatedAtDescIdDesc(
            String userNumber
    );
}
