package com.likelion.routineeatbe.domain.user.controller;

import com.likelion.routineeatbe.domain.user.dto.request.CreateUserRequest;
import com.likelion.routineeatbe.domain.user.dto.response.UserResponse;
import com.likelion.routineeatbe.domain.user.service.UserService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController implements UserContorllerDocs{
    private final UserService userService;

    @Override
    public GlobalResponse<UserResponse> createUser(CreateUserRequest createUserRequest){
        UserResponse userResponse=userService.createUser(createUserRequest);
        return GlobalResponse.success(createUserRequest.loginNumber()+" 사용자가 성공적으로 생성되었습니다.",userResponse);
    }

}
