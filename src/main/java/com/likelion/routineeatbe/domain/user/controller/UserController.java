package com.likelion.routineeatbe.domain.user.controller;

import com.likelion.routineeatbe.domain.user.dto.request.CreateUserFoodIngredientRequest;
import com.likelion.routineeatbe.domain.user.dto.request.CreateUserRequest;
import com.likelion.routineeatbe.domain.user.dto.response.UserFoodIngredientResponse;
import com.likelion.routineeatbe.domain.user.dto.response.UserResponse;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import com.likelion.routineeatbe.domain.user.service.UserFoodIngredientService;
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

    @Override
    public GlobalResponse<List<UserResponse>> getAllUsers(){
        List<UserResponse> userResponseList=userService.getAllUsers();
        return GlobalResponse.success("사용자 전체 조회에 성공했습니다.",userResponseList);
    }

    @Override
    public GlobalResponse<UserFoodIngredientResponse> getUserFoodIngredients(
            Long userId, UserFoodIngredientType relationType){
        UserFoodIngredientResponse userFoodIngredientResponse=
                userFoodIngredientService.getUserFoodIngredients(userId,relationType);
        return GlobalResponse.success("사용자와 관련된 식재료가 성공적으로 조회되었습니다.",userFoodIngredientResponse);
    }

    @Override
    public GlobalResponse<UserResponse> createUser(CreateUserRequest createUserRequest){
        UserResponse userResponse=userService.createUser(createUserRequest);
        return GlobalResponse.success(createUserRequest.loginNumber()+" 사용자가 성공적으로 생성되었습니다.",userResponse);
    }

    @Override
    public GlobalResponse<UserFoodIngredientResponse> createUserFoodIngredient(
            Long userId,
            CreateUserFoodIngredientRequest createUserFoodIngredientRequest){
        UserFoodIngredientResponse userFoodIngredientResponse=userFoodIngredientService.createUserFoodIngredient(userId,createUserFoodIngredientRequest);
        return GlobalResponse.success("사용자-식재료 관게 추가에 성공했습니다.",userFoodIngredientResponse);
    }
}
