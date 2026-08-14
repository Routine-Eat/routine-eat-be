package com.likelion.routineeatbe.domain.mealPlan.controller;

import com.likelion.routineeatbe.domain.mealPlan.dto.request.CreateMealPlanRequest;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.MealPlanDetailResponse;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.MealPlanResponse;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanStatus;
import com.likelion.routineeatbe.domain.mealPlan.service.MealPlanAICreateService;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.AiMealRecommendationResponse;
import com.likelion.routineeatbe.domain.mealPlan.service.MealPlanService;
import com.likelion.routineeatbe.domain.mealPlan.service.PlanMenuService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MealPlanController implements MealPlanControllerDocs{
    private final MealPlanAICreateService mealPlanAICreateService;
    private final MealPlanService mealPlanService;
    private final PlanMenuService planMenuService;

    @Override
    public GlobalResponse<AiMealRecommendationResponse> recommendThreeMeals(Long userId) {
        // HTTP 계층은 요청/공통 응답 포장만 담당하고, 추천 규칙은 Service에 위임합니다.
        return GlobalResponse.success(mealPlanAICreateService.recommendThreeMeals(userId));
    }

    @Override
    public GlobalResponse<MealPlanDetailResponse> saveMealPlan(Long userId, CreateMealPlanRequest request){
        MealPlanDetailResponse mealPlanDetailResponse = mealPlanService.createUserMealPlan(userId,request);
        return GlobalResponse.success(201,"사용자 식단 저장에 성공했습니다.", mealPlanDetailResponse);
    }

    @Override
    public GlobalResponse<List<MealPlanResponse>> getUserMealPlan(Long userId, MealPlanStatus status){
        List<MealPlanResponse> mealPlanResponseList=mealPlanService.getUserMealPlan(userId,status);
        return GlobalResponse.success(200,"사용자 식단 조회에 성공했습니다.",mealPlanResponseList);
    }

    @Override
    public GlobalResponse<MealPlanDetailResponse> getDetailMealPlan(Long userId,Long mealPlanId){
        return GlobalResponse.success(200,"식단 상제조회에 성공 했습니다.",mealPlanService.getDetailMealPlan(userId,mealPlanId));
    }

    @Override
    public GlobalResponse<MealPlanDetailResponse> updatePlanMenuCompleted(Long userId,Long planMenuId,Boolean completed){
        MealPlanDetailResponse mealPlanDetailResponse= planMenuService.updateUserMealPlan(userId,planMenuId,completed);

        return GlobalResponse.success(203,"식단 메뉴 완료 여부 수정 성공",mealPlanDetailResponse);
    }
}
