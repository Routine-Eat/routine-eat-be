package com.likelion.routineeatbe.domain.mealPlan.service;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.mealPlan.dto.request.CreateMealPlanRequest;
import com.likelion.routineeatbe.domain.mealPlan.dto.request.CreatePlanMenuRequest;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.MealPlanDetailResponse;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.MealPlanResponse;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.PlanMenuResponse;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlan;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanStatus;
import com.likelion.routineeatbe.domain.mealPlan.entity.PlanMenu;
import com.likelion.routineeatbe.domain.mealPlan.exception.MealPlanErrorCode;
import com.likelion.routineeatbe.domain.mealPlan.repository.MealPlanRepository;
import com.likelion.routineeatbe.domain.mealPlan.repository.PlanMenuRepository;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.repository.MenuRepository;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import com.likelion.routineeatbe.domain.user.exception.UserFoodIngredientErrorCode;
import com.likelion.routineeatbe.domain.user.repository.UserFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
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
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final PlanMenuRepository planMenuRepository;
    private final UserFoodIngredientRepository userFoodIngredientRepository;
    private final RecipeRepository recipeRepository;

    /**
     * - 식단 저장 API
     */
    @Transactional
    public MealPlanDetailResponse createUserMealPlan(Long userId, CreateMealPlanRequest request) {
        // 1. 사용자(User) 존재 여부 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER));

        // 2. MealPlan(식단) 엔티티 생성 및 저장
        MealPlan mealPlan = MealPlan.createMealPlan(request, user);
        MealPlan savedMealPlan = mealPlanRepository.save(mealPlan);

        // 3. 식단 메뉴(PlanMenu) 생성 및 저장
        List<Menu> menus = menuRepository.findAllById(request.planMenuIdList());
        List<PlanMenu> planMenus = menus.stream()
                .map(menu -> PlanMenu.createPlanMenu(CreatePlanMenuRequest.from(savedMealPlan, menu)))
                .toList();

        List<PlanMenu> savedPlanMenus = planMenuRepository.saveAll(planMenus);

        // 4. 부족한 식재료가 포함된 PlanMenuResponse 리스트 생성
        List<PlanMenuResponse> planMenuResponses = createPlanMenuResponsesWithMissingIngredients(userId, savedPlanMenus);

        // 5. 최종 MealPlanDetailResponse 반환
        return MealPlanDetailResponse.from(savedMealPlan, planMenuResponses);
    }

    /**
     * - 사용자 식단 조회
     */
    @Transactional(readOnly = true)
    public List<MealPlanResponse> getUserMealPlan(Long userId, MealPlanStatus status) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER);
        }

        List<MealPlan> mealPlans = (status == null)
                ? mealPlanRepository.findByUser_Id(userId)
                : mealPlanRepository.findByUser_IdAndStatus(userId, status);

        if (mealPlans.isEmpty()) {
            return List.of();
        }

        List<Long> mealPlanIds = mealPlans.stream()
                .map(MealPlan::getId)
                .toList();

        List<PlanMenu> planMenus = planMenuRepository.findByMealPlan_IdIn(mealPlanIds);

        Map<Long, List<Long>> planMenuIdsMap = planMenus.stream()
                .collect(Collectors.groupingBy(
                        pm -> pm.getMealPlan().getId(),
                        Collectors.mapping(PlanMenu::getId, Collectors.toList())
                ));

        return mealPlans.stream()
                .map(mealPlan -> MealPlanResponse.from(
                        mealPlan,
                        planMenuIdsMap.getOrDefault(mealPlan.getId(), List.of())
                ))
                .toList();
    }

    /**
     * 식단 상세 조회
     */
    @Transactional(readOnly = true)
    public MealPlanDetailResponse getDetailMealPlan(Long userId, Long mealPlanId) {

        // 1. 식단(MealPlan) 존재 및 본인 소유 여부 검증
        MealPlan mealPlan = mealPlanRepository.findByIdAndUser_Id(mealPlanId, userId)
                .orElseThrow(() -> new CustomException(MealPlanErrorCode.NOT_EXIST_PLAN));

        // 2. PlanMenuRepository에서 해당 식단에 연결된 PlanMenu 목록 조회
        List<PlanMenu> planMenus = planMenuRepository.findAllByMealPlan_Id(mealPlanId);

        // 3. 부족한 식재료가 포함된 PlanMenuResponse 리스트 생성
        List<PlanMenuResponse> planMenuList = createPlanMenuResponsesWithMissingIngredients(userId, planMenus);

        // 4. 최종 MealPlanDetailResponse DTO 생성 및 반환
        return MealPlanDetailResponse.from(mealPlan, planMenuList);
    }

    /**
     * 식단 상태 변경 API
     */
    @Transactional
    public MealPlanResponse updateMealPlanStatus(Long userId, Long mealPlanId, MealPlanStatus status) {
        MealPlan mealPlan = mealPlanRepository.findById(mealPlanId)
                .orElseThrow(() -> new CustomException(MealPlanErrorCode.NOT_EXIST_PLAN));

        if (!userId.equals(mealPlan.getUser().getId())) {
            throw new CustomException(MealPlanErrorCode.NOT_HAVE_USER);
        }

        mealPlan.updateMealPlanStatus(status);

        List<Long> planMenus = planMenuRepository.findIdsByMealPlan_IdIn(mealPlanId);

        return MealPlanResponse.from(mealPlan, planMenus);
    }

    /**
     * 사용자-식단 삭제 API
     */
    @Transactional
    public void deleteUserMealPlan(Long userId, Long mealPlanId) {
        MealPlan mealPlan = mealPlanRepository.findById(mealPlanId)
                .orElseThrow(() -> new CustomException(MealPlanErrorCode.NOT_EXIST_PLAN));

        if (!userId.equals(mealPlan.getUser().getId())) {
            throw new CustomException(MealPlanErrorCode.NOT_HAVE_USER);
        }

        mealPlanRepository.deleteById(mealPlanId);
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
                .map(planMenu -> {
                    Long menuId = planMenu.getMenu().getId();
                    Recipe recipe = recipeMap.get(menuId);

                    List<String> missingIngredients = List.of();
                    if (recipe != null) {
                        missingIngredients = recipe.getRecipeFoodIngredients().stream()
                                .map(rfi -> rfi.getFoodIngredient())
                                .filter(ingredient -> !ownedIngredientIds.contains(ingredient.getId()))
                                .map(FoodIngredient::getName)
                                .toList();
                    }

                    return PlanMenuResponse.from(planMenu, missingIngredients);
                })
                .toList();
    }
}