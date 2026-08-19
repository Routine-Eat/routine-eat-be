package com.likelion.routineeatbe.domain.notification.controller;

import com.likelion.routineeatbe.domain.notification.dto.response.NotificationPollingResDto;
import com.likelion.routineeatbe.domain.notification.service.NotificationService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationControllerDocs {

    private final NotificationService notificationService;

    @Override
    public ResponseEntity<GlobalResponse<NotificationPollingResDto>> pollNotifications(
            String userNumber
    ) {
        return ResponseEntity.ok(GlobalResponse.success(
                "신규 알림 정보 조회에 성공했습니다.",
                notificationService.pollNotifications(userNumber)
        ));
    }
}
