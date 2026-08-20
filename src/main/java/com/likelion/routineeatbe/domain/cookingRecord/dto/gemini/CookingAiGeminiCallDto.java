package com.likelion.routineeatbe.domain.cookingRecord.dto.gemini;

import lombok.Builder;
import tools.jackson.databind.JsonNode;

@Builder
public record CookingAiGeminiCallDto(
        String initialPrompt,
        JsonNode interactionResponse,
        String callId,
        String functionName,
        JsonNode arguments
) {

    public static CookingAiGeminiCallDto create(
            String initialPrompt,
            JsonNode interactionResponse,
            String callId,
            String functionName,
            JsonNode arguments
    ) {
        return CookingAiGeminiCallDto.builder()
                .initialPrompt(initialPrompt)
                .interactionResponse(interactionResponse)
                .callId(callId)
                .functionName(functionName)
                .arguments(arguments)
                .build();
    }
}
