package com.likelion.routineeatbe.domain.cookingRecord.dto.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingStepStage;
import com.likelion.routineeatbe.global.dto.gemini.GeminiFunctionDeclaration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public record CookingStepGenerateGeminiFunctionDeclarationDto(
        String type,
        String name,
        String description,
        Parameters parameters
) implements GeminiFunctionDeclaration {

    public static final String FUNCTION_NAME = "generate_cooking_steps";

    public static CookingStepGenerateGeminiFunctionDeclarationDto create() {
        Property cookingStep = Property.createObject(
                "단일 요리 단계",
                Map.of(
                        "level", Property.createInteger("1부터 시작하는 요리 단계 번호", 1),
                        "stage", Property.createEnum(
                                "요리 단계 구분",
                                Arrays.stream(CookingStepStage.values()).map(Enum::name).toList()
                        ),
                        "title", Property.createString("요리 단계 제목"),
                        "content", Property.createString("요리 단계 설명"),
                        "subContent", Property.createString("초보자를 위한 부연 설명")
                ),
                List.of("level", "stage", "title", "content")
        );
        return new CookingStepGenerateGeminiFunctionDeclarationDto(
                "function",
                FUNCTION_NAME,
                "요리 시작 전 체크리스트와 사용자 맞춤 요리 단계를 생성합니다.",
                new Parameters(
                        "object",
                        Map.of(
                                "checkListBeforeStart",
                                Property.createArray(
                                        "요리 시작 전 확인해야 하는 체크리스트",
                                        Property.createString("체크리스트 항목")
                                ),
                                "cookingSteps",
                                Property.createArray("순서대로 생성된 요리 단계", cookingStep)
                        ),
                        List.of("checkListBeforeStart", "cookingSteps")
                )
        );
    }

    public record Parameters(
            String type,
            Map<String, Property> properties,
            List<String> required
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Property(
            String type,
            String description,
            @JsonProperty("enum") List<String> enumValues,
            Property items,
            Map<String, Property> properties,
            List<String> required,
            Number minimum
    ) {

        public static Property createString(String description) {
            return new Property("string", description, null, null, null, null, null);
        }

        public static Property createInteger(String description, Integer minimum) {
            return new Property("integer", description, null, null, null, null, minimum);
        }

        public static Property createEnum(String description, List<String> enumValues) {
            return new Property("string", description, enumValues, null, null, null, null);
        }

        public static Property createArray(String description, Property items) {
            return new Property("array", description, null, items, null, null, null);
        }

        public static Property createObject(
                String description,
                Map<String, Property> properties,
                List<String> required
        ) {
            return new Property("object", description, null, null, properties, required, null);
        }
    }
}
