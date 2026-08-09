package com.likelion.routineeatbe.domain.user.controller;

import com.likelion.routineeatbe.domain.user.dto.request.CreateUserRequest;
import com.likelion.routineeatbe.domain.user.dto.response.UserResponse;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.domain.user.service.UserService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class UserController implements UserContorllerDocs{
    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService){
        this.userRepository=userRepository;
        this.userService=userService;
    }

    @Override
    public GlobalResponse<UserResponse> createUser(CreateUserRequest createUserRequest){
        UserResponse userResponse=userService.createUser(createUserRequest);
        return GlobalResponse.success(createUserRequest.getLoginNumber()+" 사용자가 성공적으로 생성되었습니다.",userResponse);
    }

}
