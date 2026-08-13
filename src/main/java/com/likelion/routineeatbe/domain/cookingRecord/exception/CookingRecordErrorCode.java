package com.likelion.routineeatbe.domain.cookingRecord.exception;

import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CookingRecordErrorCode implements BaseErrorCode {

    USER_NOT_FOUND("CR4041", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    RECIPE_NOT_FOUND("CR4042", "존재하지 않는 레시피입니다.", HttpStatus.NOT_FOUND),
    RECIPE_FOOD_INGREDIENT_EMPTY(
            "CR4091",
            "레시피에 등록된 음식 재료가 없습니다.",
            HttpStatus.CONFLICT
    ),
    RECIPE_STEP_EMPTY("CR4092", "레시피에 등록된 요리 단계가 없습니다.", HttpStatus.CONFLICT),
    COOKING_ALREADY_STARTED(
            "CR4093",
            "이미 진행 중이거나 완료된 요리 기록이 있습니다.",
            HttpStatus.CONFLICT
    );

    private final String code;
    private final String message;
    private final HttpStatus status;
}
