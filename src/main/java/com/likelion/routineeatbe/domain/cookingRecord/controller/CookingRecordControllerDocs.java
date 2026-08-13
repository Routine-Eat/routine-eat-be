package com.likelion.routineeatbe.domain.cookingRecord.controller;

import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingStartReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.NextCookingStepResDto;
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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Cooking Record", description = "요리 기록 API")
@RequestMapping("/api/v1/cooking-records")
public interface CookingRecordControllerDocs {

    @Operation(
            summary = "요리 시작",
            description = """
                    사용자와 레시피 정보를 기반으로 맞춤 요리 단계와 세션을 생성합니다.

                    [Query Parameter]
                    - userNumber: 4자리 사용자 고유 식별번호

                    [Request Body]
                    - recipeId: 요리를 시작할 레시피 PK
                    - servings: 요리할 인분 수
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "요리 시작 성공",
                    content = @Content(schema = @Schema(implementation = CookingStartResDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 또는 레시피를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "409", description = "진행 중 또는 완료된 요리 존재", content = @Content),
            @ApiResponse(responseCode = "502", description = "Gemini 응답 오류", content = @Content),
            @ApiResponse(responseCode = "503", description = "Gemini 호출 한도 초과", content = @Content),
            @ApiResponse(responseCode = "504", description = "Gemini 응답 시간 초과", content = @Content)
    })
    @PostMapping
    ResponseEntity<GlobalResponse<CookingStartResDto>> startCooking(
            @Parameter(description = "사용자 고유 식별번호", required = true)
            @NotBlank(message = "사용자 고유 식별번호는 필수입니다.")
            @Size(min = 4, max = 4, message = "사용자 고유 식별번호는 4자리여야 합니다.")
            @Pattern(regexp = "^[0-9]{4}$", message = "사용자 고유 식별번호는 숫자 4자리여야 합니다.")
            @RequestParam("userNumber") String userNumber,
            @Valid @RequestBody CookingStartReqDto request
    );

    @Operation(
            summary = "다음 요리 단계로 이동",
            description = """
                    진행 중인 요리 세션을 다음 단계로 이동하고 해당 단계의 상세 정보를 반환합니다.
                    현재 단계가 마지막 단계이면 요리 세션을 완료 상태로 변경합니다.

                    [Path Variable]
                    - cookingRecordId: 요리 기록 PK

                    [Query Parameter]
                    - userNumber: 4자리 사용자 고유 식별번호
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "다음 요리 단계 이동 또는 요리 완료 성공",
                    content = @Content(schema = @Schema(implementation = NextCookingStepResDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자, 요리 기록 또는 단계를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "409", description = "진행 중인 세션이 아니거나 단계 상태가 잘못됨", content = @Content)
    })
    @PostMapping("/{cookingRecordId}/cooking-session/cooking-steps/next")
    ResponseEntity<GlobalResponse<NextCookingStepResDto>> moveToNextCookingStep(
            @Parameter(description = "요리 기록 PK", required = true)
            @Positive(message = "요리 기록 PK는 양수여야 합니다.")
            @PathVariable("cookingRecordId") Long cookingRecordId,
            @Parameter(description = "사용자 고유 식별번호", required = true)
            @NotBlank(message = "사용자 고유 식별번호는 필수입니다.")
            @Size(min = 4, max = 4, message = "사용자 고유 식별번호는 4자리여야 합니다.")
            @Pattern(regexp = "^[0-9]{4}$", message = "사용자 고유 식별번호는 숫자 4자리여야 합니다.")
            @RequestParam("userNumber") String userNumber
    );
}
