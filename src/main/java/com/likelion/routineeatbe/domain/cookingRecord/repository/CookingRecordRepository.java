package com.likelion.routineeatbe.domain.cookingRecord.repository;

import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CookingRecordRepository extends
        JpaRepository<CookingRecord, Long>,
        CookingRecordRepositoryCustom {

    /**
     * 사용자의 특정 기간 내 요리 결과 저장 건수를 조회합니다.
     *
     * @param userId 사용자 PK
     * @param startAt 조회 시작 시각
     * @param endAt 조회 종료 시각 미만
     * @return 요리 결과 저장 건수
     */
    long countByUser_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanAndDifficultyLevelIsNotNull(
            Long userId,
            LocalDateTime startAt,
            LocalDateTime endAt
    );

    /**
     * 사용자와 세션 상태에 해당하는 가장 최근 요리 기록을 조회합니다.
     *
     * @param userId 사용자 PK
     * @param status 요리 세션 상태
     * @return 생성일과 PK 기준 가장 최근 요리 기록
     */
    Optional<CookingRecord>
            findFirstByUser_IdAndCookingSession_StatusOrderByCreatedAtDescIdDesc(
                    Long userId,
                    CookingSessionStatus status
            );

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

    /**
     * 사용자 소유 요리 기록을 비관적 쓰기 잠금으로 조회합니다.
     *
     * @param cookingRecordId 요리 기록 PK
     * @param userId 사용자 PK
     * @return 잠금이 적용된 사용자 소유 요리 기록
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select cookingRecord
            from CookingRecord cookingRecord
            where cookingRecord.id = :cookingRecordId
              and cookingRecord.user.id = :userId
            """)
    Optional<CookingRecord> findByIdAndUserIdForUpdate(
            @Param("cookingRecordId") Long cookingRecordId,
            @Param("userId") Long userId
    );

    /**
     * 사용자 소유 요리 기록을 레시피와 메뉴까지 함께 조회합니다.
     *
     * @param cookingRecordId 조회할 요리 기록 PK
     * @param userId 요리 기록 소유 사용자 PK
     * @return 레시피와 메뉴가 함께 조회된 사용자 소유 요리 기록
     */
    @Query("""
            select cookingRecord
            from CookingRecord cookingRecord
            join fetch cookingRecord.recipe recipe
            join fetch recipe.menu menu
            where cookingRecord.id = :cookingRecordId
              and cookingRecord.user.id = :userId
            """)
    Optional<CookingRecord> findByIdAndUserIdWithRecipeAndMenu(
            @Param("cookingRecordId") Long cookingRecordId,
            @Param("userId") Long userId
    );

    /**
     * 사용자 소유 요리 기록을 요리 세션과 함께 조회합니다.
     *
     * @param cookingRecordId 조회할 요리 기록 PK
     * @param userId 요리 기록 소유 사용자 PK
     * @return 요리 세션이 함께 조회된 사용자 소유 요리 기록
     */
    @Query("""
            select cookingRecord
            from CookingRecord cookingRecord
            left join fetch cookingRecord.cookingSession cookingSession
            where cookingRecord.id = :cookingRecordId
              and cookingRecord.user.id = :userId
            """)
    Optional<CookingRecord> findByIdAndUserIdWithCookingSession(
            @Param("cookingRecordId") Long cookingRecordId,
            @Param("userId") Long userId
    );

    /**
     * 사용자 소유 요리 기록을 레시피, 사용 음식 재료와 함께 조회합니다.
     *
     * @param cookingRecordId 조회할 요리 기록 PK
     * @param userId 요리 기록 소유 사용자 PK
     * @return 레시피와 사용 음식 재료가 함께 조회된 사용자 소유 요리 기록
     */
    @Query("""
            select distinct cookingRecord
            from CookingRecord cookingRecord
            join fetch cookingRecord.recipe recipe
            left join fetch cookingRecord.cookingSession cookingSession
            left join fetch cookingRecord.foodIngredients cookingRecordFoodIngredient
            left join fetch cookingRecordFoodIngredient.foodIngredient foodIngredient
            where cookingRecord.id = :cookingRecordId
              and cookingRecord.user.id = :userId
            """)
    Optional<CookingRecord> findByIdAndUserIdWithFoodIngredients(
            @Param("cookingRecordId") Long cookingRecordId,
            @Param("userId") Long userId
    );
}
