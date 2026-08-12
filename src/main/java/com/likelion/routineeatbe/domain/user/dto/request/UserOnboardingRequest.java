package com.likelion.routineeatbe.domain.user.dto.request;

import com.likelion.routineeatbe.domain.user.entity.SkillLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(title = "UserOnboardingRequest: 사용자 온보딩 데이터 저장 요청 DTO")
public record UserOnboardingRequest(
        @NotNull(message = "요리 실력 입력은 필수 입니다.")
        @Schema(description = "사용자 요리 실력",example = "PRO")
        SkillLevel skillLevel,

        @Schema(description = "사용자 알레르기 음식 id 리스트",example = """
                {
                    "relationType": "ALLERGY",
                    "foodIngredientList": [
                      {
                        "foodIngredientId": 1
                      },
                      {
                        "foodIngredientId": 2
                      }
                    ]
                  }
                """)
        CreateUserFoodIngredientRequest allergyIngredientList,

        @Schema(description = "사용자 비선호 음식 id 리스트",example = """
                {
                    "relationType": "DISLIKE",
                    "foodIngredientList": [
                      {
                        "foodIngredientId": 3
                      },
                      {
                        "foodIngredientId": 4
                      }
                    ]
                  }
                """)
        CreateUserFoodIngredientRequest dislikeIngredientList,

        @Schema(description = "사용자 보유 음식 id 리스트",example = """
                {
                    "relationType": "OWN",
                    "foodIngredientList": [
                      {
                        "foodIngredientId": 1,
                        "primaryAmountValue": 200,
                        "secondaryAmountValue": 2
                      }
                    ]
                  }
                """)
        CreateUserFoodIngredientRequest ownIngredientList,

        @Schema(description = "사용자 조리도구 id 리스트",example = "[1,2,3]")
        List<Long> cookingEquipmentIdList
) {
}
