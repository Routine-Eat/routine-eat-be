package com.likelion.routineeatbe.domain.user.service;

import com.likelion.routineeatbe.domain.user.dto.request.CreateUserRequest;
import com.likelion.routineeatbe.domain.user.dto.request.UpdateUserRequest;
import com.likelion.routineeatbe.domain.user.dto.response.UserResponse;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.exception.UserErrorCode;
import com.likelion.routineeatbe.domain.user.exception.UserFoodIngredientErrorCode;
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

    /**
     * - 모든 사용자 조회
     * @return
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(){
        List<User> users=userRepository.findAll();
        return users.stream()
                .map(UserResponse::fromUserEntity)
                .toList();
    }

    /**
     * - 사용자 프로필 정보 변경
     * - 현재는 사용자 요리 실력만 변경 가능
     * - 추후 확장 고려
     * @param userId 사용자 id
     * @param request 사용자 데이터
     * @return 사용자 응답 dto로 변환
     */
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request){
        User user=userRepository.findById(userId)
                .orElseThrow(()-> new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER));

        user.updateSkillLevel(request.skillLevel());

        return UserResponse.fromUserEntity(user);
    }

    @Transactional
    public UserResponse getUserByLoginNumber(String loginNUmber){
        User user=userRepository.findByLoginNumber(loginNUmber)
                .orElseThrow(()->new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER));
        return UserResponse.fromUserEntity(user);
    }

    /**
     * 사용자 삭제 API
     * - CascadeType.REMOVE 설정에 의해 연관된 모든 하위 엔티티가 함께 삭제됨
     * @param userId 삭제할 사용자 id
     */
    @Transactional
    public void deleteUserById(Long userId) {
        // 1. 사용자 존재 여부 확인 (CustomException으로 통일)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER));

        // 2. 사용자 삭제
        userRepository.delete(user);
    }
}
