package com.likelion.routineeatbe.domain.cookingTip.exception;

import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CookingTipErrorCode implements BaseErrorCode {

    COOKING_TIP_INITIALIZATION_FAILED(
            "CT001",
            "요리 팁 데이터 초기화에 실패했습니다.",
            HttpStatus.INTERNAL_SERVER_ERROR
    );

    private final String code;
    private final String message;
    private final HttpStatus status;
}
