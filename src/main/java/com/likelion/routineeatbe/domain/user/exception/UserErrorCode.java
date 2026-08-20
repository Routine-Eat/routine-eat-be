package com.likelion.routineeatbe.domain.user.exception;

import com.likelion.routineeatbe.global.enums.GlobalErrorCode;
import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
    DUPLICATE_LOGIN_NUMBER("USER4091", "이미 존재하는 로그인 번호입니다.", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
