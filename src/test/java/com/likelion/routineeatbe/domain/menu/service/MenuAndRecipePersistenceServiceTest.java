package com.likelion.routineeatbe.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

import com.likelion.routineeatbe.domain.menu.dto.MenuAndRecipeMetaDataDto;
import com.likelion.routineeatbe.domain.menu.dto.response.MenuAndRecipeCrawlingDto;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.menu.repository.MenuRepository;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.entity.RecipeStep;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeStepType;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeType;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeStepRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuAndRecipePersistenceServiceTest {

    @InjectMocks
    private MenuAndRecipePersistenceService menuAndRecipePersistenceService;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeStepRepository recipeStepRepository;

    @Test
    @DisplayName("Gemini 메타데이터를 적용하여 메뉴, 레시피, 조리 단계를 저장한다")
    void Gemini_메타데이터_적용_메뉴_레시피_조리단계_저장_성공() {
        // given
        MenuAndRecipeCrawlingDto crawlingDto = createCrawlingDto("테스트 메뉴");
        MenuAndRecipeMetaDataDto metaData = MenuAndRecipeMetaDataDto.create(
                MenuType.CHINESE,
                RecommendationType.GLUTEN_FREE,
                35
        );

        // when
        menuAndRecipePersistenceService.saveAll(
                List.of(crawlingDto),
                Map.of(crawlingDto.menuName(), metaData)
        );

        // then
        ArgumentCaptor<List<Menu>> menuCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Recipe>> recipeCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<RecipeStep>> recipeStepCaptor = ArgumentCaptor.forClass(List.class);
        then(menuRepository).should().saveAll(menuCaptor.capture());
        then(recipeRepository).should().saveAll(recipeCaptor.capture());
        then(recipeStepRepository).should().saveAll(recipeStepCaptor.capture());

        Menu menu = menuCaptor.getValue().getFirst();
        Recipe recipe = recipeCaptor.getValue().getFirst();
        RecipeStep recipeStep = recipeStepCaptor.getValue().getFirst();

        assertThat(menu.getType()).isEqualTo(MenuType.CHINESE);
        assertThat(menu.getRecommendationType()).isEqualTo(RecommendationType.GLUTEN_FREE);
        assertThat(menu.getTimeRequired()).isEqualTo(35);
        assertThat(menu.getDifficultyLevel()).isEqualTo(DifficultyLevel.LEVEL_1);
        assertThat(recipe.getType()).isEqualTo(RecipeType.BASIC);
        assertThat(recipe.getMenu()).isSameAs(menu);
        assertThat(recipeStep.getType()).isEqualTo(RecipeStepType.NORMAL);
        assertThat(recipeStep.getRecipe()).isSameAs(recipe);
        assertThat(recipeStep.getLevel()).isEqualTo(1L);
    }

    @Test
    @DisplayName("DB에 존재하는 메뉴를 제외하고 신규 메뉴만 반환한다")
    void DB_기존_메뉴_제외_신규_메뉴_조회_성공() {
        // given
        MenuAndRecipeCrawlingDto existingMenu = createCrawlingDto("기존 메뉴");
        MenuAndRecipeCrawlingDto newMenu = createCrawlingDto("신규 메뉴");
        given(menuRepository.findExistingNames(any())).willReturn(Set.of("기존 메뉴"));

        // when
        List<MenuAndRecipeCrawlingDto> result = menuAndRecipePersistenceService.findNewMenuDtos(
                List.of(existingMenu, newMenu)
        );

        // then
        assertThat(result).extracting(MenuAndRecipeCrawlingDto::menuName).containsExactly("신규 메뉴");
    }

    @Test
    @DisplayName("모든 메뉴가 DB에 존재하면 빈 목록을 반환한다")
    void 모든_메뉴_DB_존재_신규_메뉴_빈_목록_성공() {
        // given
        MenuAndRecipeCrawlingDto existingMenu = createCrawlingDto("기존 메뉴");
        given(menuRepository.findExistingNames(any())).willReturn(Set.of("기존 메뉴"));

        // when
        List<MenuAndRecipeCrawlingDto> result = menuAndRecipePersistenceService.findNewMenuDtos(
                List.of(existingMenu)
        );

        // then
        assertThat(result).isEmpty();
        then(menuRepository).should(never()).saveAll(any());
        verifyNoInteractions(recipeRepository, recipeStepRepository);
    }

    @Test
    @DisplayName("빈 메뉴 목록이면 DB 조회와 저장을 수행하지 않는다")
    void 빈_메뉴_목록_DB_작업_생략_성공() {
        // when
        List<MenuAndRecipeCrawlingDto> result = menuAndRecipePersistenceService.findNewMenuDtos(List.of());
        menuAndRecipePersistenceService.saveAll(List.of(), Map.of());

        // then
        assertThat(result).isEmpty();
        verifyNoInteractions(menuRepository, recipeRepository, recipeStepRepository);
    }

    @Test
    @DisplayName("메뉴 메타데이터가 누락되면 저장하지 않는다")
    void 메뉴_메타데이터_누락_저장_실패() {
        // given
        MenuAndRecipeCrawlingDto crawlingDto = createCrawlingDto("테스트 메뉴");

        // when & then
        assertThatThrownBy(() -> menuAndRecipePersistenceService.saveAll(List.of(crawlingDto), Map.of()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(GeminiErrorCode.INVALID_METADATA);
        verifyNoInteractions(menuRepository, recipeRepository, recipeStepRepository);
    }

    private MenuAndRecipeCrawlingDto createCrawlingDto(String menuName) {
        return MenuAndRecipeCrawlingDto.create(
                menuName,
                100.0,
                "테스트 재료",
                List.of(MenuAndRecipeCrawlingDto.RecipeRow.create("조리 단계", null))
        );
    }
}
