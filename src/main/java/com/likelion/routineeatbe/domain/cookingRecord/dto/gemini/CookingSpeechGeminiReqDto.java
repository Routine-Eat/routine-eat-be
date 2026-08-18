package com.likelion.routineeatbe.domain.cookingRecord.dto.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CookingSpeechGeminiReqDto(
        String model,
        String input,
        Boolean store,
        @JsonProperty("response_format") ResponseFormat responseFormat,
        @JsonProperty("generation_config") GenerationConfig generationConfig
) {

    public static CookingSpeechGeminiReqDto create(
            String model,
            String input,
            String voice
    ) {
        return new CookingSpeechGeminiReqDto(
                model,
                input,
                false,
                new ResponseFormat("audio"),
                new GenerationConfig(List.of(new SpeechConfig(voice)))
        );
    }

    public record ResponseFormat(String type) {
    }

    public record GenerationConfig(
            @JsonProperty("speech_config") List<SpeechConfig> speechConfig
    ) {
    }

    public record SpeechConfig(String voice) {
    }
}
