package com.likelion.routineeatbe.domain.recipe.controller;

import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeDetailReqDto;
import com.likelion.routineeatbe.domain.recipe.dto.request.CanCookReqDto;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeKeywordSearchReqDto;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeReRecommendRequest;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.AiRecipeRecommendResponse;
import com.likelion.routineeatbe.domain.recipe.dto.response.CanCookResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeDetailResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeKeywordSearchResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeSearchResponseDto;
import com.likelion.routineeatbe.domain.userSearchHistory.dto.request.UserSearchHistoryReqDto;
import com.likelion.routineeatbe.domain.userSearchHistory.dto.response.UserSearchHistoryResDto;
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
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recipe", description = "레시피 조회 API")
@RequestMapping("/api/v1/recipes")
public interface RecipeControllerDocs {

    @Operation(
            summary = "레시피 상세 조회",
            description = """
                    레시피 기본 정보와 인분별 필요 재료, 음식 재료 활용률, 사용자 보유량을 제외한 추가 재료 및 유사 레시피를 조회합니다.

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
                    사용자 보유 재료와 필터 조건을 기준으로 남은 재료, 간단 조리 및 추천 유형별 레시피 목록을 조회합니다.

                    [Query Parameter]
                    - userNumber: 사용자 고유 식별번호
                    - cursor: 1부터 시작하는 조회 위치, 다음 요청은 응답의 nextCursor 사용
                    - size: 유형별 1회 조회 개수, 기본값 10, 최대 100
                    - timeRequired: WITHIN_15_MINUTES | WITHIN_30_MINUTES | OVER_30_MINUTES
                      (15분 이하 | 15분 초과 30분 이하 | 30분 초과)
                    - difficultyLevel: LEVEL_1 ~ LEVEL_5
                    - category: KOREAN | CHINESE | JAPANESE | WESTERN | OTHER
                    - sortType: DEFAULT | FOOD_INTEGRATION

                    [Response]
                    - remainFoodIngredientName: OWN 재료 중 primaryAmountValue 합계가 가장 큰 음식 재료 이름
                    - remainFoodIngredient: OWN 재료 중 primaryAmountValue 합계가 가장 큰 재료를 포함하는 레시피
                    - simpleRecipe: 조리 시간이 15분 이하인 레시피
                    - dietRecipe, glutenFreeRecipe: 메뉴 추천 유형에 해당하는 레시피
                    - 모든 목록:
                      전체 필요 재료 중 사용자가 보유한 재료 비율(%)과 추가 구매 비용 제공
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
                    - timeRequired: WITHIN_15_MINUTES | WITHIN_30_MINUTES | OVER_30_MINUTES
                      (15분 이하 | 15분 초과 30분 이하 | 30분 초과)
                    - difficultyLevel: LEVEL_1 ~ LEVEL_5
                    - category: KOREAN | CHINESE | JAPANESE | WESTERN | OTHER
                    - sortType: DEFAULT | FOOD_INTEGRATION

                    [Sort]
                    - 완전 일치, 접두어 일치, 부분 일치, 짧은 메뉴명 순으로 우선 정렬
                    - DEFAULT: 요리 횟수, 레시피 PK 내림차순
                    - FOOD_INTEGRATION: 재료 활용률, 일치 재료 수, 요리 횟수, 레시피 PK 내림차순

                    [Response]
                    - foodIngredientUsingPercent: 전체 필요 재료 중 사용자가 보유한 재료 비율(%)
                    - isFavoriteRecipe: 사용자가 찜한 레시피 여부
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
            summary = "요리 가능 여부 조회",
            description = """
                    사용자 보유 재료와 요청 인분 수를 기준으로 해당 레시피의 요리 가능 여부를 조회합니다.
                    진행 중이거나 완료된 동일 레시피의 요리 세션이 있으면 요리할 수 없습니다.

                    [Path Variable]
                    - recipeId: 레시피 PK

                    [Query Parameter]
                    - userNumber: 4자리 사용자 고유 식별번호
                    - servings: 요리할 인분 수, 기본값 1
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "요리 가능 여부 조회 성공",
                    content = @Content(schema = @Schema(implementation = CanCookResDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 조건", content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 또는 레시피를 찾을 수 없음",
                    content = @Content
            )
    })
    @GetMapping("/{recipeId}/can-cook")
    ResponseEntity<GlobalResponse<CanCookResDto>> canCook(
            @Parameter(description = "레시피 PK", required = true)
            @Positive(message = "레시피 PK는 양수여야 합니다.")
            @PathVariable Long recipeId,
            @Valid @ModelAttribute CanCookReqDto request
    );

    @Operation(
            summary = "최근 검색 기록 조회",
            description = """
                    사용자 고유 식별번호를 기준으로 중복을 제거한 최근 검색 기록을 최신순으로 최대 5개 조회합니다.

                    [Query Parameter]
                    - userNumber: 사용자 고유 식별번호

                    [Response]
                    - searchHistoryList: 최신순 검색어 목록, 최대 5개
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "최근 검색 기록 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserSearchHistoryResDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 사용자 식별번호", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/searchHistory")
    ResponseEntity<GlobalResponse<UserSearchHistoryResDto>> getSearchHistories(
            @Valid @ModelAttribute UserSearchHistoryReqDto request
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

    @Operation(
            summary = "AI 레시피 재추천 API",
            description = """
                    AI 레시피 재추천 API \n
                    필터링 조건 \n
                        난이도 - LEVEL_1/LEVEL_2/LEVEL_3/LEVEL_4/LEVEL_5 \n
                        요리 열정(=걸리는 시간) - QUICK/MEDIUM/LONG \n
                        희망 재료 아이디 리스트 - [1,2,3] \n
                    모든 조건은 선택사항임 \n
                    메뉴 id,이름,썸네일 url/레시피 id/추천 이유 리스트 반환
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "AI 레시피 재추천 성공",
                    content = @Content(schema = @Schema(implementation = CursorSliceResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/ai-recommend/again/{userId}")
    GlobalResponse<List<AiRecipeRecommendResponse>> getAiRecipeRecommendThree(
            @PathVariable Long userId,
            @ParameterObject @ModelAttribute
            RecipeReRecommendRequest request
    );

}
