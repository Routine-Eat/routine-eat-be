package com.likelion.routineeatbe.domain.cookingEquipment.service;

import com.likelion.routineeatbe.domain.cookingEquipment.dto.response.CookingEquipmentResponse;
import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import com.likelion.routineeatbe.domain.cookingEquipment.repository.CookingEquipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CookingEquipmentService {
    private final CookingEquipmentRepository cookingEquipmentRepository;

    /**
     * - 검색어 입력 받아서 식재료 명 기준으로 필터링
     * 1. StringUtils.hasText(search)으로 검색어가 실제로 있을 때만 필터링 수행
     * 2. 검색어가 있다면 레포에 정의한 검색어 포함하는 객체 리턴 함수 사용
     * 3. 검색어 없으면 바로 findALl() 실행
     * 4. CookingEquipmentResponse 포장함수 from으로 포장하여 리스트 리턴
     * @param search
     * @return
     */
    @Transactional(readOnly = true)
    public List<CookingEquipmentResponse> getCookingEquipments(String search){
        List<CookingEquipment> cookingEquipments;
        if(StringUtils.hasText(search)){ /* 1. 검색어 유무 검사 */
            /* 2. 검색어 있으니 레포의 findByNameContaining() */
            cookingEquipments=cookingEquipmentRepository.findByNameContaining(search);
        } else { /* 3. 검색어 없으니 findALl() */
            cookingEquipments=cookingEquipmentRepository.findAll();
        }
        /* 4. (CookingEquipmentResponse::fromCookingEquipmentEntity)로 각각을 포장한 리스트 반환 */
        return cookingEquipments.stream()
                .map(CookingEquipmentResponse::fromCookingEquipmentEntity)
                .toList();
    }
}
