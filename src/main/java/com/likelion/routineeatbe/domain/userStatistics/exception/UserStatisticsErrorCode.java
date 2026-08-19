package com.likelion.routineeatbe.domain.userStatistics.exception;

import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserStatisticsErrorCode implements BaseErrorCode {

    USER_NOT_FOUND("USER_STATISTICS4041", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    USER_STATISTICS_NOT_FOUND(
            "USER_STATISTICS4042",
            "사용자 통계를 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    );

    private final String code;
    private final String message;
    private final HttpStatus status;
}
