package com.likelion.routineeatbe.domain.cookingRecord.dto.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.likelion.routineeatbe.global.dto.gemini.GeminiFunctionDeclaration;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CookingAiInteractionReqDto(
        String model,
        Object input,
        Boolean store,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<GeminiFunctionDeclaration> tools,
        @JsonProperty("generation_config") GenerationConfig generationConfig
) {

    public static CookingAiInteractionReqDto createFunctionCall(
            String model,
            Object input,
            List<GeminiFunctionDeclaration> tools
    ) {
        List<String> allowedTools = tools.stream()
                .map(GeminiFunctionDeclaration::name)
                .toList();
        return new CookingAiInteractionReqDto(
                model,
                input,
                false,
                List.copyOf(tools),
                GenerationConfig.createFunctionCall(allowedTools)
        );
    }

    public static CookingAiInteractionReqDto createNoOutput(String model, Object input) {
        return new CookingAiInteractionReqDto(
                model,
                input,
                false,
                List.of(),
                GenerationConfig.createNoOutput()
        );
    }

    public record GenerationConfig(
            @JsonProperty("tool_choice") ToolChoice toolChoice,
            @JsonProperty("max_output_tokens") Integer maxOutputTokens,
            @JsonProperty("thinking_level") String thinkingLevel
    ) {

        public static GenerationConfig createFunctionCall(List<String> allowedTools) {
            return new GenerationConfig(
                    new ToolChoice(new AllowedTools("any", List.copyOf(allowedTools))),
                    null,
                    "minimal"
            );
        }

        public static GenerationConfig createNoOutput() {
            return new GenerationConfig(
                    new ToolChoice(new AllowedTools("none", List.of())),
                    1,
                    "minimal"
            );
        }
    }

    public record ToolChoice(
            @JsonProperty("allowed_tools") AllowedTools allowedTools
    ) {
    }

    public record AllowedTools(
            String mode,
            List<String> tools
    ) {
    }
}
