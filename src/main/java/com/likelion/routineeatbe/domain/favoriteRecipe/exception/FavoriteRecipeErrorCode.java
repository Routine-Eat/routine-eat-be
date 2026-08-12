package com.likelion.routineeatbe.domain.favoriteRecipe.exception;

import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FavoriteRecipeErrorCode implements BaseErrorCode {

    USER_NOT_FOUND("FAVORITE4041", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    RECIPE_NOT_FOUND("FAVORITE4042", "존재하지 않는 레시피입니다.", HttpStatus.NOT_FOUND),
    FAVORITE_RECIPE_ALREADY_EXISTS(
            "FAVORITE4091",
            "이미 찜한 레시피입니다.",
            HttpStatus.CONFLICT
    );

    private final String code;
    private final String message;
    private final HttpStatus status;
}
