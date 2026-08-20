package com.likelion.routineeatbe.domain.favoriteRecipe.controller;

import com.likelion.routineeatbe.domain.favoriteRecipe.dto.request.FavoriteRecipeSearchReqDto;
import com.likelion.routineeatbe.domain.favoriteRecipe.dto.response.FavoriteRecipeListResDto;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Favorite Recipe", description = "레시피 찜 API")
@RequestMapping("/api/v1/recipes")
public interface FavoriteRecipeControllerDocs {

    @Operation(
            summary = "레시피 찜 등록",
            description = """
                    사용자가 선택한 레시피를 찜 목록에 등록합니다.

                    [Path Variable]
                    - recipeId: 레시피 PK

                    [Query Parameter]
                    - userNumber: 사용자 고유 식별번호
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "레시피 찜 성공",
                    content = @Content(schema = @Schema(implementation = GlobalResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 또는 레시피를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 찜한 레시피", content = @Content)
    })
    @PostMapping("/{recipeId}/favorites")
    ResponseEntity<GlobalResponse<Void>> addFavorite(
            @Parameter(description = "레시피 PK", required = true)
            @Positive(message = "레시피 PK는 양수여야 합니다.")
            @PathVariable("recipeId") Long recipeId,
            @Parameter(description = "사용자 고유 식별번호", required = true)
            @NotNull(message = "사용자 고유 식별번호는 필수입니다.")
            @Positive(message = "사용자 고유 식별번호는 양수여야 합니다.")
            @RequestParam("userNumber") Integer userNumber
    );

    @Operation(
            summary = "레시피 찜 해제",
            description = """
                    사용자가 찜한 레시피를 찜 목록에서 해제합니다.

                    [Path Variable]
                    - recipeId: 레시피 PK

                    [Query Parameter]
                    - userNumber: 사용자 고유 식별번호
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "레시피 찜 해제 성공",
                    content = @Content(schema = @Schema(implementation = GlobalResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자, 레시피 또는 찜 정보를 찾을 수 없음", content = @Content)
    })
    @DeleteMapping("/{recipeId}/favorites")
    ResponseEntity<GlobalResponse<Void>> removeFavorite(
            @Parameter(description = "레시피 PK", required = true)
            @Positive(message = "레시피 PK는 양수여야 합니다.")
            @PathVariable("recipeId") Long recipeId,
            @Parameter(description = "사용자 고유 식별번호", required = true)
            @NotBlank(message = "사용자 고유 식별번호는 필수입니다.")
            @Size(min = 4, max = 4, message = "사용자 고유 식별번호는 4자리여야 합니다.")
            @Pattern(regexp = "^[0-9]{4}$", message = "사용자 고유 식별번호는 숫자 4자리여야 합니다.")
            @RequestParam("userNumber") String userNumber
    );

    @Operation(
            summary = "찜한 레시피 조회",
            description = """
                    사용자가 찜한 레시피를 최신 찜순으로 조회합니다.

                    [Query Parameter]
                    - userNumber: 사용자 고유 식별번호
                    - cursor: 1부터 시작하는 조회 위치, 기본값 1
                    - size: 한 번에 조회할 개수, 기본값 10, 최대 100
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "찜한 레시피 조회 성공",
                    content = @Content(schema = @Schema(implementation = FavoriteRecipeListResDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 조건", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/favorites")
    ResponseEntity<GlobalResponse<FavoriteRecipeListResDto>> getFavoriteRecipes(
            @Valid @ModelAttribute FavoriteRecipeSearchReqDto request
    );
}
