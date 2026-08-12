package com.likelion.routineeatbe.domain.cookingEquipment.controller;

import com.likelion.routineeatbe.domain.cookingEquipment.dto.response.CookingEquipmentResponse;
import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipmentSymbol;
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

@Tag(name = "Cooking Equipment", description = "조리도구 관리 API")
@RequestMapping("/api/v1/cooking-equipments")
public interface CookingEquipmentControllerDocs {
    @Operation(
            summary = "조리도구 조회 API",
            description = """
                    검색어를 이용하여 조리도구 조회, 검색어가 없다면 조리도구 전체 조회 \n
                    symbol : 도구 속성 \n
                        ESSENTIAL - 요리에 필수적인 \n
                        RECOMMEND - 요리에 추천 \n
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조리도구 조회 성공"
            ),
    })
    @GetMapping
    GlobalResponse<List<CookingEquipmentResponse>> getCookingEquipments(
            @Parameter(description = "검색어 (선택)")
            @RequestParam(name = "search", required = false) String search,
            @Parameter(description = "대표 분야 (선택)")
            @RequestParam(name = "smybol",required = false)CookingEquipmentSymbol symbol
            );

    @Operation(
            summary = "조리도구 세팅 API",
            description = "서버 DB에 조리도구 데이터 초기세팅"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조리도구 세팅 성공"
            ),
    })
    @PostMapping("/init")
    GlobalResponse insertCookingEquipment();
}
