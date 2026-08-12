package com.likelion.routineeatbe.domain.user.service;

import com.likelion.routineeatbe.domain.cookingEquipment.dto.response.CookingEquipmentResponse;
import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import com.likelion.routineeatbe.domain.cookingEquipment.repository.CookingEquipmentRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.entity.UserCookingEquipment;
import com.likelion.routineeatbe.domain.user.exception.UserCookingEquipmentErrorCode;
import com.likelion.routineeatbe.domain.user.exception.UserFoodIngredientErrorCode;
import com.likelion.routineeatbe.domain.user.repository.UserCookingEquipmentRepository;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCookingEquipmentService {
    private final UserRepository userRepository;
    private final CookingEquipmentRepository cookingEquipmentRepository;
    private final UserCookingEquipmentRepository userCookingEquipmentRepository;

    /**
     * - 사용자-조리도구 관계 생성
     * - 사용자 아이디와 도구 아이디 리스트로 관계 생성
     * @param userId 사용자 id
     * @param equipmentIdList 조리도구 id 리스트
     * @return 조리도구 반환 dto 리스트
     */
    public List<CookingEquipmentResponse> createUserCookingEquipment(Long userId,List<Long> equipmentIdList){
        User user=userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER));

        List<CookingEquipment> cookingEquipments= cookingEquipmentRepository.findAllById(equipmentIdList);

        // 요청한 ID 개수와 조회된 개수 검증 (유효하지 않은 ID가 섞인 경우 예외 처리)
        if (cookingEquipments.size() != equipmentIdList.size()) {
            throw new CustomException(UserCookingEquipmentErrorCode.NOT_EXIST_COOKINGEQUIPMENT);
        }

        List<UserCookingEquipment> userCookingEquipments= cookingEquipments.stream()
                .map(cookingEquipment -> UserCookingEquipment.createUserCookingEquipment(user,cookingEquipment))
                .toList();
        userCookingEquipmentRepository.saveAll(userCookingEquipments);

        return cookingEquipments.stream()
                .map(CookingEquipmentResponse::from)
                .toList();
    }
}
