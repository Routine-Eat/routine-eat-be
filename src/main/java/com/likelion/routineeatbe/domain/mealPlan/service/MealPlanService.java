package com.likelion.routineeatbe.domain.mealPlan.service;

import com.likelion.routineeatbe.domain.mealPlan.dto.request.CreateMealPlanRequest;
import com.likelion.routineeatbe.domain.mealPlan.dto.request.CreatePlanMenuRequest;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.MealPlanDetailResponse;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.MealPlanResponse;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.PlanMenuResponse;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlan;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanStatus;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanType;
import com.likelion.routineeatbe.domain.mealPlan.entity.PlanMenu;
import com.likelion.routineeatbe.domain.mealPlan.exception.MealPlanErrorCode;
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
import java.util.Map;
import java.util.stream.Collectors;

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
     * @param userId 사용자 식별자
     * @param request 생성 요청 데이터
     * @return 생성된 식단 상데 조회 데이터
     */
    @Transactional
    public MealPlanDetailResponse createUserMealPlan(Long userId, CreateMealPlanRequest request) {
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
        return MealPlanDetailResponse.from(savedMealPlan, planMenuResponses);
    }

    /**
     * - 사용자 식단 조회
     * - userId로 사용자 고정
     * - type가 있다면 그 종류만 없다면 전체 조회
     * @param userId 사용자 식별자
     * @param status 저장 종류
     * @return 식단 정보 및 연결된 식단 메뉴 PK
     */
    @Transactional(readOnly = true)
    public List<MealPlanResponse> getUserMealPlan(Long userId, MealPlanStatus status) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER);
        }

        // 1. type 유무에 따라 식단 조회 (null이면 전체, 존재하면 해당 타입만)
        List<MealPlan> mealPlans = (status == null)
                ? mealPlanRepository.findByUser_Id(userId)
                : mealPlanRepository.findByUser_IdAndStatus(userId, status);

        if (mealPlans.isEmpty()) {
            return List.of();
        }

        // 2. 조회된 식단들의 ID만 추출
        List<Long> mealPlanIds = mealPlans.stream()
                .map(MealPlan::getId)
                .toList();

        // 3. 식단 ID들에 속한 모든 PlanMenu를 한 번에 조회 (IN 쿼리)
        List<PlanMenu> planMenus = planMenuRepository.findByMealPlan_IdIn(mealPlanIds);

        // 4. 식단 ID를 Key로, PlanMenu ID 리스트를 Value로 그룹화 (메모리 연산)
        Map<Long, List<Long>> planMenuIdsMap = planMenus.stream()
                .collect(Collectors.groupingBy(
                        pm -> pm.getMealPlan().getId(),
                        Collectors.mapping(PlanMenu::getId, Collectors.toList())
                ));

        // 5. DTO 매핑하여 반환
        return mealPlans.stream()
                .map(mealPlan -> MealPlanResponse.from(
                        mealPlan,
                        planMenuIdsMap.getOrDefault(mealPlan.getId(), List.of())
                ))
                .toList();
    }

    /**
     * 식단 상세 조회
     * @param userId 사용자 식별자
     * @param mealPlanId 식단 식별자
     * @return 식단 상세 정보 (단일 객체 반환으로 수정)
     */
    @Transactional(readOnly = true)
    public MealPlanDetailResponse getDetailMealPlan(Long userId, Long mealPlanId) {

        // 1. 식단(MealPlan) 존재 및 본인 소유 여부 검증
        MealPlan mealPlan = mealPlanRepository.findByIdAndUser_Id(mealPlanId, userId)
                .orElseThrow(() -> new CustomException(MealPlanErrorCode.NOT_EXIST_PLAN));

        // 2. PlanMenuRepository에서 해당 식단에 연결된 PlanMenu 목록 조회
        List<PlanMenu> planMenus = planMenuRepository.findAllByMealPlan_Id(mealPlanId);

        // 3. PlanMenu 엔티티 리스트를 PlanMenuResponse DTO 리스트로 변환
        List<PlanMenuResponse> planMenuList = planMenus.stream()
                .map(PlanMenuResponse::from)
                .toList();

        // 4. 최종 MealPlanDetailResponse DTO 생성 및 반환
        return MealPlanDetailResponse.from(mealPlan, planMenuList);
    }
}
