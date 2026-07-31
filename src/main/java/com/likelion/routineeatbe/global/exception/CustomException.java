package com.likelion.routineeatbe.global.exception;
import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public CustomException(BaseErrorCode errorCode) {

        super(errorCode.getMessage());
        this.errorCode=errorCode;
    }
}

