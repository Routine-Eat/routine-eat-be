package com.likelion.routineeatbe.domain.user.controller;

import com.likelion.routineeatbe.domain.user.dto.request.CreateUserFoodIngredientRequest;
import com.likelion.routineeatbe.domain.user.dto.request.CreateUserRequest;
import com.likelion.routineeatbe.domain.user.dto.response.UserFoodIngredientResponse;
import com.likelion.routineeatbe.domain.user.dto.response.UserResponse;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
                    사용자와 관련된 식재료를 조회합니다.
                    relationType : 
                        1. 알러지 (ALLERGY)
                        2. 비선호 (DISLIKE)
                        3. 보유 (OWN)
                        4. 예약 (RESERVATION)
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
                    loginNumber을 입력하면 자동으로 중복 검사 후 실행됩니다
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
                    relationType과 그에 해당하는 식재료 리스트를 전달하여 DB에 등록
                    relationType : 
                        1. 알러지 (ALLERGY)
                        2. 비선호 (DISLIKE)
                        3. 보유 (OWN)
                        4. 예약 (RESERVATION)
                    예약은 장보기에 저장 용도
                    식재료 리스트는 OWN만 주/부 보유량 포함하여 날릴 수 있음, 나머지는 X
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
}
