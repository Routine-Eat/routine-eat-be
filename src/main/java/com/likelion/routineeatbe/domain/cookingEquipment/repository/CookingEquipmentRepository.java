package com.likelion.routineeatbe.domain.cookingEquipment.repository;

import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CookingEquipmentRepository extends JpaRepository<CookingEquipment,Long> {

    /**
     * 이름에 검색어가 포함된 조리 도구를 조회합니다.
     *
     * @param name 조리 도구 이름 검색어
     * @return 검색어가 이름에 포함된 조리 도구 목록
     */
    List<CookingEquipment> findByNameContaining(String name);

    /**
     * 전체 조리 도구를 타입과 식별자 순으로 조회합니다.
     *
     * @return Gemini 입력에 사용할 조리 도구 목록
     */
    List<CookingEquipment> findAllByOrderByTypeAscIdAsc();
}
