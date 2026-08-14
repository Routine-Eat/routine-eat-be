package com.likelion.routineeatbe.domain.cookingSession.repository;

import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CookingStepRepository extends JpaRepository<CookingStep, Long> {

    /**
     * 요리 세션 PK와 단계 번호로 요리 단계를 조회합니다.
     *
     * @param cookingSessionId 요리 세션 PK
     * @param level 요리 단계 번호
     * @return 해당 세션의 요리 단계
     */
    Optional<CookingStep> findByCookingSessionIdAndLevel(Long cookingSessionId, Long level);
}
