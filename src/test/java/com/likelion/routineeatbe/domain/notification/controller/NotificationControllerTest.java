package com.likelion.routineeatbe.domain.notification.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.likelion.routineeatbe.domain.notification.dto.response.NotificationPollingResDto;
import com.likelion.routineeatbe.domain.notification.dto.request.NotificationSearchReqDto;
import com.likelion.routineeatbe.domain.notification.dto.response.NotificationListResDto;
import com.likelion.routineeatbe.domain.notification.dto.response.NotificationResDto;
import com.likelion.routineeatbe.domain.notification.enums.NotificationType;
import com.likelion.routineeatbe.domain.notification.service.NotificationService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("신규 알림 Polling API 성공 - 200 반환")
    void 신규_알림_Polling_API_성공() throws Exception {
        // given
        NotificationResDto notification = NotificationResDto.create(
                1L,
                NotificationType.THREE_MEAL_REPORT_ARRIVED,
                "세 끼 리포트 도착!",
                "지금 내 끼니 기록을 확인하고, 추천 메뉴도 받아보세요!",
                false,
                "2026-08-19 18:30",
                10L
        );
        NotificationPollingResDto response = NotificationPollingResDto.create(List.of(notification));
        given(notificationService.pollNotifications("1234")).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/notifications/polling")
                        .param("userNumber", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message")
                        .value("신규 알림 정보 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.newNotificationCount").value(1))
                .andExpect(jsonPath("$.data.newNotificationList[0].notificationTitle")
                        .value("세 끼 리포트 도착!"))
                .andExpect(jsonPath("$.data.newNotificationList[0].contentId").value(10));
        then(notificationService).should().pollNotifications("1234");
    }

    @Test
    @DisplayName("신규 알림 Polling API 실패 - userNumber 누락")
    void 신규_알림_Polling_API_실패_userNumber_누락() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/notifications/polling"))
                .andExpect(status().isBadRequest());
        then(notificationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("알림 목록 조회 API 성공 - 200 반환")
    void 알림_목록_조회_API_성공() throws Exception {
        // given
        NotificationResDto notification = NotificationResDto.create(
                1L,
                NotificationType.THREE_MEAL_REPORT_ARRIVED,
                "세 끼 리포트 도착!",
                "지금 내 끼니 기록을 확인하고, 추천 메뉴도 받아보세요!",
                false,
                "2026-08-19 18:30",
                10L
        );
        NotificationListResDto response = NotificationListResDto.create(
                List.of(notification),
                true,
                11
        );
        NotificationSearchReqDto request = new NotificationSearchReqDto("1234", 1, 10);
        given(notificationService.getNotifications(request)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/notifications")
                        .param("userNumber", "1234")
                        .param("cursor", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("알림 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.content[0].notificationId").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value(11));
        then(notificationService).should().getNotifications(request);
    }

    @Test
    @DisplayName("알림 목록 조회 API 실패 - 조회 크기 초과")
    void 알림_목록_조회_API_실패_조회_크기_초과() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/notifications")
                        .param("userNumber", "1234")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
        then(notificationService).shouldHaveNoInteractions();
    }
}
