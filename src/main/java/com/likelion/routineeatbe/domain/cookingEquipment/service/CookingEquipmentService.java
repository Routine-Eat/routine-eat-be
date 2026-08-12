package com.likelion.routineeatbe.domain.cookingEquipment.service;

import com.likelion.routineeatbe.domain.cookingEquipment.dto.response.CookingEquipmentResponse;
import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipmentSymbol;
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
     * - 검색어 및 심볼 입력 받아서 조리도구 명 기준으로 필터링
     * @param search
     * @param symbol
     * @return
     */
    @Transactional(readOnly = true)
    public List<CookingEquipmentResponse> getCookingEquipments(String search, CookingEquipmentSymbol symbol){
        // 빈 문자열("")이나 공백이 들어오면 null로 변경해서 레포지토리에 전달
        String searchParam = StringUtils.hasText(search) ? search : null;

        List<CookingEquipment> cookingEquipments = cookingEquipmentRepository.searchEquipments(searchParam, symbol);

        /* (CookingEquipmentResponse::from)로 각각을 포장한 리스트 반환 */
        return cookingEquipments.stream()
                .map(CookingEquipmentResponse::from)
                .toList();
    }
}
