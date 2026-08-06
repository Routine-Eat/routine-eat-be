package com.likelion.routineeatbe.domain.menu.dto.response;

import java.util.List;
import java.util.stream.Stream;
import lombok.Builder;
import org.springframework.util.StringUtils;

/**
 * DB에 저장할 때 필요한 데이터들만 담는 DTO
 */
@Builder
public record MenuAndRecipeCrawlingDto(
        String menuName,
        Double calories,
        String ingredientDetails,
        List<RecipeRow> recipes
) {
    /**
     * FoodSafetyKoreaRecipeApiResponseDto를 List<MenuAndRecipeCrawlingDto>로 변환하는 static method
     * @param responseDto
     * @return
     */
    public static List<MenuAndRecipeCrawlingDto> fromFoodSafetyKoreaRecipeApiResponseDtoToList(FoodSafetyKoreaRecipeApiResponseDto responseDto) {
        List<FoodSafetyKoreaRecipeApiResponseDto.RecipeRow> rows = responseDto.cookRecipeData().rows();
        return rows.stream().map(MenuAndRecipeCrawlingDto::fromRecipeRow).toList();
    }


    private static MenuAndRecipeCrawlingDto fromRecipeRow(FoodSafetyKoreaRecipeApiResponseDto.RecipeRow row) {
        return MenuAndRecipeCrawlingDto.builder()
                .menuName(row.menuName())
                .calories(Double.parseDouble(row.calories()))
                .ingredientDetails(row.ingredientDetails())
                .recipes(MenuAndRecipeCrawlingDto.createRecipeRowList(row))
                .build();
    }

    public static List<MenuAndRecipeCrawlingDto.RecipeRow> createRecipeRowList(FoodSafetyKoreaRecipeApiResponseDto.RecipeRow row) {
        // 매뉴얼 1~20까지의 반환값으로 RecipeRow 객체를 20개 만든다. 이후 RecipeRow::hasContent 함수를 이용해서 값이 존재하는 것만 남기고 List로 정리한다.
        return Stream.of(
                        RecipeRow.create(row.manual01(), row.manualImage01()),
                        RecipeRow.create(row.manual02(), row.manualImage02()),
                        RecipeRow.create(row.manual03(), row.manualImage03()),
                        RecipeRow.create(row.manual04(), row.manualImage04()),
                        RecipeRow.create(row.manual05(), row.manualImage05()),
                        RecipeRow.create(row.manual06(), row.manualImage06()),
                        RecipeRow.create(row.manual07(), row.manualImage07()),
                        RecipeRow.create(row.manual08(), row.manualImage08()),
                        RecipeRow.create(row.manual09(), row.manualImage09()),
                        RecipeRow.create(row.manual10(), row.manualImage10()),
                        RecipeRow.create(row.manual11(), row.manualImage11()),
                        RecipeRow.create(row.manual12(), row.manualImage12()),
                        RecipeRow.create(row.manual13(), row.manualImage13()),
                        RecipeRow.create(row.manual14(), row.manualImage14()),
                        RecipeRow.create(row.manual15(), row.manualImage15()),
                        RecipeRow.create(row.manual16(), row.manualImage16()),
                        RecipeRow.create(row.manual17(), row.manualImage17()),
                        RecipeRow.create(row.manual18(), row.manualImage18()),
                        RecipeRow.create(row.manual19(), row.manualImage19()),
                        RecipeRow.create(row.manual20(), row.manualImage20())
                )
                .filter(RecipeRow::hasContent)
                .toList();
    }


    public static MenuAndRecipeCrawlingDto create(
            String menuName,
            Double calories,
            String ingredientDetails,
            List<RecipeRow> recipes
    ) {
        return MenuAndRecipeCrawlingDto.builder()
                .menuName(menuName)
                .calories(calories)
                .ingredientDetails(ingredientDetails)
                .recipes(recipes)
                .build();
    }

    @Builder
    public record RecipeRow(
            String contents,
            String imageUrl
    ) {
        public static RecipeRow create(String contents, String imageUrl) {
            return RecipeRow.builder()
                    .contents(contents)
                    .imageUrl(imageUrl)
                    .build();
        }

        /**
         * contents를 갖고 있는지 검증 (이미지만 있어도 없는 것으로 간주)
         * @return
         */
        public boolean hasContent() {
            return StringUtils.hasText(contents);
        }
    }
}
