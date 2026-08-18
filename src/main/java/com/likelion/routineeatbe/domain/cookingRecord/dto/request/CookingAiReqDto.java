package com.likelion.routineeatbe.domain.cookingRecord.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CookingAiReqDto(
        @NotBlank(message = "사용자 발화는 필수입니다.")
        @Size(max = 1000, message = "사용자 발화는 1000자 이하여야 합니다.")
        String userSpeechText
) {
}
