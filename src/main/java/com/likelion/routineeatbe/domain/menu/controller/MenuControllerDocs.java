package com.likelion.routineeatbe.domain.menu.controller;

import com.likelion.routineeatbe.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Menu", description = "메뉴 관리 API")
@RequestMapping("/api/v1/menu")
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
}
