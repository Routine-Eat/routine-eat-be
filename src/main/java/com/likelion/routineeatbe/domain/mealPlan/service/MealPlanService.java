package com.likelion.routineeatbe.domain.mealPlan.service;

import com.likelion.routineeatbe.domain.mealPlan.dto.request.CreateMealPlanRequest;
import com.likelion.routineeatbe.domain.mealPlan.dto.request.CreatePlanMenuRequest;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.MealPlanResponse;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.PlanMenuResponse;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlan;
import com.likelion.routineeatbe.domain.mealPlan.entity.PlanMenu;
import com.likelion.routineeatbe.domain.mealPlan.repository.MealPlanRepository;
import com.likelion.routineeatbe.domain.mealPlan.repository.PlanMenuRepository;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.repository.MenuRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.exception.UserFoodIngredientErrorCode;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealPlanService {
    private final MealPlanRepository mealPlanRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final PlanMenuRepository planMenuRepository;

    /**
     * - 식단 저장 API
     * - 사용자 아이디로 식단에 저장
     * - request의 메뉴 아이디로 식단메뉴에 저장
     * @param userId
     * @param request
     * @return
     */
    @Transactional
    public MealPlanResponse createUserMealPlan(Long userId, CreateMealPlanRequest request) {
        // 1. 사용자(User) 존재 여부 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER));

        // 2. MealPlan(식단) 엔티티 생성 및 저장
        MealPlan mealPlan = MealPlan.createMealPlan(request,user);

        MealPlan savedMealPlan = mealPlanRepository.save(mealPlan);

        // 3. 식단 메뉴(PlanMenu) 생성 및 저장
        List<Menu> menus = menuRepository.findAllById(request.planMenuIdList());

        // 3-1. 메뉴들을 식단-메뉴 생성 dto로 변환 후 createPlanMenu에 적용하여 리스트 만들기
        List<PlanMenu> planMenus = menus.stream()
                .map(menu-> PlanMenu.createPlanMenu(CreatePlanMenuRequest.from(savedMealPlan,menu)))
                .toList();

        List<PlanMenu> savedPlanMenus = planMenuRepository.saveAll(planMenus);

        // 4. PlanMenuResponse 리스트 변환
        List<PlanMenuResponse> planMenuResponses = savedPlanMenus.stream()
                .map(PlanMenuResponse::from) // PlanMenuResponse.from(PlanMenu) 구현체 활용
                .toList();

        // 5. 최종 MealPlanResponse 반환
        return MealPlanResponse.from(
                savedMealPlan.getId(),
                savedMealPlan.getType(),
                savedMealPlan.getStatus(),
                planMenuResponses
        );
    }
}
