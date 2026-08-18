package com.likelion.routineeatbe.domain.cookingRecord.dto;

import lombok.Builder;

@Builder
public record CookingAiResult(
        String message,
        Object data,
        byte[] audio
) {

    public static CookingAiResult create(String message, Object data, byte[] audio) {
        return CookingAiResult.builder()
                .message(message)
                .data(data)
                .audio(audio == null ? null : audio.clone())
                .build();
    }

    @Override
    public byte[] audio() {
        return audio == null ? null : audio.clone();
    }
}
