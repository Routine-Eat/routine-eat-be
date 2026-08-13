package com.likelion.routineeatbe.global.exception;

import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GeminiErrorCode implements BaseErrorCode {

    API_CALL_FAILED("GEM001", "Gemini API 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    API_TIMEOUT("GEM002", "Gemini API 응답 시간이 초과되었습니다.", HttpStatus.GATEWAY_TIMEOUT),
    INVALID_RESPONSE("GEM003", "Gemini API 응답 형식이 올바르지 않습니다.", HttpStatus.BAD_GATEWAY),
    INVALID_FUNCTION_CALL("GEM004", "Gemini Function Call 응답이 올바르지 않습니다.", HttpStatus.BAD_GATEWAY),
    INVALID_METADATA("GEM005", "Gemini가 생성한 메뉴 메타데이터가 올바르지 않습니다.", HttpStatus.BAD_GATEWAY),
    RATE_LIMIT_EXCEEDED("GEM006", "Gemini API 호출 한도를 초과했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_COOKING_STEP_METADATA(
            "GEM007",
            "Gemini가 생성한 요리 단계 데이터가 올바르지 않습니다.",
            HttpStatus.BAD_GATEWAY
    );

    private final String code;
    private final String message;
    private final HttpStatus status;
}
