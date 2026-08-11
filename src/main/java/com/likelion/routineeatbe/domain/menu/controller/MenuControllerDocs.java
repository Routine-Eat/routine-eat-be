package com.likelion.routineeatbe.domain.menu.controller;

import com.likelion.routineeatbe.domain.menu.dto.response.InitMenuAndRecipeCookingEquipmentResDto;
import com.likelion.routineeatbe.domain.menu.dto.response.InitMenuAndRecipeFoodIngredientResDto;
import com.likelion.routineeatbe.domain.menu.dto.response.InitMenuDifficultyLevelResDto;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Menu", description = "메뉴 관리 API")
@RequestMapping("/api/v1/menus")
public interface MenuControllerDocs {

    @Operation(
            summary = "식품안전나라 메뉴 및 레시피 일괄 저장",
            description = """
                    식품안전나라 조리식품 API의 메뉴, 레시피, 조리 단계를 저장합니다.

                    - startIdx와 endIdx를 모두 생략하면 전체 데이터를 반복 조회합니다.
                    - 두 값을 모두 입력하면 지정한 범위만 조회합니다.
                    - 둘 중 하나만 입력하면 잘못된 요청으로 처리합니다.
                    - 신규 메뉴만 Gemini로 메뉴 종류, 추천 유형, 예상 조리시간을 생성한 후 저장합니다.
                    - 식품안전나라 또는 Gemini 호출 중 하나라도 실패하면 저장하지 않습니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "메뉴 및 레시피 저장 성공",
                    content = @Content(schema = @Schema(implementation = GlobalResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 범위", content = @Content),
            @ApiResponse(responseCode = "502", description = "외부 API 호출 또는 응답 오류", content = @Content),
            @ApiResponse(responseCode = "504", description = "외부 API 응답 시간 초과", content = @Content)
    })
    @PostMapping("/crawl/food-safety-korea")
    GlobalResponse<Void> crawlFoodSafetyKorea(
            @Parameter(description = "조회 시작 위치", example = "1", required = false)
            @RequestParam(required = false) Integer startIdx,
            @Parameter(description = "조회 종료 위치", example = "1000", required = false)
            @RequestParam(required = false) Integer endIdx
    );

    @Operation(
            summary = "메뉴별 1인분 음식 재료 필요량 초기화",
            description = """
                    DB에 저장된 메뉴 정보와 음식 재료 데이터를 Gemini로 분석하여
                    메뉴별 1인분 음식 재료 필요량을 초기화합니다.

                    - 이미 음식 재료 필요량이 저장된 메뉴는 제외합니다.
                    - 모든 Gemini 호출이 성공한 후 일괄 저장합니다.
                    - initCount는 새로 저장된 메뉴 음식 재료 데이터 개수입니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "메뉴별 음식 재료 필요량 초기화 성공",
                    content = @Content(schema = @Schema(implementation = InitMenuAndRecipeFoodIngredientResDto.class))
            ),
            @ApiResponse(responseCode = "409", description = "음식 재료 기준 데이터 없음 또는 초기화 충돌", content = @Content),
            @ApiResponse(responseCode = "502", description = "Gemini API 호출 또는 응답 오류", content = @Content),
            @ApiResponse(responseCode = "503", description = "Gemini API 호출 한도 초과", content = @Content),
            @ApiResponse(responseCode = "504", description = "Gemini API 응답 시간 초과", content = @Content)
    })
    @PostMapping("/food-ingredients/init")
    ResponseEntity<GlobalResponse<InitMenuAndRecipeFoodIngredientResDto>>
            initMenuAndRecipeFoodIngredients();

    @Operation(
            summary = "레시피별 필요 조리 도구 초기화",
            description = """
                    DB에 저장된 기본 레시피의 메뉴 정보와 조리 단계를 Gemini로 분석하여
                    레시피별 필요한 조리 도구 연결 데이터를 초기화합니다.

                    - 이미 조리 도구가 저장된 레시피는 제외합니다.
                    - RecipeType.BASIC 레시피만 초기화합니다.
                    - 모든 Gemini 호출이 성공한 후 일괄 저장합니다.
                    - initCount는 새로 저장된 레시피 조리 도구 연결 데이터 개수입니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "레시피별 조리 도구 초기화 성공",
                    content = @Content(
                            schema = @Schema(
                                    implementation = InitMenuAndRecipeCookingEquipmentResDto.class
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "레시피 또는 조리 도구 없음", content = @Content),
            @ApiResponse(responseCode = "409", description = "조리 도구 기준 데이터 없음 또는 초기화 충돌", content = @Content),
            @ApiResponse(responseCode = "502", description = "Gemini API 호출 또는 응답 오류", content = @Content),
            @ApiResponse(responseCode = "503", description = "Gemini API 호출 한도 초과", content = @Content),
            @ApiResponse(responseCode = "504", description = "Gemini API 응답 시간 초과", content = @Content)
    })
    @PostMapping("/cooking-equipments/init")
    ResponseEntity<GlobalResponse<InitMenuAndRecipeCookingEquipmentResDto>>
            initMenuAndRecipeCookingEquipments();

    @Operation(
            summary = "메뉴 난이도 초기화",
            description = """
                    DB에 저장된 전체 메뉴의 난이도를 자동으로 계산하여 갱신합니다.

                    - 조리 시간, 기본 레시피 단계 수, 음식 재료 수를 각각 1~5점으로 계산합니다.
                    - 세 점수의 합계를 구간별 DifficultyLevel.LEVEL_1~LEVEL_5로 저장합니다.
                    - initCount는 난이도를 다시 계산한 메뉴 개수입니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "메뉴 난이도 초기화 성공",
                    content = @Content(
                            schema = @Schema(implementation = InitMenuDifficultyLevelResDto.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "메뉴 난이도 초기화 중 서버 오류", content = @Content)
    })
    @PatchMapping("/difficulty-level/init")
    ResponseEntity<GlobalResponse<InitMenuDifficultyLevelResDto>> initMenuDifficultyLevels();
}
