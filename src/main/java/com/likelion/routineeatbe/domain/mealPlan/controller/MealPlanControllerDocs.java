package com.likelion.routineeatbe.domain.mealPlan.controller;

import com.likelion.routineeatbe.domain.mealPlan.dto.request.CreateMealPlanRequest;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.AiMealRecommendationResponse;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.MealPlanDetailResponse;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.MealPlanResponse;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanStatus;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanType;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Meal Plan", description = "식단 관리 API")
@RequestMapping("/api/v1/meal-plans")
public interface MealPlanControllerDocs {

    @Operation(
            summary = "AI 목적별 식단 4종 추천",
            description = """
                알레르기·비선호 식재료와 미보유 조리도구를 제외한 뒤, \n
                PRACTICE(실력 향상), USEALL(보유 재료만 사용), SIMPLE(간단한 요리), RECYCLING(메인 재료 공통) \n
                식단을 각각 서로 다른 메뉴 3개로 추천 \n
                보유 재료만으로 만들 수 있는 USEALL 메뉴가 3개 미만이면 useAll은 null로 반환 \n
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "AI 식단 생성 성공"
            )
    })
    @GetMapping("/{userId}/ai-recommendation")
    GlobalResponse<AiMealRecommendationResponse> recommendThreeMeals(@PathVariable Long userId);

    @Operation(
            summary = "사용자-식단 저장 API",
            description = """
            mealPlanType : 선택한 식단에 맞는 타입
            mealPlanStatus : 새로 만드는 것이므로 무조건 PROGRESS/SAVED 중 하나
            planMenuIdList : 식단 메뉴 아이디
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "사용자-식단 저장 성공"
            )
    })
    @PostMapping("/{userId}")
    GlobalResponse<MealPlanDetailResponse> saveMealPlan(
            @PathVariable Long userId,
            @RequestBody CreateMealPlanRequest request
            );

    @Operation(
            summary = "사용자-식단 조회 API",
            description = """
            status : 조회할 사용자-식단 관계 종류
                DONE : 완료
                PROGRESS : 진행 중
                SAVED : 저장
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자-식단 조회 성공"
            )
    })
    @GetMapping("/{userId}")
    GlobalResponse<List<MealPlanResponse>> getUserMealPlan(
        @PathVariable Long userId,
        @RequestParam(name = "status", required = false)
        @Parameter(description = "저장 종류 (선택)")
        MealPlanStatus status
    );

    @Operation(
            summary = "사용자-식단 상세 조회 API",
            description = """
                사용자 식단 상세 조회
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자-식단 상세 조회 성공"
            )
    })
    @GetMapping("/{userId}/{mealPlanId}")
    GlobalResponse<MealPlanDetailResponse> getDetailMealPlan(
            @PathVariable Long userId,
            @PathVariable Long mealPlanId
    );
}
