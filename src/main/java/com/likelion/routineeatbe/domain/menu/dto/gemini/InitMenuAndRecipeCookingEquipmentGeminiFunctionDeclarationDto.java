package com.likelion.routineeatbe.domain.menu.dto.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.likelion.routineeatbe.global.dto.gemini.GeminiFunctionDeclaration;
import java.util.List;
import java.util.Map;

public record InitMenuAndRecipeCookingEquipmentGeminiFunctionDeclarationDto(
        String type,
        String name,
        String description,
        Parameters parameters
) implements GeminiFunctionDeclaration {

    public static final String FUNCTION_NAME = "initialize_menu_recipe_cooking_equipments";

    public static InitMenuAndRecipeCookingEquipmentGeminiFunctionDeclarationDto create(
            int batchSize
    ) {
        return new InitMenuAndRecipeCookingEquipmentGeminiFunctionDeclarationDto(
                "function",
                FUNCTION_NAME,
                "레시피별 조리에 필요한 조리 도구 식별자 목록을 생성합니다.",
                Parameters.create(batchSize)
        );
    }

    public record Parameters(
            String type,
            Map<String, Property> properties,
            List<String> required
    ) {

        public static Parameters create(int batchSize) {
            Property recipe = Property.createObject(
                    "단일 레시피에 필요한 조리 도구 목록",
                    Map.of(
                            "sequence",
                            Property.createInteger(
                                    "입력 레시피 앞에 표시된 1부터 시작하는 순번",
                                    1,
                                    batchSize
                            ),
                            "cookingEquipmentIds",
                            Property.createArray(
                                    "입력으로 제공된 조리 도구 식별자 목록. 동일한 식별자는 한 번만 포함",
                                    Property.createInteger("조리 도구 식별자", 1, null)
                            )
                    ),
                    List.of("sequence", "cookingEquipmentIds")
            );

            return new Parameters(
                    "object",
                    Map.of("recipes", Property.createArray("입력 레시피별 결과 목록", recipe)),
                    List.of("recipes")
            );
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Property(
            String type,
            String description,
            @JsonProperty("enum") List<String> enumValues,
            Property items,
            Map<String, Property> properties,
            List<String> required,
            Number minimum,
            Number maximum
    ) {

        public static Property createInteger(
                String description,
                Integer minimum,
                Integer maximum
        ) {
            return new Property(
                    "integer",
                    description,
                    null,
                    null,
                    null,
                    null,
                    minimum,
                    maximum
            );
        }

        public static Property createObject(
                String description,
                Map<String, Property> properties,
                List<String> required
        ) {
            return new Property(
                    "object",
                    description,
                    null,
                    null,
                    properties,
                    required,
                    null,
                    null
            );
        }

        public static Property createArray(String description, Property items) {
            return new Property(
                    "array",
                    description,
                    null,
                    items,
                    null,
                    null,
                    null,
                    null
            );
        }
    }
}
