package com.likelion.routineeatbe.domain.menu.dto.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.likelion.routineeatbe.global.dto.gemini.GeminiFunctionDeclaration;
import java.util.List;
import java.util.Map;

public record InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto(
        String type,
        String name,
        String description,
        Parameters parameters
) implements GeminiFunctionDeclaration {

    public static final String FUNCTION_NAME = "initialize_menu_recipe_food_ingredients";

    public static InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto create(int batchSize) {
        return new InitMenuAndRecipeFoodIngredientGeminiFunctionDeclarationDto(
                "function",
                FUNCTION_NAME,
                "메뉴별로 1인분 조리에 필요한 음식 재료와 주 사용량 및 보조 사용량을 생성합니다.",
                Parameters.create(batchSize)
        );
    }

    public record Parameters(
            String type,
            Map<String, Property> properties,
            List<String> required
    ) {

        public static Parameters create(int batchSize) {
            Property foodIngredient = Property.createObject(
                    "메뉴에 필요한 단일 음식 재료의 1인분 사용량",
                    Map.of(
                            "foodIngredientId",
                            Property.createInteger("입력으로 제공된 음식 재료 식별자", 1, null),
                            "primaryNeedAmountValue",
                            Property.createNumber("주 단위 기준 1인분 필요량", 0.000_001, null),
                            "secondaryNeedAmountValue",
                            Property.createNumber("보조 단위 기준 1인분 필요량. 추정할 수 없으면 생략", 0.000_001, null)
                    ),
                    List.of("foodIngredientId", "primaryNeedAmountValue")
            );

            Property menu = Property.createObject(
                    "단일 메뉴의 1인분 음식 재료 필요량 목록",
                    Map.of(
                            "sequence",
                            Property.createInteger("입력 메뉴 앞에 표시된 1부터 시작하는 순번", 1, batchSize),
                            "foodIngredients",
                            Property.createArray(
                                    "메뉴에 필요한 음식 재료 목록. 동일한 foodIngredientId는 사용량을 합산하여 한 번만 포함",
                                    foodIngredient
                            )
                    ),
                    List.of("sequence", "foodIngredients")
            );

            return new Parameters(
                    "object",
                    Map.of("menus", Property.createArray("입력 메뉴별 결과 목록", menu)),
                    List.of("menus")
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

        public static Property createNumber(
                String description,
                Double minimum,
                Double maximum
        ) {
            return new Property(
                    "number",
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
            return new Property("object", description, null, null, properties, required, null, null);
        }

        public static Property createArray(String description, Property items) {
            return new Property("array", description, null, items, null, null, null, null);
        }
    }
}
