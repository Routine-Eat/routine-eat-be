package com.likelion.routineeatbe.global.enums;
import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GlobalErrorCode implements BaseErrorCode {
    // 공통 및 입력값 검증
    INVALID_INPUT_VALUE("G001", "유효하지 않은 입력입니다.", HttpStatus.BAD_REQUEST),
    INVALID_TYPE_VALUE("G002", "타입이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    HTTP_MESSAGE_NOT_READABLE("G003", "요청 본문을 읽을 수 없습니다.", HttpStatus.BAD_REQUEST),

    // 리소스 관련
    RESOURCE_NOT_FOUND("G004", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED("G005", "지원하지 않는 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),

    // 인증/인가
    UNAUTHORIZED("G006", "인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("G007", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 서버 오류
    INTERNAL_SERVER_ERROR("G008", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

