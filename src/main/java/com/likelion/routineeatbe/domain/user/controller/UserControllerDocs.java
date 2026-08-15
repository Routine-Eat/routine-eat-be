package com.likelion.routineeatbe.domain.user.controller;

import com.likelion.routineeatbe.domain.cookingEquipment.dto.response.CookingEquipmentResponse;
import com.likelion.routineeatbe.domain.user.dto.request.*;
import com.likelion.routineeatbe.domain.user.dto.response.UserFoodIngredientResponse;
import com.likelion.routineeatbe.domain.user.dto.response.UserOnboardingResponse;
import com.likelion.routineeatbe.domain.user.dto.response.UserResponse;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User", description = "사용자 관리 API")
@RequestMapping("/api/v1/users")
public interface UserControllerDocs {

    @Operation(
            summary = "사용자 목록 조회",
            description = "전체 사용자를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 리스트 조회 성공"
            ),
    })
    @GetMapping
    GlobalResponse<List<UserResponse>> getAllUsers();

    @Operation(
            summary = "사용자-식재료 목록 조회",
            description = """
                    사용자와 관련된 식재료를 조회합니다. \n
                    relationType : \n
                        1. 제외 (EXCEPTION) \n
                        2. 보유 (OWN) \n
                        3. 예약 (RESERVATION) \n
                    관계를 전달하지 않을 시 관련된 식재료 전체 조회
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자-식재료 관계 리스트 조회 성공"
            ),
    })
    @GetMapping("/{userId}/food-ingredients")
    GlobalResponse<UserFoodIngredientResponse> getUserFoodIngredients(
            @NotNull @PathVariable("userId") Long userId,
            @RequestParam(name = "type", required = false)
            @Parameter(description = "관계 타입 (선택)")
            UserFoodIngredientType relationType
    );

    @Operation(
            summary = "사용자 생성",
            description = """
                    loginNumber을 입력하면 자동으로 중복 검사 후 실행됩니다. \n
                    DB에 중복된 값이 없다면 사용자가 생성됩니다
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "사용자 생성 성공"
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값 형식", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 인증 번호", content = @Content)
    })
    @PostMapping
    GlobalResponse<UserResponse> createUser(
            @Valid
            @RequestBody CreateUserRequest createUserRequest
    );

    @Operation(
            summary = "사용자-식재료 관계 생성",
            description = """
                    relationType과 그에 해당하는 식재료 리스트를 전달하여 DB에 등록 \n
                    relationType : \n
                        1. 제외 (EXCEPTION) \n
                        2. 보유 (OWN) \n
                        3. 예약 (RESERVATION) \n
                    - 예약은 장보기에 저장 용도 \n
                    - 식재료 리스트는 OWN만 주/부 보유량 포함하여 날릴 수 있음, 나머지는 X
                    - 주/부 보유량은 선택사항
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "사용자-식재료 관계 생성 성공"
            ),
            @ApiResponse(responseCode = "4041", description = "잘못된 사용자 id 형식", content = @Content),
            @ApiResponse(responseCode = "4042", description = "잘못된 식재료 id 포함", content = @Content),
    })
    @PostMapping("/{userId}/food-ingredients")
    GlobalResponse<UserFoodIngredientResponse> createUserFoodIngredient(
            @PathVariable("userId") Long userId,
            @Valid
            @RequestBody CreateUserFoodIngredientRequest createUserFoodIngredientRequest
    );

    @Operation(
            summary = "사용자-식재료 보유 관계 보유량 수정",
            description = """
                    1. 사용자가 보유(OWN)하고 있는 식재료 id와 수정할 보유량 값 전달 \n
                    2. 주 보유량 값 필수, 부 보유량 값은 선택
                    3. 부 보유량은 안 보내면 기존 값 유지, 보내야 덮어씌워짐
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "203",
                    description = "사용자-식재료 보유 관계 보유량 수정 성공"
            ),
            @ApiResponse(responseCode = "4041", description = "잘못된 사용자 id 형식", content = @Content),
            @ApiResponse(responseCode = "4042", description = "잘못된 식재료 id 포함", content = @Content),
    })
    @PatchMapping("/{userId}/food-ingredients/amount")
    GlobalResponse<UserFoodIngredientResponse> updateOwnFoodIngredientAmount(
            @PathVariable("userId") Long userId,
            @Valid
            @RequestBody UpdateOwnFoodIngredientAmountRequest request
    );

    @Operation(
            summary = "사용자-식재료 관계 삭제",
            description = """
                    relationType과 그에 해당하는 식재료 리스트를 전달하여 DB에서 삭제 \n
                    relationType : \n
                        1. 제외 (EXCEPTION) \n
                        2. 보유 (OWN) \n
                        3. 예약 (RESERVATION) \n
                    예약은 장보기에 저장 용도 \n
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "사용자-식재료 관계 삭제 성공"
            ),
            @ApiResponse(responseCode = "4041", description = "잘못된 사용자 id 형식", content = @Content),
            @ApiResponse(responseCode = "4042", description = "잘못된 식재료 id 포함", content = @Content),
    })
    @DeleteMapping("/{userId}/food-ingredients")
    GlobalResponse<Void> deleteUserFoodIngredient(
            @PathVariable("userId") Long userId,
            @Valid
            @RequestBody DeleteUserFoodIngredientRequest request
            );

    @Operation(
            summary = "사용자-조리도구 관계 생성",
            description = """
                    userId : 사용자 id \n
                    equipmentIdList : 조리도구 식별자 id 리스트
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "사용자-조리도구 관계 생성 성공"
            ),
            @ApiResponse(responseCode = "4041", description = "잘못된 사용자 id 형식", content = @Content),
            @ApiResponse(responseCode = "4042", description = "잘못된 조리도구 id 포함", content = @Content),
    })
    @PostMapping("/{userId}/cooking-equipments")
    GlobalResponse<List<CookingEquipmentResponse>> createUserCookingEquipment(
            @PathVariable("userId") Long userId,
            @Schema(description = "조리도구 아이디 리스트",example = "[1,2,3]")
            @RequestBody List<Long> equipmentIdList
    );

    @Operation(
            summary = "사용자-조리도구 목록 조회",
            description = """
                    userId : 사용자 id
                    특정 사용자가 보유한 조리도구 목록 조회
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자-조리도구 관계 리스트 조회 성공"
            ),
    })
    @GetMapping("/{userId}/cooking-equipments")
    GlobalResponse<List<CookingEquipmentResponse>> getUserCookingEquipment(
            @PathVariable("userId") Long userId
    );

    @Operation(
            summary = "사용자-조리도구 관계 삭제",
            description = """
                    userId에 해당하는 사용자의 보유 조리도구 목록 중 equipmentIdList 삭제 \n
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "사용자-조리도구 관계 삭제 성공"
            ),
            @ApiResponse(responseCode = "4041", description = "잘못된 사용자 id 형식", content = @Content),
            @ApiResponse(responseCode = "4042", description = "잘못된 조리도구 id 형식", content = @Content),
    })
    @DeleteMapping("/{userId}/cooking-equipments")
    GlobalResponse<Void> deleteUserCookingEquipment(
            @PathVariable("userId") Long userId,
            @Schema(description = "조리도구 아이디 리스트",example = "[1,2,3]")
            @RequestBody List<Long> equipmentIdList
    );

    @Operation(
            summary = "사용자 데이터 수정",
            description = """
                    userId에 해당하는 사용자의 데이터 수정 \n
                    Skill Level은 꼭 BEGINNER/AVAERAGE/PRO 중에 하나로 할것!!
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "사용자 프로필 데이터 수정 성공"
            ),
            @ApiResponse(responseCode = "4041", description = "잘못된 사용자 id 형식", content = @Content),
    })
    @PatchMapping("/{userId}")
    GlobalResponse<UserResponse> updateUserSkillLevel(
            @PathVariable("userId") Long userId,
            @RequestBody UpdateUserRequest request);

    @Operation(
            summary = "사용자 온보딩 데이터 저장",
            description = """
                    userId에 해당하는 사용자의 온보딩 데이터 저장 \n
                    Skill Level은 꼭 BEGINNER/AVAERAGE/PRO 중에 하나로 할것!! \n
                    알레르기/비선호/보유 식품 리스트는 없어도 통과됨 \n
                    조리환경 리스트는 최소 1개는 있어야함!
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "사용자 온보딩 데이터 저장 성공"
            ),
            @ApiResponse(responseCode = "4041", description = "잘못된 사용자 id 형식", content = @Content),
            @ApiResponse(responseCode = "4042", description = "잘못된 식재료 id 형식", content = @Content),
            @ApiResponse(responseCode = "4043", description = "잘못된 조리도구 id 형식", content = @Content),
    })
    @PostMapping("/{userId}/onboarding")
    GlobalResponse<UserOnboardingResponse> saveOnboardingData(
            @PathVariable("userId") Long userId,
            @RequestBody UserOnboardingRequest request
    );

    @Operation(
            summary = "사용자 단일 조회",
            description = """
                    loginNumber로 사용자 조회 API
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 단일 조회 성공"
            ),
            @ApiResponse(responseCode = "4041", description = "없는 사용자 loginNumber 형식", content = @Content),
    })
    @GetMapping("/{loginNumber}")
    GlobalResponse<UserResponse> getUserByLoginNumber(@PathVariable String loginNumber);
}
