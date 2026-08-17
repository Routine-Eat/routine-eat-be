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
    ),
    COOKING_RECORD_NOT_FOUND("CR4043", "요리 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COOKING_SESSION_NOT_FOUND("CR4044", "요리 세션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COOKING_STEP_NOT_FOUND("CR4045", "요리 단계를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COOKING_RECORD_FOOD_INGREDIENT_NOT_FOUND(
            "CR4046",
            "요리 기록 음식 재료를 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),
    COOKING_TIP_NOT_FOUND("CR4047", "요리 팁을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COOKING_SESSION_NOT_IN_PROGRESS(
            "CR4094",
            "진행 중인 요리 세션이 아닙니다.",
            HttpStatus.CONFLICT
    ),
    INVALID_COOKING_STEP_STATE(
            "CR4095",
            "요리 단계 상태가 올바르지 않습니다.",
            HttpStatus.CONFLICT
    ),
    COMPLETED_COOKING_RECORD_NOT_FOUND(
            "CR4096",
            "회고를 저장할 완료된 요리 기록이 없습니다.",
            HttpStatus.CONFLICT
    ),
    COOKING_SESSION_NOT_COMPLETED(
            "CR4097",
            "완료된 요리 세션이 아닙니다.",
            HttpStatus.CONFLICT
    ),
    COOKING_RECORD_FOOD_INGREDIENT_EMPTY(
            "CR4098",
            "요리 기록에 저장된 음식 재료가 없습니다.",
            HttpStatus.CONFLICT
    ),
    COOKING_TIP_EMPTY("CR4099", "등록된 요리 팁이 없습니다.", HttpStatus.CONFLICT),
    INVALID_COOKING_RECORD_IMAGE(
            "CR4001",
            "요리 결과 이미지는 올바른 이미지 파일이어야 합니다.",
            HttpStatus.BAD_REQUEST
    ),
    DUPLICATE_COOKING_RECORD_FOOD_INGREDIENT(
            "CR4002",
            "중복된 요리 기록 음식 재료가 요청되었습니다.",
            HttpStatus.BAD_REQUEST
    ),
    INVALID_COOKING_STEP_LEVEL(
            "CR4003",
            "이동할 요리 단계 번호가 올바르지 않습니다.",
            HttpStatus.BAD_REQUEST
    ),
    COOKING_RECORD_IMAGE_UPLOAD_FAILED(
            "CR5021",
            "요리 결과 이미지 업로드에 실패했습니다.",
            HttpStatus.BAD_GATEWAY
    );

    private final String code;
    private final String message;
    private final HttpStatus status;
}
