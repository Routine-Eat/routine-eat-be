package com.likelion.routineeatbe.global.dto.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GeminiInteractionReqDto(
        String model,
        String input,
        Boolean store,
        List<GeminiFunctionDeclaration> tools,
        @JsonProperty("generation_config") GenerationConfig generationConfig
) {

    public static GeminiInteractionReqDto create(
            String model,
            String input,
            GeminiFunctionDeclaration functionDeclaration
    ) {
        return create(model, input, List.of(functionDeclaration), List.of(functionDeclaration.name()));
    }

    public static GeminiInteractionReqDto create(
            String model,
            String input,
            List<GeminiFunctionDeclaration> tools,
            List<String> allowedToolNames
    ) {
        return new GeminiInteractionReqDto(
                model,
                input,
                false,
                List.copyOf(tools),
                GenerationConfig.create(allowedToolNames)
        );
    }

    public record GenerationConfig(
            @JsonProperty("tool_choice") ToolChoice toolChoice
    ) {

        public static GenerationConfig create(List<String> allowedToolNames) {
            return new GenerationConfig(ToolChoice.create(allowedToolNames));
        }
    }

    public record ToolChoice(
            @JsonProperty("allowed_tools") AllowedTools allowedTools
    ) {

        public static ToolChoice create(List<String> allowedToolNames) {
            return new ToolChoice(AllowedTools.create(allowedToolNames));
        }
    }

    public record AllowedTools(
            String mode,
            List<String> tools
    ) {

        public static AllowedTools create(List<String> allowedToolNames) {
            return new AllowedTools("any", List.copyOf(allowedToolNames));
        }
    }
}
