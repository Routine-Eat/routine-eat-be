package com.likelion.routineeatbe.domain.user.exception;

import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserCookingEquipmentErrorCode implements BaseErrorCode {
    NOT_EXIST_COOKINGEQUIPMENT("USERCOOKINGEQUIPMENT4041", "존재하지 않는 조리도구가 있습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
