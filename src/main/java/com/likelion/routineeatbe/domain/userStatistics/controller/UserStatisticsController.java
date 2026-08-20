package com.likelion.routineeatbe.domain.userStatistics.controller;

import com.likelion.routineeatbe.domain.userStatistics.dto.response.UserStatisticsResDto;
import com.likelion.routineeatbe.domain.userStatistics.service.UserStatisticsService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserStatisticsController implements UserStatisticsControllerDocs {

    private final UserStatisticsService userStatisticsService;

    @Override
    public ResponseEntity<GlobalResponse<UserStatisticsResDto>> getUserStatistics(
            Long userId,
            Long statisticsId
    ) {
        UserStatisticsResDto result = userStatisticsService.getUserStatistics(
                userId,
                statisticsId
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GlobalResponse.success(
                        HttpStatus.CREATED.value(),
                        "사용자 세끼 리포트 조회에 성공했습니다.",
                        result
                ));
    }
}
