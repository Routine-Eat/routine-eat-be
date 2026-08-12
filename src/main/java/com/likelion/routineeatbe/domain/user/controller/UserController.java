package com.likelion.routineeatbe.domain.user.controller;

import com.likelion.routineeatbe.domain.cookingEquipment.dto.response.CookingEquipmentResponse;
import com.likelion.routineeatbe.domain.user.dto.request.*;
import com.likelion.routineeatbe.domain.user.dto.response.UserFoodIngredientResponse;
import com.likelion.routineeatbe.domain.user.dto.response.UserOnboardingResponse;
import com.likelion.routineeatbe.domain.user.dto.response.UserResponse;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import com.likelion.routineeatbe.domain.user.service.UserCookingEquipmentService;
import com.likelion.routineeatbe.domain.user.service.UserFoodIngredientService;
import com.likelion.routineeatbe.domain.user.service.UserOnboardingService;
import com.likelion.routineeatbe.domain.user.service.UserService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {
    private final UserService userService;
    private final UserFoodIngredientService userFoodIngredientService;
    private final UserCookingEquipmentService userCookingEquipmentService;
    private final UserOnboardingService userOnboardingService;

    @Override
    public GlobalResponse<List<UserResponse>> getAllUsers(){
        List<UserResponse> userResponseList=userService.getAllUsers();
        return GlobalResponse.success(200,"사용자 전체 조회에 성공했습니다.",userResponseList);
    }

    @Override
    public GlobalResponse<UserFoodIngredientResponse> getUserFoodIngredients(
            Long userId, UserFoodIngredientType relationType){
        UserFoodIngredientResponse userFoodIngredientResponse=
                userFoodIngredientService.getUserFoodIngredients(userId,relationType);
        return GlobalResponse.success(200,"사용자와 관련된 식재료가 성공적으로 조회되었습니다.",userFoodIngredientResponse);
    }

    @Override
    public GlobalResponse<UserResponse> createUser(CreateUserRequest createUserRequest){
        UserResponse userResponse=userService.createUser(createUserRequest);
        return GlobalResponse.success(201,createUserRequest.loginNumber()+" 사용자가 성공적으로 생성되었습니다.",userResponse);
    }

    @Override
    public GlobalResponse<UserFoodIngredientResponse> createUserFoodIngredient(
            Long userId,
            CreateUserFoodIngredientRequest createUserFoodIngredientRequest){
        UserFoodIngredientResponse userFoodIngredientResponse=userFoodIngredientService.createUserFoodIngredient(userId,createUserFoodIngredientRequest);
        return GlobalResponse.success(201,"사용자-식재료 관게 추가에 성공했습니다.",userFoodIngredientResponse);
    }

    @Override
    public GlobalResponse<UserFoodIngredientResponse> updateOwnFoodIngredientAmount(Long userId, UpdateOwnFoodIngredientAmountRequest request){
        UserFoodIngredientResponse userFoodIngredientResponse=userFoodIngredientService.updateOwnFoodIngredientAmount(userId,request);
        return GlobalResponse.success(203,"식재료 보유량이 성공적으로 수정되었습니다.",userFoodIngredientResponse);
    }

    @Override
    public GlobalResponse deleteUserFoodIngredient(Long userId, DeleteUserFoodIngredientRequest request){
        userFoodIngredientService.deleteUserFoodIngredient(userId,request);
        return GlobalResponse.success(204,"사용자-식재료 관계가 성공적으로 삭제되었습니다.",null);
    }

    @Override
    public GlobalResponse<List<CookingEquipmentResponse>> createUserCookingEquipment(Long userId,List<Long> equipmentIdList){
        List<CookingEquipmentResponse> cookingEquipmentResponseList= userCookingEquipmentService.createUserCookingEquipment(userId,equipmentIdList);

        return GlobalResponse.success(201,"사용가-조리도구 관계 생성에 성공했습니다.",cookingEquipmentResponseList);
    }

    @Override
    public GlobalResponse<List<CookingEquipmentResponse>> getUserCookingEquipment(Long userId){
        List<CookingEquipmentResponse> cookingEquipmentResponseList=userCookingEquipmentService.getUserCookingEquipment(userId);
        return GlobalResponse.success(200,"사용자-조리도구 관계 조회가 성공했습니다.",cookingEquipmentResponseList);
    }

    @Override
    public GlobalResponse deleteUserCookingEquipment(Long userId,List<Long> equipmentIdList){
        userCookingEquipmentService.deleteUserCookEquipment(userId,equipmentIdList);
        return GlobalResponse.success(204,"사용자-조리도구 관계 삭제에 성공했습니다.",null);
    }

    @Override
    public GlobalResponse<UserResponse> updateUserSkillLevel(Long userId, UpdateUserRequest request){
        UserResponse userResponse= userService.updateUser(userId,request);

        return GlobalResponse.success(203,"사용자 정보 변경에 성공했습니다.",userResponse);
    }

    @Override
    public GlobalResponse<UserOnboardingResponse> saveOnboardingData(Long userId,UserOnboardingRequest request){
        UserOnboardingResponse userOnboardingResponse=userOnboardingService.saveOnboardingData(userId,request);

        return GlobalResponse.success(201,"사용자 온보딩 데이터 저장에 성공했습니다.",userOnboardingResponse);
    }
}
