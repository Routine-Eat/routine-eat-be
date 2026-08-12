package com.likelion.routineeatbe.domain.user.dto.response;

import com.likelion.routineeatbe.domain.cookingEquipment.dto.response.CookingEquipmentResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(title = "UserOnboardingResponse: 사용자 온보딩 데이터 응답 DTO")
public record UserOnboardingResponse(
        @Schema(description = "사용자 정보",example = """
                {
                      "userId": 3,
                      "userLoginNumber": "0000",
                      "userSkillLevel": "PRO",
                      "userCreatedAt": "2026-08-12T18:31:34.65488"
                    }
                """)
        UserResponse userResponse,

        @Schema(description = "사용자 알레르기 식재료 정보",example = """
                {
                      "userFoodIngredientType": "ALLERGY",
                      "foodIngredientList": [
                        {
                          "foodIngredientId": 1,
                          "foodIngredientName": "감자",
                          "foodIngredientType": "POTATO_AND_STARCH",
                          "foodIngredientPrimaryUnit": "G",
                          "foodIngredientSecondaryUnit": "GAE"
                        },
                        {
                          "foodIngredientId": 2,
                          "foodIngredientName": "감자전분",
                          "foodIngredientType": "POTATO_AND_STARCH",
                          "foodIngredientPrimaryUnit": "G",
                          "foodIngredientSecondaryUnit": "CUP"
                        }
                      ]
                    }
                """)
        UserFoodIngredientResponse allergyIngredientResponse,

        @Schema(description = "사용자 비선호 식재료 정보",example = """
                {
                      "userFoodIngredientType": "DISLIKE",
                      "foodIngredientList": [
                        {
                          "foodIngredientId": 3,
                          "foodIngredientName": "고구마",
                          "foodIngredientType": "POTATO_AND_STARCH",
                          "foodIngredientPrimaryUnit": "G",
                          "foodIngredientSecondaryUnit": "GAE"
                        },
                        {
                          "foodIngredientId": 4,
                          "foodIngredientName": "고구마 전분",
                          "foodIngredientType": "POTATO_AND_STARCH",
                          "foodIngredientPrimaryUnit": "G",
                          "foodIngredientSecondaryUnit": "CUP"
                        }
                      ]
                    }
                """)
        UserFoodIngredientResponse dislikeIngredientResponse,

        @Schema(description = "사용자 보유 식재료 정보",example = """
                 {
                      "userFoodIngredientType": "OWN",
                      "foodIngredientList": [
                        {
                          "foodIngredientId": 1,
                          "foodIngredientName": "감자",
                          "foodIngredientType": "POTATO_AND_STARCH",
                          "foodIngredientPrimaryUnit": "G",
                          "foodIngredientSecondaryUnit": "GAE",
                          "primaryAmountValue": 200,
                          "secondaryAmountValue": 2
                        }
                      ]
                    }
                """)
        UserFoodIngredientResponse ownIngredientResponse,

        @Schema(description = "사용자 조리환경 정보",example = """
                [
                      {
                        "cookingEquipmentId": 1,
                        "cookingEquipmentName": "전기밥솥",
                        "cookingEquipmentType": "APPLIANCE",
                        "cookingEquipmentSymbol": null
                      }
                      ]
                """)
        List<CookingEquipmentResponse> cookingEquipmentResponseList
) {
    public static UserOnboardingResponse from(
            UserResponse userResponse,
            UserFoodIngredientResponse allergyIngredientResponse,
            UserFoodIngredientResponse dislikeIngredientResponse,
            UserFoodIngredientResponse ownIngredientResponse,
            List<CookingEquipmentResponse> cookingEquipmentResponseList
    ) {
        return UserOnboardingResponse.builder()
                .userResponse(userResponse)
                .allergyIngredientResponse(allergyIngredientResponse)
                .dislikeIngredientResponse(dislikeIngredientResponse)
                .ownIngredientResponse(ownIngredientResponse)
                .cookingEquipmentResponseList(cookingEquipmentResponseList)
                .build();
    }
}