package com.likelion.routineeatbe.domain.mealPlan.exception;

import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PlanMenuErrorCode implements BaseErrorCode {
    NOT_EXIST_PLAN_MENU("PLANMENU4041","존재하지 않는 식단 메뉴입니다.", HttpStatus.NOT_FOUND),
    NOT_PROGRESS_PLAN("PLANMENU4001","진행중이지 않은 식단입니다.",HttpStatus.BAD_REQUEST),
    NOT_HAVE_USER("PLANMENU4002","해당 유저가 진행 중인 식단이 아닙니다.",HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
