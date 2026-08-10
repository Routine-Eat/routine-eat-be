package com.likelion.routineeatbe.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(title = "UserFoodIngredientResponse: 사용자-식재료 관계 응답 DTO")
@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드는 JSON 응답에서 자동 제외
public record UserFoodIngredientResponse(
        UserFoodIngredientType userFoodIngredientType,
        List<UserFoodIngredientDto> foodIngredientList
) {
    // 전체 응답 DTO 포장 함수
    // foodIngredientList는 UserFoodIngredientDto에서 포장
    public static UserFoodIngredientResponse of(UserFoodIngredientType type, List<UserFoodIngredient> userFoodIngredients) {
        // type이 null(전체 조회)일 때만 항목별 relationType을 포함하도록 flag 설정
        boolean includeRelationTypeInItems = (type == null);

        List<UserFoodIngredientDto> dtos = userFoodIngredients.stream()
                .map(entity -> UserFoodIngredientDto.from(entity, includeRelationTypeInItems))
                .toList();

        return new UserFoodIngredientResponse(type, dtos);
    }

    // 내부 식재료 DTO
    // userFoodIngredient에 있는 foodIngredient 데이터 뽑기
    // 필요한 데이터로만 구성
    // 보조 단위는 없는 경우 대비 예외처리
    @JsonInclude(JsonInclude.Include.NON_NULL) //보유량 없어서 null 나오면 응답에서 제거
    public record UserFoodIngredientDto(
            Long foodIngredientId,
            String foodIngredientName,
            String foodIngredientType,
            UserFoodIngredientType relationType,
            String foodIngredientPrimaryUnit,
            String foodIngredientSecondaryUnit,
            Double primaryAmountValue,
            Double secondaryAmountValue
    ) {
        public static UserFoodIngredientDto from(UserFoodIngredient userFoodIngredient,boolean includeRelationType) {
            FoodIngredient foodIngredient = userFoodIngredient.getFoodIngredient();

            return new UserFoodIngredientDto(
                    foodIngredient.getId(),
                    foodIngredient.getName(),
                    foodIngredient.getType().name(),
                    includeRelationType ? userFoodIngredient.getRelationType() : null,
                    foodIngredient.getPrimaryUnit().name(),
                    foodIngredient.getSecondaryUnit() != null ? foodIngredient.getSecondaryUnit().name() : null,
                    userFoodIngredient.getPrimaryAmountValue(),
                    userFoodIngredient.getSecondaryAmountValue()
            );
        }
    }
}