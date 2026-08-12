package com.likelion.routineeatbe.domain.favoriteRecipe.mapper;

import com.likelion.routineeatbe.domain.favoriteRecipe.dto.response.FavoriteRecipeListResDto;
import com.likelion.routineeatbe.domain.favoriteRecipe.dto.response.FavoriteRecipeResDto;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import java.util.List;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class FavoriteRecipeMapper {

    /**
     * 찜 레시피 조회 결과를 응답 DTO로 변환합니다.
     *
     * @param result 변환할 레시피 조회 결과
     * @return 변환된 찜 레시피 응답 DTO
     */
    public FavoriteRecipeResDto toFavoriteRecipeResDto(RecipeSearchResult result) {
        return FavoriteRecipeResDto.create(
                result.recipeId(),
                result.menuName(),
                result.thumbnailUrl(),
                result.calory(),
                result.timeRequired(),
                result.difficultyLevel(),
                result.category(),
                result.matchedIngredientCount(),
                result.requiredIngredientCount(),
                result.requiredIngredientCost()
        );
    }

    /**
     * 찜 레시피 Slice와 다음 커서를 목록 응답 DTO로 변환합니다.
     *
     * @param slice 찜 레시피 조회 결과 Slice
     * @param nextCursor 다음 조회에 사용할 위치 커서
     * @return 변환된 찜 레시피 목록 응답 DTO
     */
    public FavoriteRecipeListResDto toFavoriteRecipeListResDto(
            Slice<RecipeSearchResult> slice,
            Long nextCursor
    ) {
        List<FavoriteRecipeResDto> content = slice.getContent().stream()
                .map(this::toFavoriteRecipeResDto)
                .toList();
        return FavoriteRecipeListResDto.create(content, slice.hasNext(), nextCursor);
    }
}
