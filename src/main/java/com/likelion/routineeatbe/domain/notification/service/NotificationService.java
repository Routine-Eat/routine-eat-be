package com.likelion.routineeatbe.domain.notification.service;

import com.likelion.routineeatbe.domain.notification.dto.response.NotificationPollingResDto;
import com.likelion.routineeatbe.domain.notification.entity.Notification;
import com.likelion.routineeatbe.domain.notification.exception.NotificationErrorCode;
import com.likelion.routineeatbe.domain.notification.mapper.NotificationMapper;
import com.likelion.routineeatbe.domain.notification.repository.NotificationRepository;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    /**
     * 사용자의 읽지 않은 알림을 Polling 응답으로 조회합니다.
     *
     * @param userNumber 사용자 고유 식별번호
     * @return 읽지 않은 알림 수와 목록
     */
    @Transactional(readOnly = true)
    public NotificationPollingResDto pollNotifications(String userNumber) {
        log.info(
                "[NotificationService] 신규 알림 조회 시작 | pollNotifications() - START | userNumber: {}",
                userNumber
        );

        /*
            1. 사용자 존재 여부 확인
            - 사용자 식별번호에 해당하는 사용자가 없으면 예외를 발생시킵니다.
         */
        userRepository.findByLoginNumber(userNumber)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOT_EXIST_USER));

        /*
            2. 읽지 않은 알림 조회
            - 최신 생성순으로 조회하여 Polling 응답에 사용합니다.
         */
        List<Notification> notifications = notificationRepository
                .findByUser_LoginNumberAndIsReadFalseOrderByCreatedAtDescIdDesc(userNumber);

        /*
            3. 응답 DTO 변환
            - Entity와 DTO 간 변환은 Mapper에 위임합니다.
         */
        NotificationPollingResDto result = NotificationPollingResDto.create(
                notifications.stream()
                        .map(notificationMapper::toNotificationResDto)
                        .toList()
        );

        log.info(
                "[NotificationService] 신규 알림 조회 종료 | pollNotifications() - END | userNumber: {}, count: {}",
                userNumber,
                result.newNotificationCount()
        );
        return result;
    }

    /**
     * 알림을 저장합니다.
     *
     * @param notification 저장할 알림 Entity
     */
    public void save(Notification notification) {
        log.info(
                "[NotificationService] 알림 저장 시작 | save() - START | type: {}, contentId: {}",
                notification.getType(),
                notification.getContentId()
        );
        notificationRepository.save(notification);
        log.info(
                "[NotificationService] 알림 저장 종료 | save() - END | type: {}, contentId: {}",
                notification.getType(),
                notification.getContentId()
        );
    }
}
