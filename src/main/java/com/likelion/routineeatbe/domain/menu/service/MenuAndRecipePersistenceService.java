package com.likelion.routineeatbe.domain.menu.service;

import com.likelion.routineeatbe.domain.menu.dto.response.MenuAndRecipeCrawlingDto;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.repository.MenuRepository;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.entity.RecipeStep;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeStepRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuAndRecipePersistenceService {

    private final MenuRepository menuRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeStepRepository recipeStepRepository;

    /**
     * 메뉴 DTO 목록을 신규 메뉴, 기본 레시피, 일반 조리 단계 Entity로 변환하여 일괄 저장합니다.
     * Menu, Recipe, RecipeStep 순서로 각 Repository를 통해 저장합니다.
     *
     * @param crawlingDtos 저장할 메뉴 DTO 목록
     */
    @Transactional
    public void saveAll(List<MenuAndRecipeCrawlingDto> crawlingDtos) {
        log.info(
                "[MenuAndRecipePersistenceService] 메뉴 및 레시피 일괄 저장 시작 | saveAll() - START | menuCount: {}",
                crawlingDtos.size()
        );

        List<MenuAndRecipeCrawlingDto> newMenuDtos = excludeExistingMenus(crawlingDtos);
        if (newMenuDtos.isEmpty()) {
            log.info(
                    "[MenuAndRecipePersistenceService] 메뉴 및 레시피 일괄 저장 종료 | saveAll() - END | savedMenuCount: 0"
            );
            return;
        }

        List<Menu> menus = createMenus(newMenuDtos);
        menuRepository.saveAll(menus);

        List<Recipe> recipes = createRecipes(menus);
        recipeRepository.saveAll(recipes);

        List<RecipeStep> recipeSteps = createRecipeSteps(newMenuDtos, recipes);
        recipeStepRepository.saveAll(recipeSteps);

        log.info(
                "[MenuAndRecipePersistenceService] 메뉴 및 레시피 일괄 저장 종료 | saveAll() - END | savedMenuCount: {}",
                menus.size()
        );
    }

    /**
     * DB에 이미 존재하는 메뉴명을 조회하여 신규 메뉴 DTO만 반환합니다.
     * 메뉴명은 정규화하지 않고 원본 문자열 그대로 비교합니다.
     *
     * @param crawlingDtos 저장 대상 메뉴 DTO 목록
     * @return DB에 존재하지 않는 메뉴 DTO 목록
     */
    private List<MenuAndRecipeCrawlingDto> excludeExistingMenus(
            List<MenuAndRecipeCrawlingDto> crawlingDtos
    ) {
        log.debug(
                "[MenuAndRecipePersistenceService] 기존 메뉴 제외 시작 | excludeExistingMenus() - START | menuCount: {}",
                crawlingDtos.size()
        );

        if (crawlingDtos.isEmpty()) {
            log.debug(
                    "[MenuAndRecipePersistenceService] 기존 메뉴 제외 종료 | excludeExistingMenus() - END | resultSize: 0"
            );
            return List.of();
        }

        List<String> menuNames = crawlingDtos.stream()
                .map(MenuAndRecipeCrawlingDto::menuName)
                .distinct()
                .toList();
        Set<String> existingNames = menuRepository.findExistingNames(menuNames);
        List<MenuAndRecipeCrawlingDto> result = crawlingDtos.stream()
                .filter(crawlingDto -> !existingNames.contains(crawlingDto.menuName()))
                .toList();

        log.debug(
                "[MenuAndRecipePersistenceService] 기존 메뉴 제외 종료 | excludeExistingMenus() - END | existingCount: {}, resultSize: {}",
                existingNames.size(),
                result.size()
        );
        return result;
    }

    /**
     * 메뉴 DTO 목록을 Menu Entity 목록으로 변환합니다.
     *
     * @param crawlingDtos 변환할 메뉴 DTO 목록
     * @return 변환된 Menu Entity 목록
     */
    private List<Menu> createMenus(List<MenuAndRecipeCrawlingDto> crawlingDtos) {
        log.debug(
                "[MenuAndRecipePersistenceService] Menu Entity 목록 생성 시작 | createMenus() - START | menuCount: {}",
                crawlingDtos.size()
        );

        List<Menu> result = crawlingDtos.stream()
                .map(crawlingDto -> Menu.createFromFoodSafetyKorea(
                        crawlingDto.menuName(),
                        crawlingDto.calories(),
                        crawlingDto.ingredientDetails()
                ))
                .toList();

        log.debug(
                "[MenuAndRecipePersistenceService] Menu Entity 목록 생성 종료 | createMenus() - END | resultSize: {}",
                result.size()
        );
        return result;
    }

    /**
     * 저장할 Menu Entity마다 기본 Recipe Entity를 생성합니다.
     *
     * @param menus 저장할 Menu Entity 목록
     * @return 생성된 Recipe Entity 목록
     */
    private List<Recipe> createRecipes(List<Menu> menus) {
        log.debug(
                "[MenuAndRecipePersistenceService] Recipe Entity 목록 생성 시작 | createRecipes() - START | menuCount: {}",
                menus.size()
        );

        List<Recipe> result = menus.stream()
                .map(Recipe::createBasic)
                .toList();

        log.debug(
                "[MenuAndRecipePersistenceService] Recipe Entity 목록 생성 종료 | createRecipes() - END | resultSize: {}",
                result.size()
        );
        return result;
    }

    /**
     * DTO의 조리 단계를 각 Recipe에 속하는 RecipeStep Entity 목록으로 변환합니다.
     *
     * @param crawlingDtos 조리 단계가 포함된 메뉴 DTO 목록
     * @param recipes 저장할 Recipe Entity 목록
     * @return 생성된 RecipeStep Entity 목록
     */
    private List<RecipeStep> createRecipeSteps(
            List<MenuAndRecipeCrawlingDto> crawlingDtos,
            List<Recipe> recipes
    ) {
        log.debug(
                "[MenuAndRecipePersistenceService] RecipeStep Entity 목록 생성 시작 | createRecipeSteps() - START | recipeCount: {}",
                recipes.size()
        );

        List<RecipeStep> result = IntStream.range(0, crawlingDtos.size())
                .boxed()
                .flatMap(recipeIndex -> IntStream.range(
                                0,
                                crawlingDtos.get(recipeIndex).recipes().size()
                        )
                        .mapToObj(stepIndex -> RecipeStep.createNormal(
                                recipes.get(recipeIndex),
                                stepIndex + 1L,
                                crawlingDtos.get(recipeIndex).recipes().get(stepIndex).contents()
                        )))
                .toList();

        log.debug(
                "[MenuAndRecipePersistenceService] RecipeStep Entity 목록 생성 종료 | createRecipeSteps() - END | resultSize: {}",
                result.size()
        );
        return result;
    }
}
