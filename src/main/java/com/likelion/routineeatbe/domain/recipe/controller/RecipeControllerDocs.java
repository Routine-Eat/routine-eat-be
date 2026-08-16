package com.likelion.routineeatbe.domain.recipe.controller;

import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeDetailReqDto;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeKeywordSearchReqDto;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.AiRecipeRecommendResponse;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeDetailResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeKeywordSearchResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeSearchResponseDto;
import com.likelion.routineeatbe.global.response.CursorSliceResponse;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Recipe", description = "레시피 조회 API")
@RequestMapping("/api/v1/recipes")
public interface RecipeControllerDocs {

    @Operation(
            summary = "레시피 상세 조회",
            description = """
                    레시피 기본 정보와 인분별 필요 재료, 사용자 보유량을 제외한 추가 재료 및 유사 레시피를 조회합니다.

                    [Path Variable]
                    - recipeId: 레시피 PK

                    [Query Parameter]
                    - userNumber: 사용자 고유 식별번호
                    - servings: 인분 수, 기본값 1
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "레시피 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = RecipeDetailResDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 조건", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 또는 레시피를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/{recipeId}")
    ResponseEntity<GlobalResponse<RecipeDetailResDto>> getRecipeDetail(
            @Parameter(description = "레시피 PK", required = true)
            @PathVariable Long recipeId,
            @Valid @ModelAttribute RecipeDetailReqDto request
    );

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

    @Operation(
            summary = "검색어 기반 레시피 검색",
            description = """
                    메뉴/레시피명에 검색어가 포함된 레시피를 일치도 순으로 조회합니다.

                    [Query Parameter]
                    - userNumber: 사용자 고유 식별번호
                    - searchWord: 메뉴/레시피명 검색어
                    - cursor: 1부터 시작하는 조회 위치, 다음 요청은 응답의 nextCursor 사용
                    - size: 1회 조회 개수, 기본값 10, 최대 100
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "검색어 기반 레시피 검색 성공",
                    content = @Content(schema = @Schema(implementation = CursorSliceResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 검색 조건", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/search")
    ResponseEntity<GlobalResponse<CursorSliceResponse<RecipeKeywordSearchResDto>>> searchRecipesByMenuName(
            @Valid @ModelAttribute RecipeKeywordSearchReqDto request
    );

    @Operation(
            summary = "AI 레시피 단일 추천 API",
            description = """
                    AI 레시피 단일 추천 API
                    메뉴 id,이름,썸네일 url/레시피 id/추천 이유 반환
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "AI 레시피 단일 추천 성공",
                    content = @Content(schema = @Schema(implementation = CursorSliceResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/ai-recommend/{userId}")
    GlobalResponse<AiRecipeRecommendResponse> getAiRecipeRecommendSingle(
            @PathVariable Long userId
    );

}
