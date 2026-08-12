package com.likelion.routineeatbe.domain.recipe.controller;

import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeSearchResponseDto;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Recipe", description = "레시피 조회 API")
@RequestMapping("/api/v1/recipes")
public interface RecipeControllerDocs {

    @Operation(
            summary = "전체 레시피 목록 조회",
            description = """
                    사용자 보유 재료와 필터 조건을 기준으로 전체 및 추천 유형별 레시피 목록을 조회합니다.

                    [Query Parameter]
                    - userNumber: 사용자 고유 식별번호
                    - cursor: 1부터 시작하는 조회 위치, 다음 요청은 응답의 nextCursor 사용
                    - size: 유형별 1회 조회 개수, 기본값 10, 최대 100
                    - timeRequired: 입력한 시간 이하의 레시피 조회
                    - difficultyLevel: LEVEL_1 ~ LEVEL_5
                    - category: KOREAN | CHINESE | JAPANESE | WESTERN | OTHER
                    - sortType: DEFAULT | FOOD_INTEGRATION
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "전체 레시피 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = RecipeSearchResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 조건", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    @GetMapping
    ResponseEntity<GlobalResponse<RecipeSearchResponseDto>> getRecipes(
            @Valid @ModelAttribute RecipeSearchRequestDto request
    );
}
