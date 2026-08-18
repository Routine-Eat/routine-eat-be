package com.likelion.routineeatbe.domain.cookingTip.repository;

import com.likelion.routineeatbe.domain.cookingTip.entity.CookingStepTip;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CookingStepTipRepository extends JpaRepository<CookingStepTip, Long> {

    /**
     * 요리 단계에 연결된 요리 팁과 팁 콘텐츠를 한 번에 조회합니다.
     *
     * @param cookingStepId 요리 단계 PK
     * @return 요리 팁과 콘텐츠가 로딩된 단계별 요리 팁 목록
     */
    @Query("""
            select distinct cookingStepTip
            from CookingStepTip cookingStepTip
            join fetch cookingStepTip.cookingTip cookingTip
            left join fetch cookingTip.contents
            where cookingStepTip.cookingStep.id = :cookingStepId
            """)
    List<CookingStepTip> findAllWithCookingTipAndContentsByCookingStepId(
            @Param("cookingStepId") Long cookingStepId
    );
}
