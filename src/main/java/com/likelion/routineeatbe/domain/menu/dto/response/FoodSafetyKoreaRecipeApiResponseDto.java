package com.likelion.routineeatbe.domain.menu.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;

/**
 * 식품 안전 레시피 API 호출 결과를 받는 DTO
 * @param cookRecipeData
 */
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record FoodSafetyKoreaRecipeApiResponseDto(
        @JsonProperty("COOKRCP01") CookRecipeData cookRecipeData
) {

    public static FoodSafetyKoreaRecipeApiResponseDto create(CookRecipeData cookRecipeData) {
        return FoodSafetyKoreaRecipeApiResponseDto.builder()
                .cookRecipeData(cookRecipeData)
                .build();
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CookRecipeData(
            @JsonProperty("total_count") String totalCount,
            @JsonProperty("row") List<RecipeRow> rows,
            @JsonProperty("RESULT") ApiResult result
    ) {
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiResult(
            @JsonProperty("CODE") String code,
            @JsonProperty("MSG") String message
    ) {
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RecipeRow(
            @JsonProperty("RCP_SEQ") String recipeSequence,
            @JsonProperty("RCP_NM") String menuName,
            @JsonProperty("RCP_WAY2") String cookingMethod,
            @JsonProperty("RCP_PAT2") String dishType,
            @JsonProperty("INFO_WGT") String servingWeight,
            @JsonProperty("INFO_ENG") String calories,
            @JsonProperty("INFO_CAR") String carbohydrates,
            @JsonProperty("INFO_PRO") String protein,
            @JsonProperty("INFO_FAT") String fat,
            @JsonProperty("INFO_NA") String sodium,
            @JsonProperty("HASH_TAG") String hashTag,
            @JsonProperty("ATT_FILE_NO_MAIN") String mainImageUrl,
            @JsonProperty("ATT_FILE_NO_MK") String thumbnailImageUrl,
            @JsonProperty("RCP_PARTS_DTLS") String ingredientDetails,
            @JsonProperty("MANUAL01") String manual01,
            @JsonProperty("MANUAL_IMG01") String manualImage01,
            @JsonProperty("MANUAL02") String manual02,
            @JsonProperty("MANUAL_IMG02") String manualImage02,
            @JsonProperty("MANUAL03") String manual03,
            @JsonProperty("MANUAL_IMG03") String manualImage03,
            @JsonProperty("MANUAL04") String manual04,
            @JsonProperty("MANUAL_IMG04") String manualImage04,
            @JsonProperty("MANUAL05") String manual05,
            @JsonProperty("MANUAL_IMG05") String manualImage05,
            @JsonProperty("MANUAL06") String manual06,
            @JsonProperty("MANUAL_IMG06") String manualImage06,
            @JsonProperty("MANUAL07") String manual07,
            @JsonProperty("MANUAL_IMG07") String manualImage07,
            @JsonProperty("MANUAL08") String manual08,
            @JsonProperty("MANUAL_IMG08") String manualImage08,
            @JsonProperty("MANUAL09") String manual09,
            @JsonProperty("MANUAL_IMG09") String manualImage09,
            @JsonProperty("MANUAL10") String manual10,
            @JsonProperty("MANUAL_IMG10") String manualImage10,
            @JsonProperty("MANUAL11") String manual11,
            @JsonProperty("MANUAL_IMG11") String manualImage11,
            @JsonProperty("MANUAL12") String manual12,
            @JsonProperty("MANUAL_IMG12") String manualImage12,
            @JsonProperty("MANUAL13") String manual13,
            @JsonProperty("MANUAL_IMG13") String manualImage13,
            @JsonProperty("MANUAL14") String manual14,
            @JsonProperty("MANUAL_IMG14") String manualImage14,
            @JsonProperty("MANUAL15") String manual15,
            @JsonProperty("MANUAL_IMG15") String manualImage15,
            @JsonProperty("MANUAL16") String manual16,
            @JsonProperty("MANUAL_IMG16") String manualImage16,
            @JsonProperty("MANUAL17") String manual17,
            @JsonProperty("MANUAL_IMG17") String manualImage17,
            @JsonProperty("MANUAL18") String manual18,
            @JsonProperty("MANUAL_IMG18") String manualImage18,
            @JsonProperty("MANUAL19") String manual19,
            @JsonProperty("MANUAL_IMG19") String manualImage19,
            @JsonProperty("MANUAL20") String manual20,
            @JsonProperty("MANUAL_IMG20") String manualImage20,
            @JsonProperty("RCP_NA_TIP") String lowSodiumTip
    ) {
    }
}
