package com.likelion.routineeatbe.domain.menu.service;

import com.likelion.routineeatbe.domain.menu.dto.MenuAndRecipeMetaDataDto;
import com.likelion.routineeatbe.domain.menu.dto.response.MenuAndRecipeCrawlingDto;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.repository.MenuRepository;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.entity.RecipeStep;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeStepRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
     * (1) 작업 목적
     * 신규 메뉴 DTO와 Gemini 메타데이터를 Menu, Recipe, RecipeStep Entity로 변환하여 저장합니다.
     *
     * (2) 세부 작업 내용
     * - 메뉴별 메타데이터의 일치 여부를 검증합니다.
     * - Menu, Recipe, RecipeStep 순서로 각 Repository를 통해 저장합니다.
     *
     * @param crawlingDtos 저장할 신규 메뉴 DTO 목록
     * @param metaDataByMenuName 메뉴명별 메뉴 종류와 예상 조리시간
     */
    @Transactional
    public void saveAll(
            List<MenuAndRecipeCrawlingDto> crawlingDtos,
            Map<String, MenuAndRecipeMetaDataDto> metaDataByMenuName
    ) {
        log.info(
                "[MenuAndRecipePersistenceService] 메뉴 및 레시피 일괄 저장 시작 | saveAll() - START | menuCount: {}",
                crawlingDtos.size()
        );

        if (crawlingDtos.isEmpty()) {
            log.info(
                    "[MenuAndRecipePersistenceService] 메뉴 및 레시피 일괄 저장 종료 | saveAll() - END | savedMenuCount: 0"
            );
            return;
        }

        validateMetaData(crawlingDtos, metaDataByMenuName);

        List<Menu> menus = createMenus(crawlingDtos, metaDataByMenuName);
        menuRepository.saveAll(menus);

        List<Recipe> recipes = createRecipes(menus);
        recipeRepository.saveAll(recipes);

        List<RecipeStep> recipeSteps = createRecipeSteps(crawlingDtos, recipes);
        recipeStepRepository.saveAll(recipeSteps);

        log.info(
                "[MenuAndRecipePersistenceService] 메뉴 및 레시피 일괄 저장 종료 | saveAll() - END | savedMenuCount: {}",
                menus.size()
        );
    }

    /**
     * (1) 작업 목적
     * DB에 존재하지 않는 신규 메뉴 DTO만 반환합니다.
     *
     * (2) 세부 작업 내용
     * - 입력 메뉴명을 한 번에 조회하여 기존 메뉴명을 확인합니다.
     * - 메뉴명은 정규화하지 않고 원본 문자열을 기준으로 비교합니다.
     *
     * @param crawlingDtos 크롤링한 메뉴 DTO 목록
     * @return DB에 존재하지 않는 신규 메뉴 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<MenuAndRecipeCrawlingDto> findNewMenuDtos(
            List<MenuAndRecipeCrawlingDto> crawlingDtos
    ) {
        log.info(
                "[MenuAndRecipePersistenceService] 신규 메뉴 조회 시작 | findNewMenuDtos() - START | menuCount: {}",
                crawlingDtos.size()
        );

        if (crawlingDtos.isEmpty()) {
            log.info(
                    "[MenuAndRecipePersistenceService] 신규 메뉴 조회 종료 | findNewMenuDtos() - END | resultSize: 0"
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

        log.info(
                "[MenuAndRecipePersistenceService] 신규 메뉴 조회 종료 | findNewMenuDtos() - END | resultSize: {}",
                result.size()
        );
        return result;
    }

    private void validateMetaData(
            List<MenuAndRecipeCrawlingDto> crawlingDtos,
            Map<String, MenuAndRecipeMetaDataDto> metaDataByMenuName
    ) {
        log.debug(
                "[MenuAndRecipePersistenceService] 메뉴 메타데이터 검증 시작 | validateMetaData() - START | menuCount: {}",
                crawlingDtos.size()
        );

        Set<String> menuNames = crawlingDtos.stream()
                .map(MenuAndRecipeCrawlingDto::menuName)
                .collect(Collectors.toSet());

        if (Objects.isNull(metaDataByMenuName)
                || menuNames.size() != crawlingDtos.size()
                || !menuNames.equals(metaDataByMenuName.keySet())
                || metaDataByMenuName.values().stream().anyMatch(Objects::isNull)) {
            throw new CustomException(GeminiErrorCode.INVALID_METADATA);
        }

        log.debug("[MenuAndRecipePersistenceService] 메뉴 메타데이터 검증 종료 | validateMetaData() - END");
    }

    private List<Menu> createMenus(
            List<MenuAndRecipeCrawlingDto> crawlingDtos,
            Map<String, MenuAndRecipeMetaDataDto> metaDataByMenuName
    ) {
        log.debug(
                "[MenuAndRecipePersistenceService] Menu Entity 목록 생성 시작 | createMenus() - START | menuCount: {}",
                crawlingDtos.size()
        );

        List<Menu> result = crawlingDtos.stream()
                .map(crawlingDto -> {
                    MenuAndRecipeMetaDataDto metaData = metaDataByMenuName.get(crawlingDto.menuName());
                    return Menu.create(
                            crawlingDto.menuName(),
                            crawlingDto.calories(),
                            crawlingDto.ingredientDetails(),
                            metaData.menuType(),
                            metaData.recommendationType(),
                            metaData.timeRequired(),
                            metaData.thumbnailUrl()
                    );
                })
                .toList();

        log.debug(
                "[MenuAndRecipePersistenceService] Menu Entity 목록 생성 종료 | createMenus() - END | resultSize: {}",
                result.size()
        );
        return result;
    }

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
