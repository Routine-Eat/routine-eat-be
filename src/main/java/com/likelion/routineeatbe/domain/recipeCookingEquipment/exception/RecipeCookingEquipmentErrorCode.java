package com.likelion.routineeatbe.domain.recipeCookingEquipment.exception;

import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecipeCookingEquipmentErrorCode implements BaseErrorCode {

    COOKING_EQUIPMENT_DATA_EMPTY(
            "RCE001",
            "조리 도구 기준 데이터가 존재하지 않습니다.",
            HttpStatus.CONFLICT
    ),
    RECIPE_NOT_FOUND(
            "RCE002",
            "조리 도구를 저장할 레시피를 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),
    COOKING_EQUIPMENT_NOT_FOUND(
            "RCE003",
            "레시피 조리 도구 데이터에 해당하는 조리 도구를 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),
    ALREADY_INITIALIZED(
            "RCE004",
            "이미 초기화된 레시피 조리 도구 데이터가 포함되어 있습니다.",
            HttpStatus.CONFLICT
    );

    private final String code;
    private final String message;
    private final HttpStatus status;
}
