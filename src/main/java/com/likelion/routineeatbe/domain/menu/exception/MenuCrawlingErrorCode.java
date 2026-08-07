package com.likelion.routineeatbe.domain.menu.exception;

import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MenuCrawlingErrorCode implements BaseErrorCode {

    INVALID_CRAWLING_RANGE("MCR001", "크롤링 요청 범위가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    EXTERNAL_API_CALL_FAILED("MCR002", "식품안전나라 API 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    INVALID_EXTERNAL_API_RESPONSE("MCR003", "식품안전나라 API 응답 형식이 올바르지 않습니다.", HttpStatus.BAD_GATEWAY),
    EXTERNAL_API_TIMEOUT("MCR004", "식품안전나라 API 응답 시간이 초과되었습니다.", HttpStatus.GATEWAY_TIMEOUT);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
