package com.likelion.routineeatbe.domain.cookingTip.repository;

import com.likelion.routineeatbe.domain.cookingTip.entity.CookingTip;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CookingTipRepository extends JpaRepository<CookingTip, Long> {

    /**
     * 전체 요리 팁을 PK 오름차순으로 조회합니다.
     *
     * @return PK 오름차순으로 정렬된 전체 요리 팁
     */
    List<CookingTip> findAllByOrderByIdAsc();
}
