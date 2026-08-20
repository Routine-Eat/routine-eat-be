package com.likelion.routineeatbe.domain.mealPlan.service;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.MealPlanDetailResponse;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.PlanMenuResponse;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlan;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanStatus;
import com.likelion.routineeatbe.domain.mealPlan.entity.PlanMenu;
import com.likelion.routineeatbe.domain.mealPlan.exception.PlanMenuErrorCode;
import com.likelion.routineeatbe.domain.mealPlan.repository.PlanMenuRepository;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import com.likelion.routineeatbe.domain.user.repository.UserFoodIngredientRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanMenuService {

    private final PlanMenuRepository planMenuRepository;
    private final UserFoodIngredientRepository userFoodIngredientRepository;
    private final RecipeRepository recipeRepository;

    /**
     * 식단의 메뉴 완료 여부 수정
     * - planId로 연결된 식단 조회
     * - 식단으로 사용자 조회하여 요청한 사용자 아이디랑 비교
     * @param userId 사용자 id
     * @param planMenuId 식단 메뉴 id
     * @param completed 완료 여부
     * @return 식단 상세 조회로 요청하지 않은 식단 메뉴까지 출력
     */
    @Transactional
    public MealPlanDetailResponse updateUserMealPlan(Long userId, Long planMenuId, Boolean completed) {
        PlanMenu planMenu = planMenuRepository.findById(planMenuId)
                .orElseThrow(() -> new CustomException(PlanMenuErrorCode.NOT_EXIST_PLAN_MENU));

        MealPlan mealPlan = planMenu.getMealPlan();

        if (!userId.equals(mealPlan.getUser().getId())) {
            throw new CustomException(PlanMenuErrorCode.NOT_HAVE_USER);
        }
        if (mealPlan.getStatus() != MealPlanStatus.PROGRESS) {
            throw new CustomException(PlanMenuErrorCode.NOT_PROGRESS_PLAN);
        }

        planMenu.updatePlanMenuCompleted(completed);

        List<PlanMenu> planMenus = planMenuRepository.findByMealPlan_Id(mealPlan.getId());

        // 부족한 식재료 목록을 계산하여 PlanMenuResponse 리스트 생성
        List<PlanMenuResponse> planMenuResponses = createPlanMenuResponsesWithMissingIngredients(userId, planMenus);

        return MealPlanDetailResponse.from(mealPlan, planMenuResponses);
    }

    /**
     * [공통 내부 메서드]
     * PlanMenu 목록과 사용자의 보유 식재료(OWN)를 비교하여 부족한 식재료 목록을 포함한 PlanMenuResponse 리스트를 반환합니다.
     */
    private List<PlanMenuResponse> createPlanMenuResponsesWithMissingIngredients(Long userId, List<PlanMenu> planMenus) {
        if (planMenus.isEmpty()) {
            return List.of();
        }

        // 1. 사용자가 보유한 식재료 ID 추출
        Set<Long> ownedIngredientIds = userFoodIngredientRepository
                .findAllWithFoodIngredientByUserIdAndRelationType(userId, UserFoodIngredientType.OWN)
                .stream()
                .map(userIngredient -> userIngredient.getFoodIngredient().getId())
                .collect(Collectors.toSet());

        // 2. 메뉴 ID 추출
        List<Long> menuIds = planMenus.stream()
                .map(pm -> pm.getMenu().getId())
                .distinct()
                .toList();

        // 3. 메뉴들의 Recipe 조회 및 Map 구성 (menuId -> Recipe)
        List<Recipe> recipes = recipeRepository.findAllByMenu_IdIn(menuIds);
        Map<Long, Recipe> recipeMap = recipes.stream()
                .collect(Collectors.toMap(r -> r.getMenu().getId(), r -> r, (r1, r2) -> r1));

        // 4. PlanMenu -> PlanMenuResponse 변환
        return planMenus.stream()
                .map(pm -> {
                    Long menuId = pm.getMenu().getId();
                    Recipe recipe = recipeMap.get(menuId);

                    List<String> missingIngredients = List.of();
                    if (recipe != null) {
                        missingIngredients = recipe.getRecipeFoodIngredients().stream()
                                .map(rfi -> rfi.getFoodIngredient())
                                .filter(ingredient -> !ownedIngredientIds.contains(ingredient.getId()))
                                .map(FoodIngredient::getName)
                                .toList();
                    }

                    return PlanMenuResponse.from(pm, missingIngredients);
                })
                .toList();
    }
}