package com.likelion.routineeatbe.domain.cookingRecord.dto.gemini;

/**
 * Gemini가 생성한 요리 결과 한 줄 팁 응답 DTO입니다.
 *
 * @param cookingTip 실제 요리 단계를 바탕으로 생성한 한 줄 팁
 */
public record CookingTipGenerateGeminiResponseDto(
        String cookingTip
) {

    public static CookingTipGenerateGeminiResponseDto create(String cookingTip) {
        return new CookingTipGenerateGeminiResponseDto(cookingTip);
    }
}
