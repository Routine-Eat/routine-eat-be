package com.likelion.routineeatbe.domain.user.exception;

import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserFoodIngredientErrorCode implements BaseErrorCode {
    NOT_EXIST_USER("USERFOODINGREDIENT4041", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    NOT_EXIST_FOODINGREDIENT("USERFOODINGREDIENT4042", "존재하지 않는 식재료가 있습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
