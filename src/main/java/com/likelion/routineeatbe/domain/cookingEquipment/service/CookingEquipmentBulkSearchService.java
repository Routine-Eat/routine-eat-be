package com.likelion.routineeatbe.domain.cookingEquipment.service;

import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import com.likelion.routineeatbe.domain.cookingEquipment.repository.CookingEquipmentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CookingEquipmentBulkSearchService {

    private final CookingEquipmentRepository cookingEquipmentRepository;

    /**
     * (1) 작업 목적
     * Gemini 분석에 제공할 전체 조리 도구 기준 데이터를 조회합니다.
     *
     * (2) 세부 작업 내용
     * - 조리 도구를 타입과 식별자 오름차순으로 조회합니다.
     * - 호출자가 변경할 수 없는 목록으로 반환합니다.
     *
     * @return 타입과 식별자 순으로 정렬된 전체 조리 도구 목록
     */
    @Transactional(readOnly = true)
    public List<CookingEquipment> findAll() {
        log.info("[CookingEquipmentBulkSearchService] 조리 도구 일괄 조회 시작 | findAll() - START");

        List<CookingEquipment> result = List.copyOf(
                cookingEquipmentRepository.findAllByOrderByTypeAscIdAsc()
        );

        log.info(
                "[CookingEquipmentBulkSearchService] 조리 도구 일괄 조회 종료 | findAll() - END | resultSize: {}",
                result.size()
        );
        return result;
    }
}
