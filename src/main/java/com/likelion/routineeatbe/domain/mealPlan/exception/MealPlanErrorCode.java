package com.likelion.routineeatbe.domain.mealPlan.exception;

import com.likelion.routineeatbe.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
/** AI 추천 과정에서만 발생할 수 있는 도메인 오류 코드입니다. */
public enum MealPlanErrorCode implements BaseErrorCode {
    // 안전 필터와 도구 필터를 통과한 서로 다른 메뉴가 세 개 미만인 경우
    NO_RECOMMENDABLE_RECIPE("MEALPLAN4041", "세 가지 식단을 구성할 만큼 추천 가능한 레시피가 없습니다.", HttpStatus.NOT_FOUND),
    // AI가 후보 이외의 메뉴, 중복 메뉴, 잘못된 식사 타입을 반환한 경우
    INVALID_AI_RECOMMENDATION("MEALPLAN5021", "AI 추천 결과가 올바르지 않습니다.", HttpStatus.BAD_GATEWAY),
    NOT_EXIST_PLAN("MEALPLAN4042","존재하지 않는 식단입니다.",HttpStatus.NOT_FOUND),
    NOT_HAVE_USER("MEALPLAN4003","해당 유저가 진행 중인 식단이 아닙니다.",HttpStatus.BAD_REQUEST);


    private final String code;
    private final String message;
    private final HttpStatus status;
}
