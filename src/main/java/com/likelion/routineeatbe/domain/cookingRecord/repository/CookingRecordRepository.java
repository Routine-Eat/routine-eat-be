package com.likelion.routineeatbe.domain.cookingRecord.repository;

import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CookingRecordRepository extends JpaRepository<CookingRecord, Long> {

    /**
     * 사용자와 레시피에 연결된 차단 대상 요리 세션의 존재 여부를 확인합니다.
     *
     * @param userId 사용자 PK
     * @param recipeId 레시피 PK
     * @param statuses 요리 시작을 차단할 세션 상태 목록
     * @return 차단 대상 세션 존재 여부
     */
    @Query("""
            select case when count(cookingRecord) > 0 then true else false end
            from CookingRecord cookingRecord
            join cookingRecord.cookingSession cookingSession
            where cookingRecord.user.id = :userId
              and cookingRecord.recipe.id = :recipeId
              and cookingSession.status in :statuses
            """)
    boolean existsBlockingSession(
            @Param("userId") Long userId,
            @Param("recipeId") Long recipeId,
            @Param("statuses") Collection<CookingSessionStatus> statuses
    );
}
