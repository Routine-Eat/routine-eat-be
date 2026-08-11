package com.likelion.routineeatbe.domain.foodIngredient.controller;

import com.likelion.routineeatbe.domain.foodIngredient.dto.response.FoodIngredientResponse;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Food Ingredient", description = "식재료 관리 API")
@RequestMapping("/api/v1/food-ingredients")
public interface FoodIngredientControllerDocs {

    @Operation(
            summary = "식재료 조회 API",
            description = "검색어를 이용하여 식재료 조회, 검색어가 없다면 식재료 전체 조회"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "식재료 조회 성공"
            ),
    })
    @GetMapping
    GlobalResponse<List<FoodIngredientResponse>> getFoodIngredients(
            @Parameter(description = "검색어 (선택)")
            @RequestParam(name = "search", required = false) String search
    );

    @Operation(
            summary = "알레르기 유발 식재료 조회 API",
            description = "알레르기 유발 식품 리스트 조회"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "알레르기 유발 식재료 품목 조회 성공"
            ),
    })
    @GetMapping("/allergy")
    GlobalResponse<List<FoodIngredientResponse>> getAllergyFoodIngredients();

    @Operation(
            summary = "식재료 세팅 API",
            description = "서버 DB에 식재료 데이터 초기세팅"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "식재료 세팅 성공"
            ),
    })
    @PostMapping("/init")
    GlobalResponse insertFoodIngredient();
}
