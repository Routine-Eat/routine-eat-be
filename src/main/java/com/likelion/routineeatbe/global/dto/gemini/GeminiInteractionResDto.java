package com.likelion.routineeatbe.global.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Builder;
import tools.jackson.databind.JsonNode;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiInteractionResDto(
        List<Step> steps
) {

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Step(
            String type,
            String name,
            JsonNode arguments
    ) {
    }
}
