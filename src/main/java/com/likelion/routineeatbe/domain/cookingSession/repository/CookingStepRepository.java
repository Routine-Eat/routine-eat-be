package com.likelion.routineeatbe.domain.cookingSession.repository;

import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import java.util.List;
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

    /**
     * 요리 세션 PK에 연결된 요리 단계 중 level이 0보다 큰 단계를
     * 단계 번호 오름차순으로 조회합니다.
     *
     * @param cookingSessionId 요리 세션 PK
     * @param level 조회할 최소 단계 번호
     * @return 단계 번호로 정렬된 요리 단계 목록
     */
    List<CookingStep> findAllByCookingSessionIdAndLevelGreaterThanOrderByLevelAsc(Long cookingSessionId, Long level);

    /**
     * 요리 세션 PK에 연결된 실제 요리 단계를 단계 번호 오름차순으로 조회합니다.
     *
     * @param cookingSessionId 요리 세션 PK
     * @param minimumLevel 조회할 최소 단계 번호
     * @return 정렬된 실제 요리 단계 목록
     */
    List<CookingStep> findAllByCookingSessionIdAndLevelGreaterThanEqualOrderByLevelAsc(
            Long cookingSessionId,
            Long minimumLevel
    );
}
