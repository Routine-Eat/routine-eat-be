package com.likelion.routineeatbe.domain.menu.exception;

import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum FoodSafetyKoreaApiMessageCode implements BaseErrorCode {

    INFO_000("INFO-000", "정상 처리되었습니다.", HttpStatus.OK),
    ERROR_300("ERROR-300", "필수 값이 누락되어 있습니다. 요청인자를 참고 하십시오.", HttpStatus.BAD_REQUEST),
    INFO_100("INFO-100", "인증키가 유효하지 않습니다. 인증키가 없는 경우, 홈페이지에서 인증키를 신청하십시오.", HttpStatus.UNAUTHORIZED),
    ERROR_301("ERROR-301", "파일타입 값이 누락 혹은 유효하지 않습니다. 요청인자 중 TYPE을 확인하십시오.", HttpStatus.BAD_REQUEST),
    ERROR_310("ERROR-310", "해당하는 서비스를 찾을 수 없습니다. 요청인자 중 SERVICE를 확인하십시오.", HttpStatus.NOT_FOUND),
    ERROR_331("ERROR-331", "요청시작위치 값을 확인하십시오. 요청인자 중 START_INDEX를 확인하십시오.", HttpStatus.BAD_REQUEST),
    ERROR_332("ERROR-332", "요청종료위치 값을 확인하십시오. 요청인자 중 END_INDEX값을 확인하십시오.", HttpStatus.BAD_REQUEST),
    ERROR_334("ERROR-334", "종료위치보다 시작위치가 더 큽니다. 요청시작조회건수는 정수를 입력하세요.", HttpStatus.BAD_REQUEST),
    ERROR_336("ERROR-336", "데이터요청은 한번에 최대 1000건을 넘을 수 없습니다.", HttpStatus.BAD_REQUEST),
    ERROR_500("ERROR-500", "서버오류입니다.", HttpStatus.BAD_GATEWAY),
    ERROR_601("ERROR-601", "SQL 문장 오류입니다.", HttpStatus.BAD_GATEWAY),
    INFO_200("INFO-200", "해당하는 데이터가 없습니다.", HttpStatus.NOT_FOUND),
    INFO_300("INFO-300", "유효 호출건수를 이미 초과하셨습니다.", HttpStatus.TOO_MANY_REQUESTS),
    INFO_400("INFO-400", "권한이 없습니다. 관리자에게 문의하십시오.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus status;

    public static Optional<FoodSafetyKoreaApiMessageCode> findByCode(String code) {
        return Arrays.stream(values())
                .filter(messageCode -> messageCode.code.equals(code))
                .findFirst();
    }
}
