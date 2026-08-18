package com.likelion.routineeatbe.domain.cookingRecord.dto.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.likelion.routineeatbe.global.dto.gemini.GeminiFunctionDeclaration;
import java.util.List;
import java.util.Map;

public record CookingAiGeminiFunctionDeclarationDto(
        String type,
        String name,
        String description,
        Parameters parameters
) implements GeminiFunctionDeclaration {

    public static final String MOVE_NEXT_FUNCTION_NAME = "move_next_cooking_step";
    public static final String MOVE_PREVIOUS_FUNCTION_NAME = "move_previous_cooking_step";
    public static final String MOVE_TO_FUNCTION_NAME = "move_to_cooking_step";
    public static final String REQUEST_CONTEXT_FUNCTION_NAME = "request_cooking_context";
    public static final String RETURN_ANSWER_FUNCTION_NAME = "return_cooking_answer";

    public static List<GeminiFunctionDeclaration> createCommandTools() {
        Parameters emptyParameters = new Parameters("object", Map.of(), List.of());
        return List.of(
                new CookingAiGeminiFunctionDeclarationDto(
                        "function",
                        MOVE_NEXT_FUNCTION_NAME,
                        "사용자가 다음 요리 단계로 이동하거나 현재 단계를 완료했다고 말한 경우 호출합니다.",
                        emptyParameters
                ),
                new CookingAiGeminiFunctionDeclarationDto(
                        "function",
                        MOVE_PREVIOUS_FUNCTION_NAME,
                        "사용자가 이전 요리 단계로 돌아가거나 현재 단계를 아직 완료하지 못했다고 말한 경우 호출합니다.",
                        emptyParameters
                ),
                new CookingAiGeminiFunctionDeclarationDto(
                        "function",
                        MOVE_TO_FUNCTION_NAME,
                        "사용자가 특정 번호의 요리 단계로 이동해 달라고 말한 경우 호출합니다.",
                        new Parameters(
                                "object",
                                Map.of("level", Property.createInteger(
                                        "이동할 요리 단계 번호",
                                        1
                                )),
                                List.of("level")
                        )
                ),
                new CookingAiGeminiFunctionDeclarationDto(
                        "function",
                        REQUEST_CONTEXT_FUNCTION_NAME,
                        "시스템 단계 이동 명령이 아닌 질문이나 일반 발화인 경우 호출합니다.",
                        emptyParameters
                )
        );
    }

    public static CookingAiGeminiFunctionDeclarationDto createAnswerTool() {
        return new CookingAiGeminiFunctionDeclarationDto(
                "function",
                RETURN_ANSWER_FUNCTION_NAME,
                "현재 요리와의 관련 여부와 사용자에게 전달할 최종 답변을 반환합니다.",
                new Parameters(
                        "object",
                        Map.of(
                                "cookingRelated",
                                Property.createBoolean("현재 진행 중인 요리와 관련된 발화 여부"),
                                "answer",
                                Property.createString("사용자에게 전달할 짧은 한국어 답변")
                        ),
                        List.of("cookingRelated", "answer")
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
            Number minimum
    ) {

        public static Property createString(String description) {
            return new Property("string", description, null);
        }

        public static Property createBoolean(String description) {
            return new Property("boolean", description, null);
        }

        public static Property createInteger(String description, Integer minimum) {
            return new Property("integer", description, minimum);
        }
    }
}
