package com.likelion.routineeatbe.domain.menu.dto.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.global.dto.gemini.GeminiFunctionDeclaration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public record MenuAndRecipeGeminiFunctionDeclarationDto(
        String type,
        String name,
        String description,
        Parameters parameters
) implements GeminiFunctionDeclaration {

    public static final String FUNCTION_NAME = "create_menu_metadata_batch";

    /**
     * 메뉴 메타데이터 생성을 위한 Gemini Function Declaration을 생성합니다.
     *
     * @param batchSize Function 응답에서 허용할 메뉴 순번의 최댓값
     * @return 메뉴 종류와 예상 조리시간 스키마가 정의된 Function Declaration
     */
    public static MenuAndRecipeGeminiFunctionDeclarationDto create(int batchSize) {
        List<String> menuTypes = Arrays.stream(MenuType.values())
                .map(Enum::name)
                .toList();
        List<String> recommendationTypes = Arrays.stream(RecommendationType.values())
                .map(Enum::name)
                .toList();

        return new MenuAndRecipeGeminiFunctionDeclarationDto(
                "function",
                FUNCTION_NAME,
                "여러 메뉴의 정보와 조리 단계를 분석하여 메뉴별 종류, 추천 유형, 예상 조리시간을 생성합니다.",
                Parameters.create(menuTypes, recommendationTypes, batchSize)
        );
    }

    public record Parameters(
            String type,
            Map<String, Property> properties,
            List<String> required
    ) {

        public static Parameters create(
                List<String> menuTypes,
                List<String> recommendationTypes,
                int batchSize
        ) {
            return new Parameters(
                    "object",
                    Map.of(
                            "menus",
                            Property.createArray(
                                    "입력 메뉴별 메타데이터 목록",
                                    Property.createObject(
                                            "단일 메뉴의 메타데이터",
                                            Map.of(
                                                    "sequence",
                                                    Property.createInteger(
                                                            "입력 메뉴 앞에 표시된 1부터 시작하는 순번",
                                                            1,
                                                            batchSize
                                                    ),
                                                    "menuType",
                                                    Property.createString("메뉴 종류", menuTypes),
                                                    "recommendationType",
                                                    Property.createString("추천 유형", recommendationTypes),
                                                    "timeRequired",
                                                    Property.createInteger(
                                                            "24시간을 초과하는 장기 숙성, 발효, 저장, 보관 시간을 제외한 예상 조리 시간(분)",
                                                            1,
                                                            1_440
                                                    )
                                            ),
                                            List.of(
                                                    "sequence",
                                                    "menuType",
                                                    "recommendationType",
                                                    "timeRequired"
                                            )
                                    )
                            )
                    ),
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
            Integer minimum,
            Integer maximum
    ) {

        public static Property createString(String description) {
            return new Property("string", description, null, null, null, null, null, null);
        }

        public static Property createString(String description, List<String> enumValues) {
            return new Property("string", description, enumValues, null, null, null, null, null);
        }

        public static Property createInteger(String description, int minimum, int maximum) {
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
            return new Property("object", description, null, null, properties, required, null, null);
        }

        public static Property createArray(String description, Property items) {
            return new Property("array", description, null, items, null, null, null, null);
        }
    }
}
