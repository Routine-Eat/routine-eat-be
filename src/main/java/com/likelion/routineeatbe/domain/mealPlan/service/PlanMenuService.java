package com.likelion.routineeatbe.domain.mealPlan.service;

import com.likelion.routineeatbe.domain.mealPlan.dto.response.MealPlanDetailResponse;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.PlanMenuResponse;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlan;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanStatus;
import com.likelion.routineeatbe.domain.mealPlan.entity.PlanMenu;
import com.likelion.routineeatbe.domain.mealPlan.exception.PlanMenuErrorCode;
import com.likelion.routineeatbe.domain.mealPlan.repository.PlanMenuRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanMenuService {
    private final PlanMenuRepository planMenuRepository;

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
    public MealPlanDetailResponse updateUserMealPlan(Long userId,Long planMenuId,Boolean completed){
        PlanMenu planMenu=planMenuRepository.findById(planMenuId)
                .orElseThrow(()->new CustomException(PlanMenuErrorCode.NOT_EXIST_PLAN_MENU));

        MealPlan mealPlan=planMenu.getMealPlan();
        if (mealPlan.getStatus()!=MealPlanStatus.PROGRESS){
            throw new CustomException(PlanMenuErrorCode.NOT_PROGRESS_PLAN);
        }
        if (userId != mealPlan.getUser().getId()){
            throw new CustomException(PlanMenuErrorCode.NOT_HAVE_USER);
        }

        planMenu.updatePlanMenuCompleted(completed);

        List<PlanMenuResponse> planMenuResponses=planMenuRepository.findByMealPlan_Id(mealPlan.getId()).stream()
                .map(PlanMenuResponse::from)
                .toList();

        return MealPlanDetailResponse.from(mealPlan,planMenuResponses);
    }
}
