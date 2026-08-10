package com.likelion.routineeatbe.domain.user.service;

import com.likelion.routineeatbe.domain.user.dto.request.CreateUserRequest;
import com.likelion.routineeatbe.domain.user.dto.response.UserResponse;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.exception.UserErrorCode;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    /**
     * User entity 생성 로직
     * - 식별자 id, 생성날짜는 자동 생성
     * - loginNumber만 받아서 중복 검사 후 생성
     * @param createUserRequest
     * @return UserResponse를 거친 User Entity
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest){
        String loginNumber= createUserRequest.loginNumber();
        if(userRepository.existsByLoginNumber(loginNumber)){
            throw new CustomException(UserErrorCode.DUPLICATE_LOGIN_NUMBER);
        }
        User user=User.createUser(loginNumber);
        User savedUser=userRepository.save(user);
        return UserResponse.fromUserEntity(savedUser);
    }

    @Transactional
    public List<UserResponse> getAllUsers(){
        List<User> users=userRepository.findAll();
        return users.stream()
                .map(UserResponse::fromUserEntity)
                .toList();
    }
}
