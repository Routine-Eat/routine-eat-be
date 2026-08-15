package com.likelion.routineeatbe.domain.cookingRecord.controller;

import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingResultSaveReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingStartReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordFoodIngredientsResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingResultSaveResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Cooking Record", description = "요리 기록 API")
@RequestMapping("/api/v1/cooking-records")
public interface CookingRecordControllerDocs {

    @Operation(
            summary = "이번 요리에 사용한 음식 재료 양 조회",
            description = """
                    완료된 요리 기록에 대해 사용자의 현재 재료 보유량과 요리 후 예상 보유량을 조회합니다.
                    요리 후 예상 보유량은 현재 보유량에서 요리 시작 시 초기화한 사용량을 차감해 계산하며,
                    실제 사용자 재고 데이터는 변경하지 않습니다.

                    [Path Variable]
                    - cookingRecordId: 요리 기록 PK

                    [Query Parameter]
                    - userNumber: 4자리 사용자 고유 식별번호
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "이번 요리에 사용한 음식 재료 양 조회 성공",
                    content = @Content(
                            schema = @Schema(
                                    implementation = CookingRecordFoodIngredientsResDto.class
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 또는 요리 기록을 찾을 수 없음",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "요리 세션이 완료되지 않았거나 음식 재료가 초기화되지 않음",
                    content = @Content
            )
    })
    @GetMapping("/{cookingRecordId}/food-ingredients")
    ResponseEntity<GlobalResponse<CookingRecordFoodIngredientsResDto>> getFoodIngredients(
            @Parameter(description = "요리 기록 PK", required = true)
            @Positive(message = "요리 기록 PK는 양수여야 합니다.")
            @PathVariable("cookingRecordId") Long cookingRecordId,
            @Parameter(description = "사용자 고유 식별번호", required = true)
            @NotBlank(message = "사용자 고유 식별번호는 필수입니다.")
            @Size(min = 4, max = 4, message = "사용자 고유 식별번호는 4자리여야 합니다.")
            @Pattern(regexp = "^[0-9]{4}$", message = "사용자 고유 식별번호는 숫자 4자리여야 합니다.")
            @RequestParam("userNumber") String userNumber
    );

    @Operation(
            summary = "요리 결과 저장",
            description = """
                    사용자의 가장 최근 완료 요리 기록에 맛 평가와 실제 난이도를 저장합니다.
                    요청된 음식 재료의 실제 사용량을 먼저 수정하고 해당 값으로 사용자 보유량을 차감합니다.
                    수정 목록이 비어 있으면 요리 시작 시 초기화된 사용량으로 사용자 보유량을 차감합니다.
                    선택 이미지가 있으면 S3에 업로드하고 CloudFront URL을 기록합니다.

                    [Query Parameter]
                    - userNumber: 4자리 사용자 고유 식별번호

                    [Multipart Part]
                    - request: application/json 형식의 맛 평가, 난이도와 음식 재료 실제 사용량
                    - image: 선택 이미지 파일
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "요리 결과 저장 성공",
                    content = @Content(schema = @Schema(implementation = CookingResultSaveResDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청, 중복 음식 재료 또는 이미지", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자, 요리 기록 또는 요리 기록 음식 재료를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "409", description = "완료된 요리 기록 또는 세션이 없음", content = @Content),
            @ApiResponse(responseCode = "502", description = "이미지 업로드 실패", content = @Content)
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(
                            name = "request",
                            contentType = MediaType.APPLICATION_JSON_VALUE
                    )
            )
    )
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<GlobalResponse<CookingResultSaveResDto>> saveCookingResult(
            @Parameter(description = "사용자 고유 식별번호", required = true)
            @NotBlank(message = "사용자 고유 식별번호는 필수입니다.")
            @Size(min = 4, max = 4, message = "사용자 고유 식별번호는 4자리여야 합니다.")
            @Pattern(regexp = "^[0-9]{4}$", message = "사용자 고유 식별번호는 숫자 4자리여야 합니다.")
            @RequestParam("userNumber") String userNumber,
            @Parameter(description = "맛 평가, 실제 요리 난이도와 음식 재료 실제 사용량", required = true)
            @Valid @RequestPart("request") CookingResultSaveReqDto request,
            @Parameter(description = "선택 요리 결과 이미지")
            @RequestPart(value = "image", required = false) MultipartFile image
    );

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
                    content = @Content(schema = @Schema(implementation = CookingStepNavigationResDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자, 요리 기록 또는 단계를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "409", description = "진행 중인 세션이 아니거나 단계 상태가 잘못됨", content = @Content)
    })
    @PostMapping("/{cookingRecordId}/cooking-session/cooking-steps/next")
    ResponseEntity<GlobalResponse<CookingStepNavigationResDto>> moveToNextCookingStep(
            @Parameter(description = "요리 기록 PK", required = true)
            @Positive(message = "요리 기록 PK는 양수여야 합니다.")
            @PathVariable("cookingRecordId") Long cookingRecordId,
            @Parameter(description = "사용자 고유 식별번호", required = true)
            @NotBlank(message = "사용자 고유 식별번호는 필수입니다.")
            @Size(min = 4, max = 4, message = "사용자 고유 식별번호는 4자리여야 합니다.")
            @Pattern(regexp = "^[0-9]{4}$", message = "사용자 고유 식별번호는 숫자 4자리여야 합니다.")
            @RequestParam("userNumber") String userNumber
    );

    @Operation(
            summary = "이전 요리 단계로 이동",
            description = """
                    진행 중인 요리 세션을 이전 단계로 이동하고 해당 단계의 상세 정보를 반환합니다.
                    현재 단계가 1이면 단계를 변경하지 않습니다.

                    [Path Variable]
                    - cookingRecordId: 요리 기록 PK

                    [Query Parameter]
                    - userNumber: 4자리 사용자 고유 식별번호
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "이전 요리 단계 이동 또는 첫 단계 경계 처리 성공",
                    content = @Content(schema = @Schema(implementation = CookingStepNavigationResDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자, 요리 기록 또는 단계를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "409", description = "진행 중인 세션이 아니거나 단계 상태가 잘못됨", content = @Content)
    })
    @PostMapping("/{cookingRecordId}/cooking-session/cooking-steps/prev")
    ResponseEntity<GlobalResponse<CookingStepNavigationResDto>> moveToPreviousCookingStep(
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
