package com.likelion.routineeatbe.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.likelion.routineeatbe.domain.notification.dto.response.NotificationPollingResDto;
import com.likelion.routineeatbe.domain.notification.dto.request.NotificationSearchReqDto;
import com.likelion.routineeatbe.domain.notification.dto.response.NotificationListResDto;
import com.likelion.routineeatbe.domain.notification.dto.response.NotificationResDto;
import com.likelion.routineeatbe.domain.notification.entity.Notification;
import com.likelion.routineeatbe.domain.notification.enums.NotificationType;
import com.likelion.routineeatbe.domain.notification.exception.NotificationErrorCode;
import com.likelion.routineeatbe.domain.notification.mapper.NotificationMapper;
import com.likelion.routineeatbe.domain.notification.repository.NotificationRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Test
    @DisplayName("사용자의 읽지 않은 알림을 최신순으로 조회한다")
    void 사용자의_읽지_않은_알림_조회_성공() {
        // given
        String userNumber = "1234";
        Notification notification = Notification.create(
                User.builder().loginNumber(userNumber).build(),
                NotificationType.THREE_MEAL_REPORT_ARRIVED,
                10L
        );
        NotificationResDto notificationResDto = NotificationResDto.create(
                1L,
                NotificationType.THREE_MEAL_REPORT_ARRIVED,
                "세 끼 리포트 도착!",
                "지금 내 끼니 기록을 확인하고, 추천 메뉴도 받아보세요!",
                false,
                "2026-08-19 18:30",
                10L
        );
        given(userRepository.findByLoginNumber(userNumber))
                .willReturn(Optional.of(notification.getUser()));
        given(notificationRepository
                .findByUser_LoginNumberAndIsReadFalseOrderByCreatedAtDescIdDesc(userNumber))
                .willReturn(List.of(notification));
        given(notificationMapper.toNotificationResDto(notification)).willReturn(notificationResDto);

        // when
        NotificationPollingResDto result = notificationService.pollNotifications(userNumber);

        // then
        assertThat(result.newNotificationCount()).isEqualTo(1);
        assertThat(result.newNotificationList()).containsExactly(notificationResDto);
        then(notificationRepository).should()
                .findByUser_LoginNumberAndIsReadFalseOrderByCreatedAtDescIdDesc(userNumber);
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 알림 조회에 실패한다")
    void 존재하지_않는_사용자_알림_조회_실패() {
        // given
        String userNumber = "9999";
        given(userRepository.findByLoginNumber(userNumber)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.pollNotifications(userNumber))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(NotificationErrorCode.NOT_EXIST_USER));
        then(notificationRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("알림 목록을 커서 기반으로 조회한다")
    void 알림_목록_커서_조회_성공() {
        // given
        NotificationSearchReqDto request = new NotificationSearchReqDto("1234", 1, 1);
        Notification notification = Notification.create(
                User.builder().loginNumber("1234").build(),
                NotificationType.MEAL_PLAN_COMPLETED,
                20L
        );
        NotificationResDto notificationResDto = NotificationResDto.create(
                2L,
                NotificationType.MEAL_PLAN_COMPLETED,
                "식단 완료",
                "축하드려요, 식단을 완료했어요! 완료한 식단은 마이페이지에서 볼 수 있어요.",
                false,
                "2026-08-19 18:30",
                20L
        );
        given(userRepository.findByLoginNumber("1234"))
                .willReturn(Optional.of(notification.getUser()));
        given(notificationRepository.searchByUserNumber("1234", 1, 1))
                .willReturn(new SliceImpl<>(List.of(notification), PageRequest.of(0, 1), true));
        given(notificationMapper.toNotificationResDto(notification)).willReturn(notificationResDto);

        // when
        NotificationListResDto result = notificationService.getNotifications(request);

        // then
        assertThat(result.content()).containsExactly(notificationResDto);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(2);
    }
}
