package com.likelion.routineeatbe.domain.favoriteRecipe.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.favoriteRecipe.dto.response.FavoriteRecipeListResDto;
import com.likelion.routineeatbe.domain.favoriteRecipe.dto.response.FavoriteRecipeResDto;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

class FavoriteRecipeMapperTest {

    private final FavoriteRecipeMapper favoriteRecipeMapper = new FavoriteRecipeMapper();

    @Test
    @DisplayName("레시피 조회 결과를 찜한 레시피 응답으로 변환 성공")
    void 레시피_조회_결과를_찜한_레시피_응답으로_변환_성공() {
        // given
        RecipeSearchResult source = createRecipeSearchResult();

        // when
        FavoriteRecipeResDto result = favoriteRecipeMapper.toFavoriteRecipeResDto(source);

        // then
        assertThat(result.recipeId()).isEqualTo(659L);
        assertThat(result.menuName()).isEqualTo("감자미역국");
        assertThat(result.thumbnailUrl()).isEqualTo("thumbnail");
        assertThat(result.calory()).isEqualTo(35.4);
        assertThat(result.timeRequired()).isEqualTo(20);
        assertThat(result.difficultyLevel()).isEqualTo(DifficultyLevel.LEVEL_2);
        assertThat(result.category()).isEqualTo(MenuType.KOREAN);
        assertThat(result.matchedIngredientCount()).isEqualTo(1L);
        assertThat(result.requiredIngredientCount()).isEqualTo(4L);
        assertThat(result.requiredIngredientCost()).isEqualTo(10_000L);
    }

    @Test
    @DisplayName("레시피 Slice를 찜한 레시피 목록 응답으로 변환 성공")
    void 레시피_Slice를_찜한_레시피_목록_응답으로_변환_성공() {
        // given
        Slice<RecipeSearchResult> source = new SliceImpl<>(
                List.of(createRecipeSearchResult()),
                PageRequest.of(0, 10),
                true
        );

        // when
        FavoriteRecipeListResDto result = favoriteRecipeMapper.toFavoriteRecipeListResDto(
                source,
                11L
        );

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(11L);
    }

    private RecipeSearchResult createRecipeSearchResult() {
        return new RecipeSearchResult(
                659L,
                100L,
                "감자미역국",
                "thumbnail",
                35.4,
                20,
                DifficultyLevel.LEVEL_2,
                MenuType.KOREAN,
                0L,
                1L,
                4L,
                10_000L
        );
    }
}
