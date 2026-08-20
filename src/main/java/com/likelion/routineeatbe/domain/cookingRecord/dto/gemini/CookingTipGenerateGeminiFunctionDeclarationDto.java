package com.likelion.routineeatbe.domain.cookingRecord.dto.gemini;

import com.likelion.routineeatbe.global.dto.gemini.GeminiFunctionDeclaration;
import java.util.List;
import java.util.Map;

public record CookingTipGenerateGeminiFunctionDeclarationDto(
        String type,
        String name,
        String description,
        Parameters parameters
) implements GeminiFunctionDeclaration {

    public static final String FUNCTION_NAME = "generate_cooking_tip";

    public static CookingTipGenerateGeminiFunctionDeclarationDto create() {
        return new CookingTipGenerateGeminiFunctionDeclarationDto(
                "function",
                FUNCTION_NAME,
                "완료된 요리 단계를 바탕으로 사용자에게 도움이 되는 한 줄 요리 팁을 생성합니다.",
                new Parameters(
                        "object",
                        Map.of("cookingTip", new Property("string", "생성한 한 줄 요리 팁")),
                        List.of("cookingTip")
                )
        );
    }

    public record Parameters(
            String type,
            Map<String, Property> properties,
            List<String> required
    ) {
    }

    public record Property(
            String type,
            String description
    ) {
    }
}
