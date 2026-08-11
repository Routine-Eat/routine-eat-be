package com.likelion.routineeatbe.domain.user.service;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.repository.FoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.dto.request.CreateUserFoodIngredientRequest;
import com.likelion.routineeatbe.domain.user.dto.request.DeleteUserFoodIngredientRequest;
import com.likelion.routineeatbe.domain.user.dto.response.UserFoodIngredientResponse;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import com.likelion.routineeatbe.domain.user.exception.UserFoodIngredientErrorCode;
import com.likelion.routineeatbe.domain.user.repository.UserFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserFoodIngredientService {
    private final UserRepository userRepository;
    private final FoodIngredientRepository foodIngredientRepository;
    private final UserFoodIngredientRepository userFoodIngredientRepository;

    /**
     * 1. userId로 유저 데이터 조회
     * 2. request에서 받은 foodIngredientList로 식재료 id 리스트 뽑기
     *  2-1. id 리스트로 식재료 데이터 가져오기
     *  2-2. id 개수 카운트
     *  2-3. id와 객체 맵핑
     * 3. UserFoodIngredient 리스트로 빌드
     * 4. 레포지토리에 저장
     * 5. 저장한 객체 리스트 UserFoodIngredientResponse.of로 포장
     * @param userId
     * @param request
     * @return
     */
    @Transactional
    public UserFoodIngredientResponse createUserFoodIngredient(Long userId,CreateUserFoodIngredientRequest request){
        User user=userRepository.findById(userId) /* 1. userId로 유저 데이터 조회 */
                .orElseThrow(() -> new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER));

        /* 2. request에서 받은 foodIngredientList로 식재료 id 리스트 뽑기 */
        List<Long> requestedIds = request.foodIngredientList().stream()
                .map(CreateUserFoodIngredientRequest.FoodIngredientDto::foodIngredientId)
                .toList();
        /* 2-1. id 리스트로 식재료 데이터 가져오기 */
        List<FoodIngredient> foodIngredients=foodIngredientRepository.findAllById(requestedIds);
        /* 2-2. id 개수 카운트 */
        long uniqueRequestedCount = requestedIds.stream().distinct().count();
        /* 2-3. id와 객체 맵핑 */
        Map<Long, FoodIngredient> foodIngredientMap = foodIngredientRepository.findAllById(requestedIds).stream()
                .collect(Collectors.toMap(FoodIngredient::getId, Function.identity()));

        if (foodIngredients.size() != uniqueRequestedCount) {
            throw new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_FOODINGREDIENT);
        }
        /* 3. UserFoodIngredient 리스트로 빌드 */
        List<UserFoodIngredient> userFoodIngredients = request.foodIngredientList().stream()
                .map(itemDto -> UserFoodIngredient.createUserFoodIngredient(
                        user,
                        foodIngredientMap.get(itemDto.foodIngredientId()), // Map에서 식재료 객체 꺼내기
                        request.relationType(),
                        itemDto.primaryAmountValue(),   // 요청받은 기본 단위 수량
                        itemDto.secondaryAmountValue()  // 요청받은 보조 단위 수량
                ))
                .toList();
        /* 4. 레포지토리에 저장 */
        List<UserFoodIngredient> savedUserFoodIngredients = userFoodIngredientRepository.saveAll(userFoodIngredients);
        /* 5. 저장한 객체 리스트 UserFoodIngredientResponse.of로 포장 */
        return UserFoodIngredientResponse.of(request.relationType(),savedUserFoodIngredients);
    }

    /**
     * - 사용자 id 기반 관계 테이블 조회
     * - 관계 타입 필터링, 없으면 전체 조회
     * @param userId
     * @param relationType
     * @return
     */
    @Transactional(readOnly = true)
    public UserFoodIngredientResponse getUserFoodIngredients(Long userId, UserFoodIngredientType relationType){

        // 1. 사용자 존재 유무 확인
        if (!userRepository.existsById(userId)) {
            throw new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER);
        }

        // 2. relationType 전달 여부에 따른 조건 조회
        List<UserFoodIngredient> userFoodIngredients;
        if (relationType != null) {
            userFoodIngredients = userFoodIngredientRepository.findByUserIdAndRelationType(userId, relationType);
        } else {
            userFoodIngredients = userFoodIngredientRepository.findByUserId(userId);
        }

        // 3. DTO 변환 및 반환 (relationType이 null이면 DTO의 type 필드도 null로 나옴)
        return UserFoodIngredientResponse.of(relationType, userFoodIngredients);
    }

    /**
     * - 사용자 아이디, 관계 타입, 관계에 해당하는 식재료 id 리스트로 찾아서 삭제
     * @param userId
     * @param request
     */
    @Transactional
    public void deleteUserFoodIngredient(Long userId,DeleteUserFoodIngredientRequest request){
        userFoodIngredientRepository.deleteByUserIdAndTypeAndIngredientIds(
                userId,
                request.relationType(),
                request.foodIngredientList()
        );
    }
}
