package com.likelion.routineeatbe.domain.recipe.exception;

import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecipeErrorCode implements BaseErrorCode {
    USER_NOT_FOUND("RECIPE4041", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    RECIPE_NOT_FOUND("RECIPE4042", "존재하지 않는 레시피입니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
