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

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    /* return값 UserResponse 포장 함수 */
    private UserResponse toUseResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .loginNumber(user.getLoginNumber())
                .skillLevel(user.getSkillLevel())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * User entity 생성 로직
     * - 식별자 id, 생성날짜는 자동 생성
     * - loginNumber만 받아서 중복 검사 후 생성
     * @param createUserRequest
     * @return UserResponse를 거친 User Entity
     */
    public UserResponse createUser(CreateUserRequest createUserRequest){
        if(userRepository.existsByLoginNumber(createUserRequest.getLoginNumber())){
            throw new CustomException(UserErrorCode.DUPLICATE_LOGIN_NUMBER);
        }

        User user=User.builder()
                .loginNumber(createUserRequest.getLoginNumber())
                .build();
        User savedUser=userRepository.save(user);
        return toUseResponse(savedUser);
    }
}
