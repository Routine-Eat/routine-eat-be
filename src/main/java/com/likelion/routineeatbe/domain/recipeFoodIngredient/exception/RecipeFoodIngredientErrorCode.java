package com.likelion.routineeatbe.domain.recipeFoodIngredient.exception;

import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecipeFoodIngredientErrorCode implements BaseErrorCode {

    FOOD_INGREDIENT_DATA_EMPTY(
            "RFI001",
            "음식 재료 기준 데이터가 존재하지 않습니다.",
            HttpStatus.CONFLICT
    ),
    MENU_NOT_FOUND(
            "RFI002",
            "음식 재료 필요량을 저장할 메뉴를 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),
    FOOD_INGREDIENT_NOT_FOUND(
            "RFI003",
            "음식 재료 필요량에 해당하는 음식 재료를 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),
    ALREADY_INITIALIZED(
            "RFI004",
            "이미 초기화된 메뉴 음식 재료 데이터가 포함되어 있습니다.",
            HttpStatus.CONFLICT
    ),
    BASIC_RECIPE_NOT_FOUND(
            "RFI005",
            "음식 재료 필요량을 저장할 기본 레시피를 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    );

    private final String code;
    private final String message;
    private final HttpStatus status;
}
