package com.likelion.routineeatbe.global.dto.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GeminiInteractionReqDto(
        String model,
        String input,
        Boolean store,
        List<GeminiFunctionDeclarationDto> tools,
        @JsonProperty("generation_config") GenerationConfig generationConfig
) {

    public static GeminiInteractionReqDto create(
            String model,
            String input,
            GeminiFunctionDeclarationDto functionDeclaration
    ) {
        return new GeminiInteractionReqDto(
                model,
                input,
                false,
                List.of(functionDeclaration),
                GenerationConfig.create(functionDeclaration.name())
        );
    }

    public record GenerationConfig(
            @JsonProperty("tool_choice") ToolChoice toolChoice
    ) {

        public static GenerationConfig create(String functionName) {
            return new GenerationConfig(ToolChoice.create(functionName));
        }
    }

    public record ToolChoice(
            @JsonProperty("allowed_tools") AllowedTools allowedTools
    ) {

        public static ToolChoice create(String functionName) {
            return new ToolChoice(AllowedTools.create(functionName));
        }
    }

    public record AllowedTools(
            String mode,
            List<String> tools
    ) {

        public static AllowedTools create(String functionName) {
            return new AllowedTools("any", List.of(functionName));
        }
    }
}
