package com.likelion.routineeatbe.domain.cookingEquipment.repository;

import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipmentSymbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * 전체 조리도구를 검색어와 심볼로 필터링하여 조회
     * @param search
     * @param symbol
     * @return
     */
    @Query("SELECT c FROM CookingEquipment c WHERE " +
            "(:search IS NULL OR c.name LIKE CONCAT('%', :search, '%')) AND " +
            "(:symbol IS NULL OR c.symbol = :symbol)")
    List<CookingEquipment> searchEquipments(
            @Param("search") String search,
            @Param("symbol") CookingEquipmentSymbol symbol
    );
}
